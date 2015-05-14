(ns mycotrack-reagent.pages.project-location-page
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
(def selected-project (atom {}))

(defn reset-all []
  (reset! locations []))

;; Init data
(defn refresh-locations []
  (go (let [url "/api/locations"
            response (<! (http/get url {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 200 )
      (reset! locations (:body response))))))

(defn refresh-project [projectId]
  (go (let [response (<! (http/get (str "/api/projects/" projectId) {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! selected-project (:body response))))))

(defn save-project-location []
  (fn [] (go (let [response (<! (http/post (str "/api/projects/" (:_id @selected-project) "/children") {:json-params @selected-project :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (reset-all)
      (aset (.-location js/window) "href" "/"))))))

;; -------------------------
;; Views

(defn row [label & body]
  [:div.form-group
   [:label label]
   body])

(defn project-location-input [value]
  (fn []
    [:div.row
    [:div.col-xs-12 [:h2 (str "Move Project " (:_id @selected-project))]]
    [:div.col-xs-12
     [:form
   [row "Location"
     [:select {:value (:location @selected-project) :key "location-select" :on-change #(swap! selected-project assoc :locationId (-> % .-target .-value))}
      [:option {:value "" :key ""} ""]
      (for [location @locations]
        [:option {:value (:_id location) :key (:_id location)} (:name location)])]]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-project-location ) }]]]]))

(defn project-location-page []
  (refresh-locations)
  (refresh-project (session/get :current-project))
   [project-location-input])
