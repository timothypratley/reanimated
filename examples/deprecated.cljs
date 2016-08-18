(ns examples.deprecated
  (:require-macros
    [examples.macros :refer [example]]
    [cljs.test :refer [testing is]]
    [devcards.core :refer [defcard deftest defcard-rg start-devcard-ui!]])
  (:require
    [examples.storyboard]
    [reagent.core :as reagent]
    [reanimated.core :as anim]
    [devcards.core]))

(example pop-when
  "Want to pop ui elements in and out? Use `pop-when`.
  You could have used a scale spring instead though."
  (let [show? (reagent/atom true)]
    (fn a-pop-when-example []
      [:div
       [:button {:on-click (anim/toggle-handler show?)} "Pop!"]
       [anim/pop-when @show?
        [:center [:svg [:circle {:r 50 :cx 50 :cy 50 :fill "green"}]]]]])))

(example interpolate-if-card
  "`interpolate-if` moves between two values based on a flag.
  Not sure why you would want that, just use a spring instead."
  (let [selected? (reagent/atom false)
        radius (anim/interpolate-if selected? 40 20)]
    (fn an-interpolate-if-example []
      [:div
       [:button {:on-click (anim/toggle-handler selected?)} "Pop!"]
       [:svg [:circle {:r @radius :cx 40 :cy 40 :fill "blue"}]]])))

(defn circle [radius]
  [:svg [:circle {:r radius :cx 40 :cy 40 :fill "red"}]])

(example interpolate-arg-example
  "You can define a component that takes the target as an argument with `interpolate-arg`.
  Let me know if you ever use this instead of a spring, I can't imagine why you would want to."
  (let [selected? (reagent/atom false)]
    (fn an-interpolate-arg-example-component []
      [:div
       [:button {:on-click (anim/toggle-handler selected?)} "Pop!"]
       [anim/interpolate-arg circle (if @selected? 40 20)]])))
