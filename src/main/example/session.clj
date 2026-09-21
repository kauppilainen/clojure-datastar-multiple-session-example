(ns example.session
  (:require
    [example.route1 :as route1]
    [example.route2 :as route2]
    [example.route3 :as route3]
    [starfederation.datastar.clojure.api :as d*])
  (:import
    (java.time
      Instant)))


(comment
  {"session-id" ; IDs a client, i.e an SSE-connection + data
   {:last-read nil ; used for data unsubscription
    :sse-connection nil ; used for sending HTML
    :current-route :route/route-id ; only current route data needed
    :route/route-id {:init ... ; query results from first render
                     :subscriptions
                     {"proxy-idN" ... ; proxy-ref, feeds to render, used when unmounting
                      }}
    :route/route-idN {#_...}}}

  {:sse-connection
   {:render (fn [data])
    :data {:init nil
           :subscriptions {}}
    }
   }
  )


(def route-fns
  {route1/route-id route1/lifecycle-fns
   route2/route-id route2/lifecycle-fns
   route3/route-id route3/lifecycle-fns})


(defonce !state
  (atom {}))


(defn update-session
  [sse route-id]
  (swap! !state assoc sse
         (merge
           (get route-fns route-id)
           {:data {:init {:message "Hello from rendering loop"} ; query results from first render
                   :subscriptions {}}})))


(defn remove-session
  "`unmount` takes subscriptions and gracefully shuts them down"
  [sse]
  (d*/close-sse! sse)
  ;; TODO [ ] unmount all route subscriptions
  (swap! !state dissoc sse))


(comment
  @!state
  (reset! !state {})

  )
