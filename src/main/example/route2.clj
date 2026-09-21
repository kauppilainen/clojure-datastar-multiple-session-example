(ns example.route2
  (:require
    [dev.onionpancakes.chassis.core :as h]
    [example.mock :as mock]))


(def route-id :route/hello-world2)


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
  {:message ((:get-message queries) 2)
   :tick @(-> subscriptions :ticker :value)})

;; "session step failed:Cannot invoke \"clojure.lang.IFn.invoke(Object)\"
;; {:e #error {
 ;; :cause \"Cannot invoke \\\"clojure.lang.IFn.invoke(Object)\\\"\"
 ;; :via
 ;; [{:type java.lang.NullPointerException
 ;;   :message \"Cannot invoke \\\"clojure.lang.IFn.invoke(Object)\\\"\"
 ;;   :at [example.route2$data__GT_render invokeStatic \"route2.clj\" 26]}]
 ;; :trace
 ;; [[example.route2$data__GT_render invokeStatic \"route2.clj\" 26]
 ;;  [example.route2$data__GT_render invoke \"route2.clj\" 23]
 ;;  [example.session$render_session invokeStatic \"session.clj\" 30]
 ;;  [example.session$render_session invoke \"session.clj\" 28]
 ;;  [clojure.core$comp$fn__5895 invoke \"core.clj\" 2591]
 ;;  [example.core$render_and_cleanup_all_BANG_$fn__15867 invoke \"core.clj\" 74]
 ;;  [example.core$render_and_cleanup_all_BANG_ invokeStatic \"core.clj\" 73]
 ;;  [example.core$render_and_cleanup_all_BANG_ invoke \"core.clj\" 69]
 ;;  [example.lifecycle$start_BANG_$fn__16353 invoke \"lifecycle.clj\" 17]
 ;;  [clojure.lang.AFn run \"AFn.java\" 22]
 ;;  [java.util.concurrent.Executors$RunnableAdapter call \"Executors.java\" 545]
 ;;  [java.util.concurrent.FutureTask runAndReset \"FutureTask.java\" 369]
 ;;  [java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask run \"ScheduledThreadPoolExecutor.java\" 310]
 ;;  [java.util.concurrent.ThreadPoolExecutor runWorker \"ThreadPoolExecutor.java\" 1090]
 ;;  [java.util.concurrent.ThreadPoolExecutor$Worker run \"ThreadPoolExecutor.java\" 614]
 ;;  [java.lang.Thread run \"Thread.java\" 1474]]}
;; }"

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
