(ns mycotrack-reagent.pages.spawn-project-page
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
(def selected-project (atom {}))
(def selected-species (atom ""))
(def current-farm (atom {}))

(def state (atom {:doc {:description "" :substrate "" :container "" :enabled true :species "" :culture "" :count 1} :saved? false}))

(defn reset-all []
  (reset! selected-species "")
  (reset! selected-project {})
  (reset! state {:doc {:description "" :substrate "" :container "" :enabled true :species "" :culture "" :count 1} :saved? false}))

(defn set-value! [id value]
  (swap! state assoc :saved? false)
  (swap! state assoc-in [:doc id] value))

(defn get-value [id]
  (get-in @state [:doc id]))

;; Init data
(defn refresh-project [projectId]
  (go (let [response (<! (http/get (str "/api/projects/" projectId) {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! selected-project (:body response))
      (set-value! :cultureId (:cultureId @selected-project))
      (set-value! :speciesId (:speciesId @selected-project))))))

(defn refresh-farm []
  (go (let [response (<! (http/get "/api/farms" {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (reset! current-farm (:body response))))))

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

(defn save-project []
  (fn [] (go (let [response (<! (http/post (str "/api/projects/" (:_id @selected-project) "/children") {:json-params (:doc @state) :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (reset-all)
      (aset (.-location js/window) "href" "/"))))))

(defn spawn-project-input []
  (fn []
   [:div.col-xs-12
     [:div.col-xs-12
      [:div.col-xs-3 "Species: " (:speciesId @selected-project)]
      [:div.col-xs-3 "Culture: " (:cultureId @selected-project)]]
     [:div.col-xs-12
       [:form
       [text-input :description "Description"]
       [number-input :count "Count"]
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
            :on-click ( save-project ) }]]]]))

(defn spawn-project-page []
  (prn (str "Got a new project" (session/get :current-project)))
  (refresh-project (session/get :current-project))
  (refresh-farm)
  [:div [:h2 "Spawn project"]
   [:div [:a {:href "#"} "<< Home"]]
   [spawn-project-input]])
