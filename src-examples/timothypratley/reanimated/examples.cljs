(ns ^:figwheel-always timothypratley.reanimated.examples
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent]
   [timothypratley.reanimated.core :as anim]
   [cljs.test :as t :include-macros true :refer-macros [testing is]]
   [devcards.core :as dc :refer-macros [defcard deftest defcard-rg]]))

(enable-console-print!)

(defn logo []
  (let [tilt (reagent/atom 0)
        rotation (anim/spring tilt)
        flip (reagent/atom 90)
        scale (anim/spring flip)
        size (reagent/atom 0)
        width (anim/spring size)]
    (fn a-logo []
      [:div
       [anim/timeout #(reset! size 300) 1000]
       [anim/interval #(swap! flip -) 10000]
       [:img
        {:src "img/monster_zombie_hand-512.png"
         :width (str @width "px")
         :style (zipmap [:-ms-transform
                         :-moz-transform
                         :-webkit-transform
                         :transform]
                        (repeat (str "rotate(" @rotation "deg) rotateY(" (+ 90 @scale) "deg)")))
         :on-mouse-over (fn logo-mouseover [e]
                          (reset! tilt 15))
         :on-mouse-out (fn logo-mouseout [e]
                         (reset! tilt 0))}]])))

(defcard-rg about
  "# [Reanimated](https://github.com/timothypratley/reanimated)
   * Reactive animation.
   * Local time state, not part of your model.
   * Calculates only while animating."
  [logo])

(defn spring-example-component []
  (let [size (reagent/atom 100)
        size-spring (anim/spring size)]
    (fn a-spring-example-component []
      [:img
       {:width @size-spring
        :src "img/golem2-512.png"
        :on-click (fn [e]
                    (swap! size + 10))}])))

(defcard-rg spring-example
  "Springs follow the value of a Reagent atom, with a transition.
```Clojure
(defn spring-example-component []
  (let [size (reagent/atom 100)
        size-spring (anim/spring size)]
    (fn a-spring-example-component []
      [:img
       {:width @size-spring
        :src \"img/golem2-512.png\"
        :on-click (fn [e]
                    (swap! size + 10))}])))
```
Wrapping `size` with `anim/spring` returns a reaction `size-spring`,
which produces animated values from the previous size to the current size.

\"Click me!\""
  [spring-example-component])

(defn scroll-example-component []
  (let [scroll-i (anim/interpolate-to anim/scroll)]
    (fn []
      [:div
       {:style {:background (str "linear-gradient(rgb(" (- 255 (quot @scroll-i 10)) ",127,127), darkred)")
                }}
       [:img
        {:src "img/full-moon-icon-hi.png"
         :style {
                 :width "100"
                 :position "absolute"
                 :left (+ 500.0 (* 300.0 (js/Math.sin (+ (/ js/Math.PI 2.0) (/ @scroll-i 500.0)))))
                 :top (+ 200.0 (* 200.0 (js/Math.cos (+ (/ js/Math.PI 2.0) (/ @scroll-i 500.0)))))}}]
       [:img
        {:src "img/house.png"
         :style {:position "relative"
                 :left "20%"
                 :width (+ 500.0 (/ @scroll-i 10.0))}}]])))

(defcard-rg scroll-example
  [scroll-example-component])

(defcard-rg pop-when-example
  (fn a-pop-when-example [show? _]
    [:div
     [:button {:on-click (anim/toggle-handler show?)} "Pop!"]
     [anim/pop-when @show?
      [:div
       {:style {:background-color "yellow"}}
       [:p "Here is a circle."]
       [:svg [:circle {:r 50 :cx 50 :cy 50 :fill "green"}]]]]])
  (reagent/atom true))

(defn interpolate-if-example []
  (let [selected? (reagent/atom false)
        radius (anim/interpolate-if selected? 40 20)]
    (fn an-interpolate-if-example []
      [:div
       [:button {:on-click (anim/toggle-handler selected?)} "Pop!"]
       [:svg [:circle {:r @radius :cx 40 :cy 40 :fill "blue"}]]])))

(defcard-rg interpolate-if-card
  [interpolate-if-example])

(defn circle [radius]
  [:svg [:circle {:r radius :cx 40 :cy 40 :fill "red"}]])

(defn interpolate-arg-example []
  (let [selected? (reagent/atom false)]
    (fn an-interpolate-arg-example []
      [:div
       [:button {:on-click (anim/toggle-handler selected?)} "Pop!"]
       [anim/interpolate-arg circle (if @selected? 40 20)]])))

(defcard-rg interpolate-arg-card
  [interpolate-arg-example])

(defcard-rg integrate-rk4-card
  [:svg
   [:path
    {:stroke "blue"
     :fill "none"
     :d (str
         "M0 0"
         (string/join
          " "
          (->> [0 0]
               (iterate (fn [[v a]] (anim/integrate-rk4 50 1 v a)))
               (take 100)
               (map first)
               (map vector (range))
               (map (fn [[t x]] (str "L" t " " x))))))}]])

(defn spring-example []
  (let [x (reagent/atom 150)
        cx (anim/spring x)]
    (fn a-spring-example []
      [:div
       [:button {:on-click (fn [e] (swap! x - 50))} "<"]
       [:button {:on-click (fn [e] (swap! x + 50))} ">"]
       [:svg [:circle {:r 20 :cx @cx :cy 50 :fill "green"}]]])))

(defcard-rg spring-card
  [spring-example])

(defn timeline-example []
  (let [script ["Once upon a time"
                "in a land far away"
                "interfaces were static and boring"
                "until one day"
                "springs were attached to everything"
                "and through the power of animation"
                "the intefaces came alive"]
        lines (reagent/atom (cycle script))]
    (fn a-timeline-example []
      [:div
       [anim/interval #(swap! lines rest) 2000]
       [:p (first @lines)]])))

(defcard-rg timeline-card
  [timeline-example])

(deftest interpolate-test
  (is (= 1 (anim/interpolate 1 2 100 -10)))
  (is (= 2 (anim/interpolate 1 2 100 110)))
  (is (= 1.5 (anim/interpolate 1 2 100 50))))

(dc/start-devcard-ui!)

(defn on-js-reload [])
