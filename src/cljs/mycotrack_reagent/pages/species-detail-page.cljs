(ns mycotrack-reagent.pages.species-detail-page
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
(def culture-list (atom #{}))
(def current-species (atom {}))

;; Init data

(defn refresh-cultures [species-id]
  (go (let [response (<! (http/get (str "/api/species/" species-id "/cultures") {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (prn (:status response))
    (prn (= (:status response) 200 ))
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (prn "Cultures:")
      (prn (:body response))
      (swap! culture-list union (:body response))
      (prn culture-list)))))

(defn get-species [species-id]
  (go (let [response (<! (http/get (str "/api/species/" species-id) {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (prn "Getting species:")
      (prn (:body response))
      (swap! current-species union (:body response))))))

;; -------------------------
;; Views

(defn culture-list-comp []
  (fn [] [:div.col-xs-12.pad-top (for [culture @culture-list]
     [:a {:href (str "#/cultures/" (:_id culture)) :key (:_id culture)} [:div.image-tile.col-xs-5
      [:p (:name culture)]]])]))

(defn save-culture [value]
  (prn value)
  (fn [] (go (let [response (<! (http/post "/api/cultures" {:json-params @value :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (prn "Culture saved")
      (swap! culture-list conj (:body response)))))))

(defn new-culture-input [value]
  (fn [] [:div.col-xs-6
   [:input {:type "text"
            :placeholder "name"
           :value (:name @value)
           :on-change #(swap! value assoc :name (-> % .-target .-value))}]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-culture value ) }]]))

(defn species-detail-page []
  (prn (str "Species " (session/get :current-species)))
  (get-species (session/get :current-species))
  (refresh-cultures (session/get :current-species))
  (let [new-culture (atom {:speciesId (session/get :current-species)})] [:div.col-xs-12
   [:div.col-xs-12 [:a {:href "#/"} "go to home page"]]
   [:div.col-xs-6
    [:h2 (:commonName @current-species)]
    [:h2 "Cultures" [culture-list-comp]]
    [new-culture-input new-culture]]
   [:div.col-xs-6.pad-top [:img {:src (:imageUrl @current-species)}]]]))
