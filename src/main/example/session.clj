(ns example.session
  (:require
    [example.live :as live]))


(comment
  {:sse-connection
   {:render (fn [_data])
    :data {:init nil
           :subscriptions {}}
    }}

  #__)


(def route-fns
  {live/route-id live/lifecycle-fns})


(defonce !state
  (atom {}))


(defn render-session
  [sse {:keys [render data->render data _mount _unmount]}]
  [sse (-> data data->render render)])


(defn update-session
  [sse req route-id]
  (swap! !state assoc sse
         (let [{:keys [mount] :as lifecycle-fns} (get route-fns route-id)]
           (assoc lifecycle-fns :data (mount req)))))


(defn remove-session
  "`unmount` takes subscriptions and gracefully shuts them down.
  Does not close `sse`: the SDK closes it (on-exception -> true) and calls
  on-close -> here, so closing again would re-enter."
  [sse]
  (swap! !state dissoc sse))


(defn cleanup-session!
  "Drops the session when `alive?` (the result of a send) is false. Returns `alive?`."
  [[sse alive?]]
  (when-not alive?
    (println "SSE connection closed, dropping session")
    (remove-session sse))
  ;; TODO [ ] unmount all route subscriptions
  alive?)


(comment
  @!state
  (reset! !state {})

  )
