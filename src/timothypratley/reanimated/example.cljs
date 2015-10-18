(ns ^:figwheel-always timothypratley.reanimated.example
  (:require
   [reagent.core :as reagent]
   [timothypratley.reanimated.core]))

(enable-console-print!)

(defn main-page []
  [:div])

(defn mount-root []
  (reagent/render [main-page] (js/document.getElementById "app")))

(defonce start
  (mount-root))

(defn on-js-reload []
  (mount-root))
