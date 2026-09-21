(ns example.sse
  (:require
    [example.session :as session]
    [starfederation.datastar.clojure.adapter.ring :refer [->sse-response on-open on-close on-exception]]
    [starfederation.datastar.clojure.api :as d*]))


(defn send!
  "Patch `html` into the client behind `sse`. Returns false when the connection is dead."
  [[sse html]]
  (prn "send!" sse html)
  [sse (d*/patch-elements! sse html)])


(defn handle-sse-open
  [{{{route-id :id} :data} :reitit.core/match :as req} sse-conn]
  (prn "Opening SSE stream" {:level :info :data {:session route-id}})
  (session/update-session sse-conn req route-id))


(defn handle-sse-close
  [{{{route-id :id} :data} :reitit.core/match :as _req} sse-conn]
  (prn "Closing SSE stream for" {:level :info :data {:session route-id}})
  (session/remove-session sse-conn))


(defn handle-sse-exception
  " Args:
   - `sse-gen`: the SSEGenerator
   - `e`: the exception
   - `ctx`: context information about the exception, a map whose keys are:
     - `:sse-gen`: the sse generator throwing
     - `:event-type`: type of the event that failed
     - `:data-lines`: data lines for this event
     - `:opts`: options used when sending"
  [_sse e ctx]
  (prn "Entered `handle-sse-exception`")
  (prn (ex-message e)
       #_{:level :error :data (assoc ctx :exception e)})
  ;; truthy => SDK closes the generator and fires on-close (-> remove-session)
  true)


(defn sse-handler
  [req respond _raise]
  (respond
    (->sse-response req
                    {on-open #(handle-sse-open req %)
                     on-close #(handle-sse-close req %)
                     on-exception handle-sse-exception})))
