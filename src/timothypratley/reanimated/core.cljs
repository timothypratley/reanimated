(ns timothypratley.reanimated.core
  "An animation library for Reagent (ClojureScript)."
  (:require
   [reagent.core :as reagent]
   [reagent.ratom :as ratom]
   [goog.events :as events]
   [goog.events.EventType :as EventType]
   [goog.dom :as dom]))

(defn now []
  (js/Date.))

(defn interpolate
  "Calculates a value between a and b relative to t in duration."
  [a b duration t]
  (cond
    (<= t 0) a
    (>= t duration) b
    :else (+ a (/ (* t (- b a)) duration))))

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
