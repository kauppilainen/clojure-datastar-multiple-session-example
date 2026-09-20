(ns example.session
  (:require
    [starfederation.datastar.clojure.api :as d*])
  (:import
    (java.time
      Instant)))


(comment
  {"session-id" ; IDs a client, i.e an SSE-connection + data
   {:last-read nil ; used for data unsubscription
    :sse-connection nil ; used for sending HTML
    :current-route :route/route-id ; only current route data needed
    :route/route-id {:init-data ... ; query results from first render
                     :subscriptions
                     {"proxy-idN" ... ; proxy-ref, feeds to render-fn, used when unmounting
                      }}
    :route/route-idN {#_...}}}
  )


(def !session-state
  (atom {}))


(defn update-session
  [sse session route-id]
  (swap! !session-state assoc session
         {:last-read (Instant/now)
          :sse-connection sse
          :current-route route-id ; only current route data needed
          route-id {:init-data
                    {:message "Hello from rendering loop"} ; query results from first render
                    :subscriptions {}
                    ;; {"proxy-idN" ... ; proxy-ref, feeds to render-fn, used when unmounting
                    ;;  }
                    }}))


(defn remove-session
  "`unmount-fn` takes subscriptions and gracefully shuts them down"
  [session]
  (let [{:keys [sse-connection]} (get !session-state session)]
    (d*/close-sse! sse-connection)
    ;; TODO [ ] unmount all route subscriptions
    (swap! !session-state dissoc session)))


(comment
  @!session-state

  )
