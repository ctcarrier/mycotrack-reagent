(ns mycotrack-reagent.pages.new-location-page
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
            [mycotrack-reagent.channels :refer [echo-chan]])
  (:import goog.History))

;; Atoms
(def state (atom {:doc {:description "" :substrate "" :container "" :enabled true :species "" :culture "" :count 1} :saved? false}))

(defn reset-all []
  (reset! state {:doc {:description "" :substrate "" :container "" :enabled true :species "" :culture "" :count 1} :saved? false}))

(defn set-value! [id value]
  (swap! state assoc :saved? false)
  (swap! state assoc-in [:doc id] value))

(defn get-value [id]
  (get-in @state [:doc id]))

;; Init data

;; -------------------------
;; Views

(defn row [label & body]
  [:div.form-group
   [:label label]
   body])

(defn text-input [id label]
  [row label
   [:input
     {:type "text"
       :class "form-control"
       :value (get-value id)
       :key id
       :on-change #(set-value! id (-> % .-target .-value))}]])

(defn number-input [id label]
  [row label
   [:input
     {:type "text"
       :class "form-control"
       :value (get-value id)
       :key id
       :on-change #(set-value! id (js/parseInt (-> % .-target .-value)))}]])

(defn save-location [value]
  (fn [] (go (let [response (<! (http/post "/api/locations" {:json-params (:doc @state) :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (reset-all)
      (aset (.-location js/window) "hash" "#/locations"))
    false))))

(defn new-location-input [value]
  (fn [] [:form
   [text-input :name "Name"]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-location value ) }]]))

(defn new-location-page []
  [:div [:h2 "Create location"]
   [:div [:a {:href "#"} "<< Home"]]
   [new-location-input]])
