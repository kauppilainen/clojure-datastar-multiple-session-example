(ns example.route1
  (:require
    [dev.onionpancakes.chassis.compiler :as hc]
    [dev.onionpancakes.chassis.core :as h]))


(def route-id :route/hello-world)


(defn render
  [{:keys [message] :as _data}]
  (h/html
    [:div {:id "message"}
     (format "Route 1: %s" message)]))


(defn unmount
  [subscriptions]
  nil)


(def lifecycle-fns
  {:render-fn render
   :unmount-fn unmount})
