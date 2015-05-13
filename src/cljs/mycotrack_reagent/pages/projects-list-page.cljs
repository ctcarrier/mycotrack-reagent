(ns mycotrack-reagent.pages.projects-list-page
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.set :refer [union]])
  (:import goog.History))

;; Atoms
(def projects (atom #{}))
(def new-project (atom {:enabled true}))

;; Init data
(defn refresh-projects []
  (.log js/console "yoyo2")
  (go (let [url (str "/api/projects?"
                  (when (not (clojure.string/blank? (session/get :cultureId))) (str "cultureId=" (session/get :cultureId)))
                  (when (not (clojure.string/blank? (session/get :containerId))) (str "&containerId=" (session/get :containerId))))
            response (<! (http/get url {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 200 )
      (reset! projects (:body response))))))

;; -------------------------
;; Views

(defn project-list []
  (fn [] [:div.col-xs-12
   [:table
    [:thead
      [:tr
       [:th "Description"]
       [:th "Container"]
       [:th "Substrate"]
       [:th "Count"]
       [:th "Species"]
       [:th "Culture"]
       [:th "Actions"]]]
     [:tbody
      (for [project @projects]
       [:tr {:key (:_id project)}
        [:div.col-xs-12
          [:td (:description project)]
          [:td (:container project)]
          [:td (:substrate project)]
          [:td (:count project)]
          [:td (:speciesId project)]
          [:td (:cultureId project)]
          [:td [:a {:href (str "#/projects/" (:_id project))} "Spawn to new container"]
           [:a {:href (str "#/project_location/" (:_id project))} "Move to new location"]]]])]]]))

(defn projects-list-page []
  (prn "yoyo1")
  (refresh-projects)
  [:div.col-xs-12
   [:div.col-xs-6 [:h2 "Projects"]]
   [:div.col-xs-6 [:a {:href "#/new_project"} "+ New"]]
   [:div.col-xs-12 [:a {:href "#/about"} "go to about page"]]
   [:div.col-xs-12 [:a {:href "#/species"} "go to species page"]]
   [project-list]])
