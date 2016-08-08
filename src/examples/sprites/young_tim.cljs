(ns examples.sprites.young-tim
  (:require-macros
    [devcards.core :refer [defcard-rg]])
  (:require
    [clojure.string :as str]
    [devcards.core]
    [datafrisk.core :as datafrisk]
    [reagent.core :as reagent]))

(defn finger [{:keys [x y]}]
  [:path {:stroke "white" :fill "white"
          :d (str/join
               " "
               ['M (- x 1) (+ y 1)
                'L (- x 1) y
                'Q (- x 1) (- y 1), x (- y 1), (+ x 1) (- y 1), (+ x 1) y,
                'L (+ x 1) (+ y 1)])}])

(defn young-tim [{:keys [x y scale blink eye-ball-pop smile]}]
  [:g
   {:transform (str (when x (str "translate(" x " " y ")"))
                    (when scale (str " scale(" scale ")")))}
   ;; head
   [:circle {:r 20 :fill "none" :stroke "black"}]
   ;; eyes
   [:g {:transform (str (when blink (str "scale(1," blink ")")))}
    [:circle {:cx -6 :r 2 :fill "none" :stroke "black" :stroke-width 0.5}]
    [:circle {:cx 6 :r 2 :fill "none" :stroke "black" :stroke-width 0.5}]
    ;; eyeballs
    [:g {:transform (str (when eye-ball-pop (str "translate(" eye-ball-pop ")")))}
     [:circle {:cx 6 :r 1}]
     [:circle {:cx -6 :r 1}]]]
   ;; mouth
   [:path {:d (str/join
                " "
                ['M -7 10
                 'Q 0 (+ 10 smile), 7 10])
           :stroke "black" :fill "none"}]
   ;; neck
   [:path {:d "M-2,20 L-2,25" :stroke "black"}]
   [:path {:d "M2,20 L2,25" :stroke "black"}]
   ;; body
   [:path {:d (str/join
                " "
                (let [w 60
                      b 45
                      t 25
                      d (- b t)
                      l (/ w -2)
                      r (/ w 2)]
                  ['M l b
                   'Q l t, (+ l d) t
                   'L (- r d) t
                   'Q r t, r b]))}]
   ;; fingers
   [finger {:x -20 :y 44}]
   [finger {:x -17 :y 44}]
   [finger {:x -14 :y 44}]
   [finger {:x 20 :y 44}]
   [finger {:x 17 :y 44}]
   [finger {:x 14 :y 44}]])

(defcard-rg young-tim-card
  (let [params (reagent/atom {:x 0
                              :y 0
                              :scale 1
                              :blink 1
                              :eye-ball-pop 0
                              :smile 1})]
    (fn []
      [:div
       [datafrisk/FriskInline params]
       [:svg
        {:view-box "-50 -50 100 100"
         :style {:border "solid 1px"}}
        [young-tim @params]]])))