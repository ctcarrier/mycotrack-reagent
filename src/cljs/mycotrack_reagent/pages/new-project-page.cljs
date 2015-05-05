(ns mycotrack-reagent.pages.new-project-page
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
(def culture-list (atom []))
(def species-list (atom []))
(def selected-species (atom ""))
(def current-farm (atom {}))

(def state (atom {:doc {:description "" :substrate "" :container "" :enabled true :species "" :culture ""} :saved? false}))

(defn reset-all []
  (reset! culture-list [])
  (reset! species-list [])
  (reset! selected-species "")
  (reset! current-farm {})
  (reset! state {:doc {:description "" :substrate "" :container "" :enabled true :species "" :culture ""} :saved? false}))

(defn set-value! [id value]
  (swap! state assoc :saved? false)
  (swap! state assoc-in [:doc id] value))

(defn get-value [id]
  (get-in @state [:doc id]))

;; Init data
(defn refresh-species []
  (go (let [response (<! (http/get (str "/api/species") {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! species-list (:body response))))))

(defn refresh-cultures [species-id]
  (go (let [response (<! (http/get (str "/api/species/" species-id "/cultures") {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! culture-list (:body response))))))

(defn refresh-farm []
  (go (let [response (<! (http/get "/api/farms" {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! current-farm (:body response))))))

;; -------------------------
;; Triggers
(add-watch selected-species :watcher
  (fn [key atom old-state new-state]
    (set-value! :speciesId new-state)
    (refresh-cultures new-state)))

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

(defn save-project [value]
  (fn [] (go (let [response (<! (http/post "/api/projects" {:json-params (:doc @state) :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (reset-all)
      (aset (.-location js/window) "href" "/"))))))

(defn species-input []
  [:div.form-group
   [:label "Species"]
    [:select {:value @selected-species :key "species-select" :on-change #(reset! selected-species (-> % .-target .-value))}
     [:option {:value "" :key ""} ""]
      (for [species @species-list]
        [:option {:value (:_id species) :key (:_id species)} (:commonName species)])]])

(defn culture-input []
  [:div.form-group
   [:label "Culture"]
    [:select {:value (get-value :cultureId) :key "culture-select" :on-change #(set-value! :cultureId (-> % .-target .-value))}
     [:option {:value "" :key ""} ""]
      (for [culture @culture-list]
        [:option {:value (:_id culture) :key (:_id culture)} (:name culture)])]])

(defn new-project-input [value]
  (fn [] [:form
   [species-input]
   [culture-input]
   [text-input :description "Description"]
   [row "Substrate"
     [:select {:value (get-value :substrate) :key "substrate-select" :on-change #(set-value! :substrate (-> % .-target .-value))}
      [:option {:value "" :key ""} ""]
      (for [substrate (:substrates @current-farm)]
        [:option {:value (:_id substrate) :key (:_id substrate)} (:name substrate)])]]
   [row "Container"
     [:select {:value (get-value :container) :key "container-select" :on-change #(set-value! :container (-> % .-target .-value))}
      [:option {:value "" :key ""} ""]
      (for [container (:containers @current-farm)]
        [:option {:value (:_id container) :key (:_id container)} (:name container)])]]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-project value ) }]]))

(defn new-project-page []
  (refresh-species)
  (refresh-farm)
  [:div [:h2 "Create project"]
   [:div [:a {:href "#"} "<< Home"]]
   [new-project-input]])
