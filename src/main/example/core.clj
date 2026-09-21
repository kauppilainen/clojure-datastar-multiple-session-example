(ns example.core
  (:require
    [dev.onionpancakes.chassis.compiler :as cc]
    [dev.onionpancakes.chassis.core :as h]
    [example.live :as live]
    [example.session :as session]
    [example.sse :as sse :refer [sse-handler]]
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
    [ring.util.response :as ruresp]))


(defn home-page
  "The Datastar hello-world page, subscribed to `/live/:n` for the same number."
  [n]
  (h/html
    (cc/compile
      [h/doctype-html5
       [:html {:lang "en"}
        [:head
         [:title "Datastar SDK Demo"]
         [:script {:src "https://unpkg.com/@tailwindcss/browser@4"}]
         [:script {:type "module"
                   :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@main/bundles/datastar.js"}]]
        [:body {:class "bg-white dark:bg-gray-900 text-lg max-w-xl mx-auto my-16"}
         [:div {:data-effect (str "@get('/live/" n "')")}]
         [:div {:class "my-16 text-2xl font-bold text-transparent"
                :style "background: linear-gradient(to right in oklch, red, orange, yellow, green, blue, blue, violet); background-clip: text"}
          [:div#message "Hello, world!"]]]]])))


(defn home
  [{{:keys [n] :or {n "1"}} :path-params} respond _raise]
  (prn (str "home" n " route visited"))
  (respond
    (-> (home-page n)
        (ruresp/response)
        (ruresp/content-type "text/html"))))


(def render-and-cleanup!
  (comp session/cleanup-session! sse/send! session/render-session))


(defn render-and-cleanup-all!
  "Calls `(f sse route-data)` for every session. One failing entry doesn't stop the rest."
  []
  (doseq [[sse route-data] @session/!state]
    (try
      (render-and-cleanup! sse route-data)
      (catch Exception e
        (prn (str "Session step failed:" (ex-message e) {:e e}))))))


(def routes
  [["/live/:n" {:id live/route-id
                :handler sse-handler
                :middleware [rmparams/parameters-middleware]}]
   ["/" {:handler home}]
   ["/:n" {:handler home}]])


(def router (rr/router routes {:conflicts nil}))


(def handler
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
