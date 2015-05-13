(ns mycotrack-reagent.pages.locations-list-page
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
(def locations (atom #{}))

;; Init data
(defn refresh-locations []
  (go (let [url "/api/locations"
            response (<! (http/get url {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 200 )
      (reset! locations (:body response))))))

;; -------------------------
;; Views

(defn location-list []
  (fn [] [:div.col-xs-12
   [:table
    [:thead
      [:tr
       [:th "Name"]
       [:th "Created Date"]]]
     [:tbody
      (for [location @locations]
       [:tr {:key (:_id location)}
        [:div.col-xs-12
          [:td (:name location)]
          [:td (:createdDate location)]]])]]]))

(defn locations-list-page []
  (prn "yoyo1")
  (refresh-locations)
  [:div.col-xs-12
   [:div.col-xs-6 [:h2 "Locations"]]
   [:div.col-xs-6 [:a {:href "#/new_location"} "+ New"]]
   [:div.col-xs-12 [:a {:href "#/about"} "go to about page"]]
   [:div.col-xs-12 [:a {:href "#/species"} "go to species page"]]
   [location-list]])
