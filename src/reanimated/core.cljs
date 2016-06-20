(ns reanimated.core
  "An animation library for Reagent (ClojureScript).
  There is only one concept:
  A reaction that moves toward some target,
  each step triggers another update until it reaches the end state.
  The trigger occurs by touching a local atom a zero timeout,
  which changes the atom in the next Reagent render."
  (:require-macros
   [reagent.ratom :as ratom])
  (:require
   [reagent.core :as reagent]
   [goog.events :as events]
   [goog.events.EventType :as EventType]
   [goog.dom :as dom]))

(defn ^:private now []
  (js/Date.))

(defn ^:private interpolate
  "Calculates a value between a and b relative to t in duration."
  [a b duration t]
  (cond
    (<= t 0) a
    (>= t duration) b
    :else (+ a (/ (* t (- b a)) duration))))

(defn pop-when
  "Wraps a component to animate creation and destruction.
  Takes a condition ratom and a vector or value to be rendered.
  Options can contain duration (milliseconds)
  and easing (a function that takes a b duration t)."
  ([condition then] (pop-when condition then {}))
  ([condition then options]
   (let [anim (reagent/atom {:from condition})]
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
             (reagent/next-tick #(swap! anim update :frame inc))
             [:div
              {:style {:transform (str "scale(" scale ")")
                       :opacity scale}}
              then])
           (when condition then)))))))

(defn toggle-handler
  "Creates an event handler that will toggle a given ratom."
  [ratom]
  (fn a-toggle-handler [e]
    (swap! ratom not)
    e))

(defn mouse-watcher
  "Returns a map suitable for merging with component properties,
  that will keep a given ratom updated with the mouseover status."
  [ratom]
  {:on-mouse-over (fn timeline-mouse-over [e]
                    (reset! ratom true)
                    e)
   :on-mouse-out (fn timeline-mouse-out [e]
                   (reset! ratom false)
                   e)})

;; TODO: pop-cond, animate between many elements. pop-case pop-if
;; TODO: can pop-when be written in terms of interpolate-if

(defn interpolate-if
  "Interpolates between two values when the conditon changes.
  Takes a condition ratom to watch, and 2 vectors or values to render.
  Options can contain duration (in milliseconds)
  and easing (a function of a b duration t)."
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
            (reagent/next-tick #(swap! anim update :frame inc))
            (+ (* a (- 1 scale)) (* b scale)))
          (if @condition b a)))))))

(defn interpolate-to
  "Interpolates toward new values.
  Takes a ratom which stores a numeric value.
  Options can contain duration (in milliseconds)
  and easing (a function of a b duration t)."
  ([x] (interpolate-to x {}))
  ([x options]
   (let [anim (reagent/atom {:from @x
                             :at @x
                             :to @x
                             :start (now)})
         {:keys [duration easing]
          :or {duration 200
               easing interpolate}} options]
     (ratom/reaction
      (when (not= (:to @anim) @x)
        (swap! anim assoc
               :start (now)
               :to @x
               :from (:at @anim)
               :frame 0))
      (let [t (->> @anim :start (- (now)))
            scale (easing 0 1 duration t)
            a (:from @anim)
            b @x]
        (if (< t duration)
          (let [at (+ (* a (- 1 scale)) (* b scale))]
            (reagent/next-tick #(swap! anim assoc
                                           :at at
                                           :frame (inc (:frame @anim))))
            at)
          b))))))

(defn interpolate-arg
  "Interpolates the argument of a component to x.
  Will call the given component with values approaching x.
  Options can contain duration (in milliseconds)
  and easing (a function of a b duration t)."
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
             (reagent/next-tick #(swap! anim update :frame inc))
             (let [i (easing (:from @anim) x duration t)]
               (swap! anim assoc :current i)
               [component i]))
           [component x]))))))

;; TODO: why does passing options as second argument not work?
;; it would look more reagenty [pop-when {:duration 1000} condition then]

(def ^:private mass 10)
(def ^:private stiffness 1)
(def ^:private damping 1)

(defn ^:private evaluate
  "This is where the spring physics formula is applied."
  [x2 dt x v a {:keys [mass stiffness damping]}]
  (let [x (+ x (* v dt))
        v (+ v (* a dt))
        f (- (* stiffness (- x2 x)) (* damping v))
        a (/ f mass)]
    [v a]))

