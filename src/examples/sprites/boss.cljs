(ns examples.sprites.boss
  (:require-macros
    [devcards.core :refer [defcard-rg]])
  (:require
    [datafrisk.core :as datafrisk]
    [reagent.core :as reagent]))

(defn boss [{:keys [x y scale arm]}]
  [:g
   {:transform (str (when x (str "translate(" x " " y ")"))
                    (when scale (str " scale(" scale ")")))}
   ;; head
   [:circle {:r 20 :fill "none" :stroke "black"}]
   ;; eyes
   [:circle {:cx -10 :r 2 :fill "none" :stroke "black" :stroke-width 0.5}]
   ;; eyeballs
   [:circle {:cx -10 :r 1}]
   ;; mouth
   [:path {:d "M-17,11 Q-11,11 -4,10"
           :stroke "black" :fill "none"}]
   ;; neck
   [:path {:d "M-2,20 L-2,25" :stroke "black"}]
   [:path {:d "M2,20 L2,25" :stroke "black"}]
   ;; body
   [:circle {:cy 45 :r 20}]
   [:rect {:x -20 :y 45 :width 40 :height 40}]
   ;; arm
   [:g {:transform (str "translate(0,40) rotate(" arm ")")}
    [:rect {:width 3 :height 40}]
    [:rect {:x -7 :y 40 :width 25 :height 14 :fill "white" :stroke "black"}]
    [:g {:transform "translate(0,53) rotate(-90)"}
     [:text {:font-size 2
             :style {:font-family "courier"}}
      [:tspan "(map inc foo)"]
      [:tspan {:x 0 :dy 3} "(hahaha)"]]]]])

(defcard-rg boss-card
  (let [params (reagent/atom {:x 0
                              :y 0
                              :scale 1
                              :arm 0})]
    (fn []
      [:div
       [datafrisk/FriskInline params]
       [:svg
        {:view-box "-50 -50 100 100"
         :style {:border "solid 1px"}}
        [boss @params]]])))