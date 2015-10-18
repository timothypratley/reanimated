(ns timothypratley.reanimated.core
  "An animation library for Reagent (ClojureScript)."
  (:require
   [cljs.test :as t :include-macros true :refer-macros [testing is]]
   [clojure.string :as string]
   [devcards.core :as dc :refer-macros [defcard deftest]]
   [reagent.core :as reagent]
   [reagent.ratom :as ratom]))

(defn img []
  [:img {:src "img/monster_zombie_hand-512.png"}])

(defcard about
  "# Reanmiated
   * Reactive value interpolation.
   * Local time state, not part of your model.
   * Only calculates while animating. "
  (dc/reagent img))

(defn now []
  (js/Date.))

(defn interpolate
  "Calculates a value between a and b relative to t in period."
  [a b period t]
  (cond
    (<= t 0) a
    (>= t period) b
    :else (+ a (/ (* t (- b a)) period))))

(deftest interpolate-test
  (is (= 1 (interpolate 1 2 100 -10)))
  (is (= 2 (interpolate 1 2 100 110)))
  (is (= 1.5 (interpolate 1 2 100 50))))

;; TODO: interpolate between vectors
;; TODO: curve interpolation

(defn toggle-handler [r]
  (fn a-toggle-handler [e]
    (swap! r not)
    e))

(defn pop-when
  "Wraps a component to animate creation and destruction"
  ([condition then] (pop-when condition then {}))
  ([condition then options]
   (let [anim (reagent/atom {:from (not condition)})]
     (fn a-pop-when [condition then {:keys [period] :or {period 200}}]
       (when (not= condition (:from @anim))
         (reset! anim {:start (now)
                       :from condition
                       :frame 0}))
       (let [t (->> @anim :start (- (now)))
             scale (interpolate 0 1 period t)
             scale (if condition scale (- 1 scale))]
         (if (< t period)
           (do
             (js/setTimeout (fn trigger-next-render []
                              (swap! anim update :frame inc)))
             [:div
              {:style {:transform (str "scale(" scale ")")
                       :opacity scale}}
              then])
           (when condition then)))))))

(defn pop-when-example []
  (let [show? (reagent/atom true)]
    (fn a-pop-when-example []
      [:div
       [:button {:on-click (toggle-handler show?)} "Pop!"]
       [pop-when @show?
        [:div
         {:style {:background-color "yellow"}}
         [:p "Here is a circle."]
         [:svg [:circle {:r 50 :cx 50 :cy 50 :fill "green"}]]]]])))

(defcard pop-when-card
  (dc/reagent pop-when-example))

;; TODO: pop-cond, animate between many elements. pop-case pop-if
;; TODO: can pop-when be written in terms of interpolate-if

(defn interpolate-if
  "Interpolates between two values when the conditon changes."
  ([condition a b] (interpolate-if condition a b {}))
  ([condition a b options]
   (let [anim (reagent/atom {:from a})
         {:keys [period] :or {period 200}} options]
     (ratom/reaction
      (when (not= @condition (:condition @anim))
        (reset! anim {:start (now)
                      :condition @condition
                      :from a
                      :frame 0}))
      (let [t (->> @anim :start (- (now)))
            scale (interpolate 0 1 period t)
            scale (if @condition scale (- 1 scale))]
        (if (< t period)
          (do
            (js/setTimeout (fn trigger-next-render []
                             (swap! anim update :frame inc)))
            (+ (* a (- 1 scale)) (* b scale)))
          (if @condition b a)))))))

(defn interpolate-if-example []
  (let [selected? (reagent/atom false)
        radius (interpolate-if selected? 40 20)]
    (fn an-interpolate-if-example []
      [:div
       [:button {:on-click (toggle-handler selected?)} "Pop!"]
       [:svg [:circle {:r @radius :cx 40 :cy 40 :fill "blue"}]]])))

(defcard interpolate-if-card
  (dc/reagent interpolate-if-example))

(defn interpolate-arg
  "Interpolates the argument of a component to x."
  ([component x] (interpolate-arg component x {}))
  ([component x options]
   (let [anim (reagent/atom {:start 0 :to x :frame 0 :current x})]
     (fn an-interpolate-arg [component x {:keys [period] :or {period 200}}]
       (when (not= x (:to @anim))
         (swap! anim assoc :start (now) :from (:current @anim) :to x :frame 0))
       (let [t (->> @anim :start (- (now)))
             i (interpolate (:from @anim) x period t)]
         (if (< t period)
           (do
             (js/setTimeout (fn trigger-next-render []
                              (swap! anim update :frame inc)))
             (let [i (interpolate (:from @anim) x period t)]
               (swap! anim assoc :current i)
               [component i]))
           (do
             [component x])))))))

(defn circle [radius]
  [:svg [:circle {:r radius :cx 40 :cy 40 :fill "red"}]])

(defn interpolate-arg-example []
  (let [selected? (reagent/atom false)]
    (fn an-interpolate-arg-example []
      [:div
       [:button {:on-click (toggle-handler selected?)} "Pop!"]
       [interpolate-arg circle (if @selected? 40 20)]])))

(defcard interpolate-arg-card
  (dc/reagent interpolate-arg-example))

;; TODO: why does passing options as second argument not work?
;; it would look more reagenty [pop-when {:period 1000} condition then]

(defn watch
  "Watch a ref only while mounted in the DOM."
  [r f]
  (let [k (keyword (gensym "watch"))]
    (reagent/create-class
     {:display-name "watch"
      :component-did-mount
      (fn [this]
        (add-watch r k f))
      :component-will-unmount
      (fn [this]
        (remove-watch r k))
      :reagent-render
      (fn watch-render [r f])})))
