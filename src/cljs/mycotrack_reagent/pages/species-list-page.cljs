(ns mycotrack-reagent.pages.species-list-page
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
(def species-list (atom #{}))
(def new-species (atom {}))

;; Init data

(defn refresh-species []
  (go (let [response (<! (http/get "/api/species" {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 200 )
      (swap! species-list union (:body response))))))

;; -------------------------
;; Views

(defn species-list-comp []
  (fn [] [:div (for [species @species-list]
     [:a {:href (str "#/species/" (:_id species))} [:div.image-tile.col-xs-5
      [:p (:commonName species)]
      [:p (:scientificName species)]
      [:div {:key (:_id species)}
       [:img {:src (:imageUrl species)}]]]])]))

(defn save-species [value]
  (fn [] (go (let [response (<! (http/post "/api/species" {:json-params @new-species :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (prn "Species saved")
      (swap! species-list conj (:body response)))))))

(defn new-species-input [value]
  (fn [] [:div
   [:input {:type "text"
            :placeholder "common name"
           :value (:commonName @value)
           :on-change #(swap! value assoc :commonName (-> % .-target .-value))}]
   [:input {:type "text"
            :placeholder "scientific name"
           :value (:scientificName @value)
           :on-change #(swap! value assoc :scientificName (-> % .-target .-value))}]
   [:input {:type "text"
            :placeholder "image url"
           :value (:imageUrl @value)
           :on-change #(swap! value assoc :imageUrl (-> % .-target .-value))}]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-species value ) }]]))

(defn species-list-page []
  [:div [:h2 "Welcome to the Species Page"]
   [:div [:a {:href "#/"} "go to home page"]
   [new-species-input new-species]]
   [species-list-comp]])
