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
  (prn (str (session/get :cultureId)))
  (prn (str (session/get :containerId)))
  (prn (str
    (when (not (clojure.string/blank? (session/get :cultureId))) (str "cultureId=" (session/get :cultureId)))
    (when (not (clojure.string/blank? (session/get :containerId))) (str "&containerId=" (session/get :containerId)))))
  (go (let [response (<! (http/get "/api/projects" {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 200 )
      (reset! projects (:body response))))))

;; -------------------------
;; Views

(defn project-list []
  (fn [] [:div.col-xs-12
   [:table
    [:tr
     [:th "Description"]
     [:th "Container"]
     [:th "Substrate"]
     [:th "Species"]
     [:th "Culture"]]
    (for [project @projects]
     [:tr {:key (:_id project)}
      [:div.col-xs-12
        [:td (:description project)]
        [:td (:container project)]
        [:td (:substrate project)]
        [:td (:speciesId project)]
        [:td (:cultureId project)]]])]]))

(defn save-project [value]
  (fn [] (go (let [response (<! (http/post "/api/projects" {:json-params @new-project :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (prn "Project saved")
      (swap! projects conj (:body response)))))))

(defn new-project-input [value]
  (fn [] [:div
   [:input {:type "text"
           :value (:description @value)
           :on-change #(swap! value assoc :description (-> % .-target .-value))}]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-project value ) }]]))

(defn projects-list-page []
  (refresh-projects)
  [:div.col-xs-12
   [:div.col-xs-6 [:h2 "Projects"]]
   [:div.col-xs-6 [:a {:href "#/new_project"} "+ New"]]
   [:div.col-xs-12 [:a {:href "#/about"} "go to about page"]]
   [:div.col-xs-12 [:a {:href "#/species"} "go to species page"]]
   [project-list]])
