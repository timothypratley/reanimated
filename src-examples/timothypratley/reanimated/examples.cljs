(ns ^:figwheel-always timothypratley.reanimated.examples
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent]
   [timothypratley.reanimated.core :as anim]
   [cljs.test :as t :include-macros true :refer-macros [testing is]]
   [devcards.core :as dc :refer-macros [defcard deftest defcard-rg]]))

(enable-console-print!)

(defn on-js-reload [])

(defcard-rg about
  "# Reanmiated
   * Reactive value interpolation.
   * Local time state, not part of your model.
   * Only calculates while animating.
   https://github.com/timothypratley/reanimated"
  [:img {:src "site/monster_zombie_hand-512.png"}])

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

(defn spring-x-example []
  (let [x (reagent/atom 150)
        cx (anim/spring-x x)]
    (fn a-spring-x-example []
      [:div
       [:button {:on-click (fn [e] (swap! x - 50))} "<"]
       [:button {:on-click (fn [e] (swap! x + 50))} ">"]
       [:svg [:circle {:r 20 :cx @cx :cy 50 :fill "green"}]]])))

(defcard-rg spring-x-card
  [spring-x-example])

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