(defn ^:private integrate-rk4
  "Takes an itegration step from numbers x to x2 over time dt,
  with a present velocity v."
  [x2 dt x v options]
  (let [dt2 (* dt 0.5)
        [av aa] (evaluate x2 0.0 x v 0.0 options)
        [bv ba] (evaluate x2 dt2 x av aa options)
        [cv ca] (evaluate x2 dt2 x bv ba options)
        [dv da] (evaluate x2 dt x cv ca options)
        dx (/ (+ av (* 2.0 (+ bv cv)) dv) 6.0)
        dv (/ (+ aa (* 2.0 (+ ba ca)) da) 6.0)]
    [(+ x (* dx dt)) (+ v (* dv dt))]))

(defn ^:private small [x]
  (< -0.1 x 0.1))

(defn spring
  "Useful for wrapping a value in your component to make it springy.
  Returns a reaction that will take values approaching x2,
  updating every time Reagent calls requestAnimationFrame.
  Integrates a physical spring simulation for each step.
  Options can contain:
  from - a value to start from (initial value is used if absent).
  velocity of the mass on the spring (initially 0 if absent).
  mass, stiffness, damping of the spring."
  ([x2] (spring x2 {}))
  ([x2 options]
   (let [{:keys [from velocity]
          :or {from @x2
               velocity 0
               mass 10
               stiffness 1
               damping 1}} options
         anim (reagent/atom {:t (now)
                             :x from
                             :v velocity})]
     (ratom/reaction
      (let [{:keys [x v t]} @anim
            t2 (now)
            dt (min 1 (/ (- t2 t) 10.0))]
        (if (and (small (- x @x2)) (small v))
          @x2
          (let [[x v] (integrate-rk4 @x2 dt x v {:mass mass
                                                 :stiffness stiffness
                                                 :damping damping})]
            (reagent/next-tick #(reset! anim {:t t2
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
  "Call function f period t in milliseconds after being mounted in the DOM,
  only if still mounted."
  [f t]
  (let [id (atom)]
    (reagent/create-class
     {:display-name "timeout"
      :component-did-mount
      (fn timeout-did-mount [this]
        (reset! id (js/setTimeout f t)))
      :component-will-unmount
      (fn timeout-will-unmount [this]
        (js/clearTimeout @id))
      :reagent-render
      (fn timeout-render [])})))

(defn ^:private and-then
  "Use timeline instead of this function directly.
  Provides a way to express a sequence of actions and pauses.
  Takes an id atom, element ratom,
  and a sequence of numbers and/or callback functions and/or vectors.
  Treats numbers as a wait timeout in milliseconds,
  calls callbacks after the elapsed time.
  id is reset as the timeout reference for cleanup.
  element is reset to vectors for rendering. "
  [id element x & more]
  (cond
    (number? x)
    (when (seq more)
      (reset! id (js/setTimeout (fn [] (apply and-then id element more)) x)))

    (vector? x)
    (do (reset! element x)
        (when (seq more)
          (apply and-then id element more)))

    (and (ifn? x) (not (coll? x)) (not (map? x)))
    (do (x)
        (when (seq more)
          (apply and-then id element more)))

    :else
    (apply and-then id element
           [:div
            (str "timeline encountered unexpected type " (type x))
            [:p (pr-str x)]]
           more)))

(defn timeline
  "Given a sequence of inputs, will consume them depending on their type:
  numbers will be a sleep in milliseconds
  functions will be called with no arguments
  vectors will be rendered as reagent components."
  [x & xs]
  (let [id (atom nil)
        element (reagent/atom nil)]
    (reagent/create-class
     {:display-name "interval"
      :component-did-mount
      (fn timeout-did-mount [this]
        (apply and-then id element x xs))
      :component-will-unmount
      (fn timeout-will-unmount [this]
        (when @id
          (js/clearTimeout @id)))
      :reagent-render
      (fn timeout-render []
        @element)})))

(defn get-scroll-y
  "Gets the current document y scroll position."
  []
  (.-y (dom/getDocumentScroll)))

(def get-scroll
  "Gets the current document y scroll position."
  get-scroll-y)

(defn get-scroll-x
  "Gets the current document x scroll position."
  []
  (.-x (dom/getDocumentScroll)))

(def scroll-y
  "A ratom for watching the current document y scroll,
  will be updated when there is a scroll event."
  (reagent/atom (get-scroll-y)))

(def scroll
  "A ratom for watching the current document y scroll,
  will be updated when there is a scroll event."
  scroll-y)

(def scroll-x
  "A ratom for watching the current document x scroll,
  will be updated when there is a scroll event."
  (reagent/atom (get-scroll-x)))

(events/listen
 js/window EventType/SCROLL
 (fn a-scroll [e]
   (reset! scroll-y (get-scroll-y))
   (reset! scroll-x (get-scroll-x))))

;; TODO: still thinking about this
#_(defn scroll
   []
   {:display-name "scroll"
    :component-did-mount
    (fn scroll-did-mount [this]
      (.getDomNode this))})

