(ns mycotrack-reagent.pages.aggregate-page
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
(def aggregate (atom #{}))

;; Init data

(defn refresh-aggregate []
  (go (let [response (<! (http/get (str "/api/aggregations") {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! aggregate (:body response))))))

;; -------------------------
;; Views

(defn aggregation-list-comp []
  (fn [] [:div.col-xs-12 (for [aggregation @aggregate]
     [:a {:href (str "#/projects?cultureId=" (:_id (:culture aggregation)) "&containerId=" (:_id (:container aggregation)))} [:div.col-xs-3.aggregation-card
      [:div.col-xs-12 (:count aggregation) " " (:name (:container aggregation))]
      [:div.col-xs-12 (:commonName (:species aggregation))]]])]))

(defn aggregation-page []
  (refresh-aggregate)
  [:div.col-xs-12
   [:div.col-xs-12
     [:div.col-xs-6 [:h2 "Active Projects"]]
     [:div.col-xs-6 [:a {:href "#/new_project"} "+ New"]]]
   [aggregation-list-comp]])
