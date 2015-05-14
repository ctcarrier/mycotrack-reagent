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
(def culture (atom #{}))
(def new-project (atom {:enabled true}))

;; Init data
(defn refresh-projects []
  (go (let [url (str "/api/projects?"
                  (when (not (clojure.string/blank? (session/get :cultureId))) (str "cultureId=" (session/get :cultureId)))
                  (when (not (clojure.string/blank? (session/get :containerId))) (str "&containerId=" (session/get :containerId))))
            response (<! (http/get url {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 200 )
      (reset! projects (:body response))))))

(defn refresh-cultures []
  (go (let [url (str "/api/cultures/" (session/get :cultureId))
            response (<! (http/get url {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
        (when (= (:status response) 200 )
          (reset! culture (:body response))))))

;; -------------------------
;; Views

(defn project-list []
  (fn [] [:div.col-xs-12
          [:div.col-xs-6 [:h2 (str "Projects > " (:name @culture) " > " (session/get :containerId))]]
          [:div.col-xs-6 [:a {:href "#/new_project"} "+ New"]]
          [:div.col-xs-12 [:a {:href "#/about"} "go to about page"]]
          [:div.col-xs-12 [:a {:href "#/species"} "go to species page"]]
          [:div.col-xs-12
   [:table
    [:thead
      [:tr
       [:th "Description"]
       [:th "Substrate"]
       [:th "Location"]
       [:th "Count"]
       [:th "Actions"]]]
     [:tbody
      (for [project @projects]
       [:tr {:key (:_id project)}
        [:div.col-xs-12
          [:td (:description project)]
          [:td (:name (:substrate project))]
          [:td (:name (:name (:location project)))]
          [:td (:count project)]
          [:td [:p [:a {:href (str "#/projects/" (:_id project))} "Spawn to new container"]]
           [:p [:a {:href (str "#/project_location/" (:_id project))} "Move to new location"]]]]])]]]]))

(defn projects-list-page []
  (prn "yoyo1")
  (refresh-projects)
  (refresh-cultures)
   [project-list])
