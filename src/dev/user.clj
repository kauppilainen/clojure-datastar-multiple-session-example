(ns user
  (:require
    [clj-reload.core :as reload]
    [example.core :as c]
    [example.lifecycle :as lifecycle]
    [example.server :as server]
    [example.session :as session]))


(alter-var-root #'*warn-on-reflection* (constantly true))


(reload/init
  {:no-reload ['user]})


(defn reload!
  []
  (reload/reload))


(comment
  (reload/reload)


  *e

  ;; start server
  (server/reboot-jetty-server! #'c/handler)

  ;; start rendering loop
  (lifecycle/restart!)

  @session/!state

  ;; number of SSE connections
  (count @session/!state)

  (reset! session/!state {})
  )
