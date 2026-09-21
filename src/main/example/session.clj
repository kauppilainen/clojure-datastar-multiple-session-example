(ns example.session
  (:require
    [example.route1 :as route1]
    [example.route2 :as route2]
    [example.route3 :as route3]))


(comment
  {:sse-connection
   {:render (fn [_data])
    :data {:init nil
           :subscriptions {}}
    }}

  #__)


(def route-fns
  {route1/route-id route1/lifecycle-fns
   route2/route-id route2/lifecycle-fns
   route3/route-id route3/lifecycle-fns})


(defonce !state
  (atom {}))


(defn render-session
  [sse {:keys [render data->render _unmount data]}]
  [sse (-> data data->render render)])


(defn update-session
  [sse route-id]
  (swap! !state assoc sse
         (merge
           (get route-fns route-id)
           {:data {:init {:message "Hello from rendering loop"} ; query results from first render
                   :subscriptions {}}})))


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
