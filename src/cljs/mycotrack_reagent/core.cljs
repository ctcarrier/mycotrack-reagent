(ns mycotrack-reagent.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.set :refer [union]]
            [mycotrack-reagent.pages.projects-list-page :refer [projects-list-page refresh-projects]]
            [mycotrack-reagent.pages.species-list-page :refer [species-list-page refresh-species]]
            [mycotrack-reagent.pages.species-detail-page :refer [species-detail-page refresh-cultures]]
            [mycotrack-reagent.pages.new-project-page :refer [new-project-page]]
            [mycotrack-reagent.channels :refer [echo-chan]]
            [mycotrack-reagent.pages.aggregate-page :refer [aggregation-page]])
  (:import goog.History))

;; -------------------------
;; Views

(defn about-page []
  [:div [:h2 "About mycotrack-reagent"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/projects" [query-params]
  (prn query-params)
  (session/put! :cultureId (:cultureId query-params))
  (session/put! :containerId (:containerId query-params))
  (session/put! :current-page #'projects-list-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/" []
  (session/put! :current-page #'aggregation-page))

(secretary/defroute "/species" []
  (session/put! :current-page #'species-list-page))

(secretary/defroute "/new_project" []
  (session/put! :current-page #'new-project-page))

(secretary/defroute "/species/:id" {:as params}
  (prn (str "Species coming in: " (:id params)))
  (session/put! :current-species (:id params))
  (session/put! :current-page #'species-detail-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defmulti redirect
  (fn[x] x))

;params is not used, so we could have used [_]
(defmethod redirect "projects" [params]
 (session/put! :current-page #'projects-list-page))

(refresh-projects)
(refresh-species)
(go (redirect (<! echo-chan)))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (prn "INITIALIZED!")
  (hook-browser-navigation!)
  (mount-root))
