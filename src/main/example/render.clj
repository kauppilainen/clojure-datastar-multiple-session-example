(ns example.render
  (:require
    [example.session :as session]
    [starfederation.datastar.clojure.api :as d*])
  (:import
    (java.util.concurrent
      Executors
      ScheduledExecutorService
      TimeUnit)))


(defn render-session
  [{:keys [render data->render _unmount data]}]
  (-> data data->render render))


(defn send!
  "Patch `html` into the client behind `sse`. Returns false when the connection is dead."
  [sse html]
  (d*/patch-elements! sse html))


(defn render-all!
  []
  (doseq [[sse route-data] @session/!state]
    (try
      (when-not (send! sse (render-session route-data))
        (println "SSE connection closed, dropping session")
        (session/remove-session sse))
      (catch Exception e
        (println "render failed" (ex-message e))))))


(defn start!
  "Ticks `f` every `ms`. Returns a stop fn."
  [f ms]
  (let [ex (Executors/newSingleThreadScheduledExecutor)]
    ;; (prn "Render loop tick" (str (Instant/now)))
    ;; ponytail: try/catch is mandatory, an uncaught throw silently kills the schedule
    (.scheduleAtFixedRate ex #(try (f) (catch Throwable t (println "tick error" t)))
                          0 ms TimeUnit/MILLISECONDS)
    (fn stop! [] (.shutdownNow ^ScheduledExecutorService ex) nil)))


(defonce !loop (atom nil))


(defn restart!
  []
  (swap! !loop (fn [stop] (when stop (stop)) (start! render-all! 1000))))


(defn stop!
  []
  (swap! !loop (fn [stop] (when stop (stop)) nil)))


(comment
  (restart!)
  (stop!)
  ;; or bare, no global:
  (def stop (start! render-all! 1000))
  (stop)

  (render-all!)

  )
