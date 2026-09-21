(ns example.route1
  (:require
    [dev.onionpancakes.chassis.compiler :as hc]
    [dev.onionpancakes.chassis.core :as h]))


(def route-id :route/hello-world)


(def data
  {:init {:message "Hello from rendering loop"}, :subscriptions {}}
  )

(defn data->render
  [data]
  (-> data :init :message)
  )

(data->render data)

(defn render
  [message]
  (h/html
    [:div {:id "message"}
     (format "Route 1: %s" message)]))


(defn unmount
  [subscriptions]
  nil)


(def lifecycle-fns
  {:render render
   :data->render data->render
   :unmount unmount})
