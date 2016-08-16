(ns examples.storyboard
  (:require-macros
    [examples.macros :refer [example]]
    [cljs.test :refer [testing is]]
    [devcards.core :refer [defcard deftest defcard-rg start-devcard-ui!]])
  (:require
    [examples.sprites.young-tim :refer [young-tim]]
    [examples.sprites.boss :refer [boss]]
    [reagent.core :as reagent]
    [datafrisk.core :as datafrisk]
    [reanimated.core :as anim]
    [devcards.core]))

(defn caption [s]
  [:text {:y -40 :font-size 6 :text-anchor "middle"} s])

(defn scene-1 []
  (let [scale (reagent/atom 1)
        scale-spring (anim/spring scale)
        x (reagent/atom 0)
        x-spring (anim/spring x)
        arm (reagent/atom 0)
        arm-spring (anim/spring arm)
        blink (reagent/atom 1)
        blink-to (anim/interpolate-to blink {:duration 50})
        eye-ball-pop (reagent/atom 0)
        eye-ball-pop-spring (anim/spring eye-ball-pop)]
    (fn a-scene-1 []
      [:svg
       {:view-box "-50 -50 100 100"
        :style {:border "solid 1px"}}
       [young-tim {:x @x-spring :y 0 :scale @scale-spring :blink @blink-to :eye-ball-pop @eye-ball-pop-spring :smile 2}]
       [boss {:x (+ 100 (* 3 @x-spring)) :y -10 :scale @scale-spring :arm @arm-spring}]
       [anim/timeline
        1000
        [caption "I was coding away one day"]
        #(reset! blink 0)
        100
        #(reset! blink 1)
        100
        #(reset! blink 0)
        100
        #(reset! blink 1)
        1100
        #(reset! scale 0.5)
        #(reset! x -25)
        [caption "When my boss turned up at my desk with a printout"]
        1000
        [caption "'What do you think of this code?'"]
        1000
        #(reset! arm 120)
        1000
        #(reset! eye-ball-pop 2)
        1000
        #(reset! x -20)
        #(reset! scale 0.8)
        [:text {:x -10 :y -20} "???"]
        1000
        [caption "It was code"]
        1000
        [caption "I didn't understand it"]
        1000
        [caption "It was so concise"]
        1000
        [caption "What did it mean?"]
        1000]])))

(defcard-rg scene-1-card
  [scene-1])

(defn scene-2 []
  (let [smile (reagent/atom 0)
        smile-spring (anim/spring smile)
        blink (reagent/atom 1)
        blink-to (anim/interpolate-to blink {:duration 50})]
    (fn a-scene-2 []
      [:svg
       {:view-box "-50 -50 100 100"
        :style {:border "solid 1px"}}
       [young-tim {:smile @smile-spring :blink @blink-to}]
       [anim/timeline
        [caption "It was lisp"]
        1000
        [caption ""]
        #(reset! blink 0)
        100
        #(reset! blink 1)
        100
        1000
        #(reset! smile -1)
        1000
        #(reset! smile -2)
        1000
        #(reset! blink 0)
        100
        #(reset! blink 1)
        100
        1000]])))

(defcard-rg scene-2-card
  [scene-2])

(defn story []
  [anim/timeline
   [scene-1]
   15000
   [scene-2]
   15000])

(defcard-rg story-card
  [story])