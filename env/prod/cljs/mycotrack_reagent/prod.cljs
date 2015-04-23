(ns mycotrack-reagent.prod
  (:require [mycotrack-reagent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
