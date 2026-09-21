(ns example.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dev.onionpancakes.chassis.compiler :as hc]
    [dev.onionpancakes.chassis.core :as h]
    [example.route1 :as route1]
    [example.route2 :as route2]
    [example.route3 :as route3]
    [example.session :as session]
    [example.sse :refer [sse-handler]]
    [reitit.ring :as rr]
    [reitit.ring.middleware.parameters :as rmparams]
    ;; [ring-middleware-csp.core :refer [wrap-csp]]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.util.response :as ruresp]
    [starfederation.datastar.clojure.api :as d*]))


(defn home-page
  [n]
  (-> (io/resource (str "public/hello-world" n ".html"))
      slurp
      (string/split-lines)
      (->> (drop 3)
           (apply str))))


(defn home1
  [_req respond _raise]
  (prn "home1 route visited")
  (respond
    (-> (home-page nil)
        (ruresp/response)
        (ruresp/content-type "text/html"))))


(defn home2
  [_req respond _raise]
  (prn "home1 route visited")
  (respond
    (-> (home-page 2)
        (ruresp/response)
        (ruresp/content-type "text/html"))))


(defn home3
  [_req respond _raise]
  (prn "home1 route visited")
  (respond
    (-> (home-page 3)
        (ruresp/response)
        (ruresp/content-type "text/html"))))


(defn render-session
  [{:keys [render data->render _unmount data]}]
  (-> data data->render render))


(defn render-and-emit-to-all-sessions
  []
  (doseq [[sse-connection data] @session/!state]
    (let [open? (d*/patch-elements!
                  sse-connection (render-session data))]
      ;; TODO add unmount if not open here
      (prn "SSE connection open?:" open?))))


(comment
  (render-and-emit-to-all-sessions)

  
  )


(def routes
  [["/"  {:handler home1}]
   ["/1" {:handler home1}]
   ["/2" {:handler home2}]
   ["/3" {:handler home3}]
   ["/hello-world" {:id route1/route-id
                    :handler sse-handler
                    :middleware [rmparams/parameters-middleware]}]
   ["/hello-world2" {:id route2/route-id
                     :handler sse-handler
                     :middleware [rmparams/parameters-middleware]}]
   ["/hello-world3" {:id route3/route-id
                     :handler sse-handler
                     :middleware [rmparams/parameters-middleware]}]])


(def router (rr/router routes))

#_(def handler (rr/ring-handler router))


(def handler2
  (rr/ring-handler
    router
    {:async? true
     :middleware [[wrap-cookies]
                  [wrap-content-type]
                  [wrap-resource "assets"]
                  ;; Ring session carrying the Auth0 sign-in (`api.auth.session`): in-memory
                  ;; store — a restart signs everyone out, which the Electric app's per-load
                  ;; re-authentication effectively did. SameSite=Lax so Auth0's callback GET
                  ;; (a top-level navigation) still carries the cookie; :secure off locally,
                  ;; where dev serves plain http on tailnet origins (same gate as ff-cookies).
                  ;; TODO Hold on relaxing this. Look into if any auth issues arise
                  [wrap-session #_{:cookie-attrs {:http-only true
                                 :same-site :lax
                                 :secure    (not ops/is-local)}}]
                  [wrap-params]
                  [wrap-keyword-params]
                  [wrap-multipart-params]]}))


(comment
  @session/!state


  ;; sse opts
  ;; - [[id]]
  ;; - [[retry-duration]]
  ;; - [[selector]]
  ;; - [[patch-mode]]
  ;; - [[use-view-transition]]
  ;; - [[view-transition-selector]]
  ;; - [[element-ns]]
    
  (render-and-emit-to-all-sessions)



  )
