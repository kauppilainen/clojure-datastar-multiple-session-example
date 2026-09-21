(ns example.lifecycle
  (:require
    [example.core :as core])
  (:import
    (java.util.concurrent
      Executors
      ScheduledExecutorService
      TimeUnit)))


(defn start!
  "Ticks `f` every `ms`. Returns a stop fn."
  [f ms]
  (let [ex (Executors/newSingleThreadScheduledExecutor)]
    ;; (prn "Render loop tick" (str (Instant/now)))
    ;; ponytail: try/catch is mandatory, an uncaught throw silently kills the schedule
    (.scheduleAtFixedRate ex #(try (f) (catch Throwable t (println "tick error" t)))
                          0 ms TimeUnit/MILLISECONDS)
    (fn stop! [] (.shutdownNow ^ScheduledExecutorService ex) nil)))


(def tick 1000)

(defonce !loop (atom nil))


(defn restart!
  []
  (swap! !loop (fn [stop] (when stop (stop)) (start! core/render-and-cleanup-all! tick))))


(defn stop!
  []
  (swap! !loop (fn [stop] (when stop (stop)) nil)))


(comment
  (restart!)
  (stop!)
  ;; or bare, no global:
  (def stop (start! core/render-and-cleanup-all! 1000))
  (stop)


  )
