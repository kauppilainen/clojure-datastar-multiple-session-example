(ns example.route1
  (:require
    [dev.onionpancakes.chassis.core :as h]
    [example.mock :as mock]))


(def route-id :route/hello-world)


(def queries
  "Pull: `(query input)` whenever render needs a value."
  {:get-message #(mock/get-message %)})


(defn mount
  "Called once per session. `init` is whatever the request gives us (params, user).
  Returns the :data the render loop reads every tick."
  [init]
  {:init init
   :queries queries
   :subscriptions {:ticker (mock/subscription #(rand-int 100) 700)}})


(defn data->render
  "From data to what render expects: pulls queries with `init` as input, reads subscriptions."
  [{:keys [_init queries subscriptions]}]
  {:message ((:get-message queries) 1)
   :tick @(-> subscriptions :ticker :value)})


(defn render
  [{:keys [message tick]}]
  (h/html
    [:div {:id "message"}
     message " " tick]))


(defn unmount
  [subscriptions]
  (run! (fn [{:keys [stop]}] (stop)) (vals subscriptions)))


(def lifecycle-fns
  {:data->render data->render
   :render render
   :mount mount
   :unmount unmount})


(comment
  ;; check: tick changes between reads, unmount stops it
  (let [data (mount {:user "felix"})
        a (data->render data)
        _ (Thread/sleep 800)
        b (data->render data)]
    (unmount (:subscriptions data))
    (assert (not= (:tick a) (:tick b)) "subscription should have advanced")
    [a b (render b)])

  )
