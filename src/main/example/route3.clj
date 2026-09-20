(ns example.route3
  (:require
    [dev.onionpancakes.chassis.compiler :as hc]
    [dev.onionpancakes.chassis.core :as h]))


(def route-id :route/hello-world3)


(defn render
  [{:keys [message] :as _data}]
  (h/html
    (hc/compile
      [:div {:id "message"}
       (format "Route 3: %s" message)])))


(defn unmount
  [subscriptions]
  nil)


(def lifecycle-fns
  {:render-fn render
   :unmount-fn unmount})
