(ns example.main
  (:require
    [example.core :as c]
    [example.server :as server]))


(defn -main [& _]
  (let [server
        #_(server/start! c/handler)
        (server/start! c/handler2)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                  (server/stop! server)
                                  (shutdown-agents))))))

(def stop (-main))
