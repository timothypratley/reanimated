(ns timothypratley.reanimated.core
  "An animation library for Reagent (ClojureScript)."
  (:require
   [cljs.test :as t :include-macros true :refer-macros [testing is]]
   [clojure.string :as string]
   [devcards.core :as dc :refer-macros [defcard defcard-rg deftest]]
   [reagent.core :as reagent]
   [reagent.ratom :as ratom]
   [goog.events :as events]
   [goog.events.EventType :as EventType]
   [goog.dom :as dom]))

;; TODO
;;(foo)

(defcard-rg about
  "# Reanmiated
   * Reactive value interpolation.
   * Local time state, not part of your model.
   * Only calculates while animating."
  [:img {:src "img/monster_zombie_hand-512.png"}])

(defn now []
  (js/Date.))

(defn interpolate
  "Calculates a value between a and b relative to t in duration."
  [a b duration t]
  (cond
    (<= t 0) a
    (>= t duration) b
    :else (+ a (/ (* t (- b a)) duration))))

(deftest interpolate-test
  (is (= 1 (interpolate 1 2 100 -10)))
  (is (= 2 (interpolate 1 2 100 110)))
  (is (= 1.5 (interpolate 1 2 100 50))))

;; TODO: interpolate between vectors
;; TODO: curve interpolation

(defn pop-when
  "Wraps a component to animate creation and destruction"
  ([condition then] (pop-when condition then {}))
  ([condition then options]
   (let [anim (reagent/atom {:from (not condition)})]
     (fn a-pop-when [condition then options]
       (when (not= condition (:from @anim))
         (reset! anim {:start (now)
                       :from condition
                       :frame 0}))
       (let [{:keys [duration easing]
              :or {duration 200
                   easing interpolate}} options
             t (->> @anim :start (- (now)))
             scale (easing 0 1 duration t)
             scale (if condition scale (- 1 scale))]
         (if (< t duration)
           (do
             (js/setTimeout #(swap! anim update :frame inc))
             [:div
              {:style {:transform (str "scale(" scale ")")
                       :opacity scale}}
              then])
           (when condition then)))))))

(defn toggle-handler [r]
  (fn a-toggle-handler [e]
    (swap! r not)
    e))

(defcard-rg pop-when-example
  (fn a-pop-when-example [show? _]
    [:div
     [:button {:on-click (toggle-handler show?)} "Pop!"]
     [pop-when @show?
      [:div
       {:style {:background-color "yellow"}}
       [:p "Here is a circle."]
       [:svg [:circle {:r 50 :cx 50 :cy 50 :fill "green"}]]]]])
  (reagent/atom true))

;; TODO: pop-cond, animate between many elements. pop-case pop-if
;; TODO: can pop-when be written in terms of interpolate-if

(defn interpolate-if
  "Interpolates between two values when the conditon changes."
  ([condition a b] (interpolate-if condition a b {}))
  ([condition a b options]
   (let [anim (reagent/atom {:from a})
         {:keys [duration easing]
          :or {duration 200
               easing interpolate}} options]
     (ratom/reaction
      (when (not= @condition (:condition @anim))
        (reset! anim {:start (now)
                      :condition @condition
                      :from a
                      :frame 0}))
      (let [t (->> @anim :start (- (now)))
            scale (easing 0 1 duration t)
            scale (if @condition scale (- 1 scale))]
        (if (< t duration)
          (do
            (js/setTimeout #(swap! anim update :frame inc))
            (+ (* a (- 1 scale)) (* b scale)))
          (if @condition b a)))))))

(defn interpolate-if-example []
  (let [selected? (reagent/atom false)
        radius (interpolate-if selected? 40 20)]
    (fn an-interpolate-if-example []
      [:div
       [:button {:on-click (toggle-handler selected?)} "Pop!"]
       [:svg [:circle {:r @radius :cx 40 :cy 40 :fill "blue"}]]])))

(defcard-rg interpolate-if-card
  [interpolate-if-example])

(defn interpolate-arg
  "Interpolates the argument of a component to x."
  ([component x] (interpolate-arg component x {}))
  ([component x options]
   (let [anim (reagent/atom {:start 0 :to x :frame 0 :current x})]
     (fn an-interpolate-arg [component x options]
       (when (not= x (:to @anim))
         (swap! anim assoc :start (now) :from (:current @anim) :to x :frame 0))
       (let [{:keys [easing duration]
              :or {duration 200
                   easing interpolate}} options
             t (->> @anim :start (- (now)))]
         (if (< t duration)
           (do
             (js/setTimeout #(swap! anim update :frame inc))
             (let [i (easing (:from @anim) x duration t)]
               (swap! anim assoc :current i)
               [component i]))
           [component x]))))))

(defn circle [radius]
  [:svg [:circle {:r radius :cx 40 :cy 40 :fill "red"}]])

(defn interpolate-arg-example []
  (let [selected? (reagent/atom false)]
    (fn an-interpolate-arg-example []
      [:div
       [:button {:on-click (toggle-handler selected?)} "Pop!"]
       [interpolate-arg circle (if @selected? 40 20)]])))

(defcard-rg interpolate-arg-card
  [interpolate-arg-example])

;; TODO: why does passing options as second argument not work?
;; it would look more reagenty [pop-when {:duration 1000} condition then]

(def m 10)
(def k 1)
(def b 1)

(defn evaluate
  [x x2 dt v a]
  (let [x (+ x (* v dt))
        v (+ v (* a dt))
        f (- (* k (- x2 x)) (* b v))
        a (/ f m)]
    [v a]))

(defn integrate-rk4
  [x2 dt x v]
  (let [dt2 (* dt 0.5)
        [av aa] (evaluate x x2 0.0 v 0.0)
        [bv ba] (evaluate x x2 dt2 av aa)
        [cv ca] (evaluate x x2 dt2 bv ba)
        [dv da] (evaluate x x2 dt cv ca)
        dx (/ (+ av (* 2.0 (+ bv cv)) dv) 6.0)
        dv (/ (+ aa (* 2.0 (+ ba ca)) da) 6.0)]
    [(+ x (* dx dt)) (+ v (* dv dt))]))

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
               (iterate (fn [[v a]] (integrate-rk4 50 1 v a)))
               (take 100)
               (map first)
               (map vector (range))
               (map (fn [[t x]] (str "L" t " " x))))))}]])

