(ns user
  (:require
    [example.session :as session]
    [example.server :as server]
    [example.render :as render]
    [example.core :as c]
    #_[clj-reload.core :as reload]))


(alter-var-root #'*warn-on-reflection* (constantly true))


#_(reload/init
  {:no-reload ['user]})


#_(defn reload! []
  (reload/reload))






(comment
  *e

  ;; start server
  (server/reboot-jetty-server! #'c/handler)

  ;; start rendering loop
  (render/restart!)

  @session/!state

  ;; number of SSE connections
  (count @session/!state)


  )


