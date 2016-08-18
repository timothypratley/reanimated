(ns examples.scroll
  (:require-macros
    [examples.macros :refer [example]]
    [cljs.test :refer [testing is]]
    [devcards.core :refer [defcard deftest defcard-rg start-devcard-ui!]])
  (:require
    [examples.storyboard]
    [reagent.core :as reagent]
    [reanimated.core :as anim]
    [devcards.core]))

(example scroll-example
  "`anim/scroll` is a convenience ratom of the current scroll-y"
  (let [scroll-i (anim/interpolate-to anim/scroll)]
    (fn []
      [:div
       [:div
        {:style {:background (str "linear-gradient(rgb(" (- 255 (quot @scroll-i 10)) ",127,127), darkred)")}}
        [:img
         {:src "img/full-moon-icon-hi.png"
          :style {:width "100px"
                  :position "absolute"
                  ;; TODO: make these relative
                  :left (+ 500.0 (* 300.0 (js/Math.sin (+ (/ js/Math.PI 2.0) (/ @scroll-i 500.0)))))
                  :top (+ 800.0 (* 200.0 (js/Math.cos (+ (/ js/Math.PI 2.0) (/ @scroll-i 500.0)))))}}]
        [:img
         {:src "img/house.png"
          :style {:position "relative"
                  :left "20%"
                  :width (+ 500.0 (/ @scroll-i 10.0))}}]]
       [:div {:style {:height "1000px"}}]])))