(defn small [x]
  (< (js/Math.abs x) 0.1))

(defn spring-x
  "Interpolates the argument of a component to x."
  ([x2] (spring-x x2 {}))
  ([x2 options]
   (let [{:keys [from velocity]
          :or {from @x2
               velocity 0}} options
         anim (reagent/atom {:t (now)
                             :x from
                             :v velocity})]
     (ratom/reaction
      (let [{:keys [x v t]} @anim
            t2 (now)
            dt (min 1 (/ (- t2 t) 10.0))]
        (if (and (small (- x @x2)) (small v))
          @x2
          (let [[x v] (integrate-rk4 @x2 dt x v)]
            ;; TODO: limit timeouts to 1
            (js/setTimeout #(reset! anim {:t t2
                                          :x x
                                          :v v}))
            x)))))))

(defn spring-x-example []
  (let [x (reagent/atom 150)
        cx (spring-x x)]
    (fn a-spring-x-example []
      [:div
       [:button {:on-click (fn [e] (swap! x - 50))} "<"]
       [:button {:on-click (fn [e] (swap! x + 50))} ">"]
       [:svg [:circle {:r 20 :cx @cx :cy 50 :fill "green"}]]])))

(defcard-rg spring-x-card
  [spring-x-example])

(defn watch
  "Watch a ref only while mounted in the DOM."
  [r f]
  (let [k (keyword (gensym "watch"))]
    (reagent/create-class
     {:display-name "watch"
      :component-did-mount
      (fn watch-did-mount [this]
        (add-watch r k f))
      :component-will-unmount
      (fn watch-will-unmount [this]
        (remove-watch r k))
      :reagent-render
      (fn watch-render [r f])})))

(defn interval
  "Call function f every period t while mounted in the DOM."
  [f t]
  (let [id (atom)]
    (reagent/create-class
     {:display-name "interval"
      :component-did-mount
      (fn interval-did-mount [this]
        (reset! id (js/setInterval f t)))
      :component-will-unmount
      (fn interval-will-unmount [this]
        (js/clearInterval @id))
      :reagent-render
      (fn interval-render [])})))

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
       [interval #(swap! lines rest) 2000]
       [:p (first @lines)]])))

(defcard-rg timeline-card
  [timeline-example])

(defn timeout
  "Call function f after period t if still mounted in the DOM."
  [f t]
  (let [id (atom)]
    (reagent/create-class
     {:display-name "interval"
      :component-did-mount
      (fn interval-did-mount [this]
        (reset! id (js/setTimeout f t)))
      :component-will-unmount
      (fn interval-will-unmount [this]
        (js/clearInterval @id))
      :reagent-render
      (fn interval-render [])})))

;;; TODO: wip
(defn get-scroll []
  (-> (dom/getDocumentScroll) (.-y)))

(defn watch-scroll-events [r]
  (events/listen js/window EventType/SCROLL
                 (fn a-scroll [e]
                   (js/console.log "HELLO" e)
                   (reset! r (get-scroll)))))
