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

(def state (atom {:doc {:description "" :substrate "" :container "" :enabled true} :saved? false}))

(defn set-value! [id value]
  (swap! state assoc :saved? false)
  (swap! state assoc-in [:doc id] value))

(defn get-value [id]
  (get-in @state [:doc id]))

;; Init data
(defn refresh-species [species-id]
  (go (let [response (<! (http/get (str "/api/species") {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (swap! species-list union (:body response))))))

(defn refresh-cultures [species-id]
  (go (let [response (<! (http/get (str "/api/species/" species-id "/cultures") {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (and (= (:status response) 200 ) (seq (:body response)))
      (swap! culture-list union (:body response))))))

;; -------------------------
;; Views

(defn row [label & body]
  [:div.row
   [:div.col-md-2 [:span label]]
   [:div.col-md-3 body]])

(defn text-input [id label]
  [row label
   [:input
     {:type "text"
       :class "form-control"
       :value (get-value id)
       :on-change #(set-value! id (-> % .-target .-value))}]])

(defn select-input [id label all-options]
  [row label
   [:select
    [:option {:value ""} ""]
    [:option {:value "rye"} "Rye"]
    [:option {:value "sawdust"} "Sawdust"]
    [:option {:value "brewery"} "Brewery Waste"]]])

(defn save-project [value]
  (fn [] (go (let [response (<! (http/post "/api/projects" {:json-params (:doc @state) :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (go (>! echo-chan "projects")))))))

(defn new-project-input [value]
  (fn [] [:div
   [text-input :description "Description"]
   [row "Substrate"
   [:select {:value (get-value :substrate) :on-change #(set-value! :substrate (-> % .-target .-value))}
    [:option {:value ""} ""]
    [:option {:value "rye"} "Rye"]
    [:option {:value "sawdust"} "Sawdust"]
    [:option {:value "brewery"} "Brewery Waste"]]]
   [text-input :container "Container"]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-project value ) }]]))

(defn new-project-page []
  [:div [:h2 "Create project"]
   [:div [:a {:href "#"} "<< Home"]]
   [new-project-input]])
