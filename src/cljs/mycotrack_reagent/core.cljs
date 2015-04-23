(ns mycotrack-reagent.core
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
(def some_string (atom ""))
(def projects (atom #{}))
(def species-list (atom #{}))
(def new-project (atom {:enabled true}))
(def new-species (atom {}))

;; Init data
(defn refresh-projects []
  (go (let [response (<! (http/get "/api/projects" {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (prn (:status response))
    (prn (= (:status response) 200 ))
    (when (= (:status response) 200 )
      (prn "Projects:")
      (swap! projects union (:body response))
      (prn projects)))))

(defn refresh-species []
  (go (let [response (<! (http/get "/api/species" {:basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (prn (:status response))
    (prn (= (:status response) 200 ))
    (when (= (:status response) 200 )
      (prn "Species:")
      (swap! species-list union (:body response))
      (prn species-list)))))

;; -------------------------
;; Views

(defn atom-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn shared-state []
  (fn []
    [:div
     [:p "The value is now: " @some_string]
     [:p "Change it here: " [atom-input some_string]]]))

(defn project-list []
  (fn [] [:div
   [:ul
    (for [project @projects]
     [:li {:key (:_id project)} (:description project)])]]))

(defn species-list-comp []
  (fn [] [:div (for [species @species-list]
     [:a {:href (str "#/species/" (:_id species))} [:div.image-tile.col-xs-5
      [:p (:commonName species)]
      [:p (:scientificName species)]
      [:div {:key (:_id species)}
       [:img {:src (:imageUrl species)}]]]])]))

(defn save-project [value]
  (fn [] (go (let [response (<! (http/post "/api/projects" {:json-params @new-project :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (prn "Project saved")
      (swap! projects conj (:body response)))))))

(defn save-species [value]
  (fn [] (go (let [response (<! (http/post "/api/species" {:json-params @new-species :basic-auth {:username "test@mycotrack.com" :password "test"}}))]
    (when (= (:status response) 201 )
      (prn "Species saved")
      (swap! species-list conj (:body response)))))))

(defn new-project-input [value]
  (fn [] [:div
   [:input {:type "text"
           :value (:description @value)
           :on-change #(swap! value assoc :description (-> % .-target .-value))}]
   [:input.btn {:type "button" :value "Save"
        :on-click ( save-project value ) }]]))

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


(defn home-page []
  [:div [:h2 "Welcome to mycotrack-reagent"]
   [:div [:a {:href "#/about"} "go to about page"]]
   [:div [:a {:href "#/species"} "go to species page"]]
   [project-list]
   [new-project-input new-project]])

(defn species-page []
  [:div [:h2 "Welcome to the Species Page"]
   [:div [:a {:href "#/"} "go to home page"]
   [new-species-input new-species]]
   [species-list-comp]])

(defn species-detail-page []
  [:div [:h2 "Welcome to the Species Detail Page"]
   [:div [:a {:href "#/"} "go to home page"]]])

(defn about-page []
  [:div [:h2 "About mycotrack-reagent"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/species" []
  (session/put! :current-page #'species-page))

(secretary/defroute "/species/:id" {:as params}
  (session/put! :current-page #'species-detail-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(refresh-projects)
(refresh-species)
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
