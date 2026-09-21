(ns example.route3
  (:require
    [dev.onionpancakes.chassis.compiler :as hc]
    [dev.onionpancakes.chassis.core :as h]))


(def route-id :route/hello-world3)

(defn data->render
  [data]
  (-> data :init :message)
  )

(defn render
  [message]
  (h/html
    (hc/compile
      [:div {:id "message"}
       (format "Route 3: %s" message)])))


(defn unmount
  [subscriptions]
  nil)


(def lifecycle-fns
  {:render render
   :data->render data->render
   :unmount unmount})
