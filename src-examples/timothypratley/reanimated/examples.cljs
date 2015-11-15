(ns ^:figwheel-always timothypratley.reanimated.examples
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent]
   [reagent.ratom :as ratom]
   [timothypratley.reanimated.core :as anim]
   [cljs.test :as t :include-macros true :refer-macros [testing is]]
   [devcards.core :as dc :refer-macros [defcard deftest defcard-rg]]))

(enable-console-print!)

(defn logo-component []
  (let [tilt (reagent/atom 0)
        rotation (anim/spring tilt)
        flip (reagent/atom 90)
        scale (anim/spring flip)
        size (reagent/atom 0)
        width (anim/spring size)]
    (fn a-logo-component []
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
  [logo-component])

(defn spring-example-component []
  (let [size (reagent/atom 100)
        size-spring (anim/spring size)]
    (fn a-spring-example-component []
      [:img
       {:width @size-spring
        :src "img/golem2-512.png"
        :on-click (fn golem-click [e]
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

(defn interpolate-arg-example-component []
  (let [selected? (reagent/atom false)]
    (fn an-interpolate-arg-example-component []
      [:div
       [:button {:on-click (anim/toggle-handler selected?)} "Pop!"]
       [anim/interpolate-arg circle (if @selected? 40 20)]])))

(defcard-rg interpolate-arg-example
  [interpolate-arg-example-component])

(defcard-rg integrate-rk4-plot
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

(defn spring-example2-component []
  (let [x (reagent/atom 150)
        cx (anim/spring x)]
    (fn a-spring-example2-component []
      [:div
       [:button {:on-click (fn [e] (swap! x - 50))} "<"]
       [:button {:on-click (fn [e] (swap! x + 50))} ">"]
       [:svg [:circle {:r 20 :cx @cx :cy 50 :fill "green"}]]])))

(defcard-rg spring-example2
  [spring-example-component])

(def interval-script
  ["`interval` is a component."
   "When the component is mounted into the DOM,"
   "it activates a callback."
   "If the component is removed from the DOM."
   "the callback will be deactivated."
   "`interval` is useful for conditionally animating."
   "Mouse over this text to pause the script."
   "The `interval` is only present when mouse-over is true."
   "Pass `interval` a function and a timeout in milliseconds;"
   "just like JavaScript."
   "The `timeout` component is like `interval` but only fires once."
   "Pass a callback function and time in milliseconds;"
   "just like JavaScript."
   "`timeout` will start when mounted in the DOM,"
   "and cancel if unmounted from the DOM before the time expires."])

(defn prepend [xs x]
  (concat [x] xs))

;; TODO: This would be more compelling as a scrolling text area
(defn interval-example-component []
  (let [lines (reagent/atom (cycle interval-script))
        mouse-over (reagent/atom false)]
    (fn an-interval-example-component []
      [:div (anim/mouse-watcher mouse-over)
       (if @mouse-over
         [:strong "Paused"
          [anim/timeout #(swap! lines prepend "Timeout occurred.") 1000]]
         [:strong "Playing"
          [anim/interval #(swap! lines rest) 2000]])
       [:p (first @lines)]])))

(defcard-rg interval-example
  [interval-example-component])

(deftest interpolate-test
  (is (= 1 (anim/interpolate 1 2 100 -10)))
  (is (= 2 (anim/interpolate 1 2 100 110)))
  (is (= 1.5 (anim/interpolate 1 2 100 50))))

(defn timeline-component []
  (let [mouse-over (reagent/atom false)]
    (fn a-timeline-example []
      [:div (anim/mouse-watcher mouse-over)
       (if @mouse-over
         [:div
          [:strong "Playing timeline..."]
          [anim/timeline
           [:p "`timeline` is for this then that style animation."]
           3000
           [:p "`timeline` takes a mix of vectors, numbers and functions"]
           3000
           [:p "vectors are treated a reagent component to render"]
           2000
           [:p "numbers are treated as wait times in milliseconds"]
           2000
           [:p "functions are treated as callbacks"]
           3000
           #(reset! mouse-over false)]]
         [:strong "Mouse over me!"])])))

(defcard-rg timeline-example
  [timeline-component])

;; TODO: make a convenient way to watch many things
;; perhaps a collection of keys or paths of interesting things
(defn bike-component [bike]
  (let [ba (reagent/atom bike)
        xa (ratom/reaction (:x @ba))
        x (anim/interpolate-to xa {:duration 1000})]
    (fn [{:keys [color y size] :as bike}]
      (reset! ba bike)
      [:g
       {:transform (str "translate(" @x " " y ")")}
       [:circle
        {:r size
         :cx 20 :cy 20
         :fill color}]
       [:circle
        {:r size
         :cx 40 :cy 20
         :fill color}]
       [:path
        {:stroke color
         :fill "none"
         :d "M25 10 L35 10 L40 20 L30 20 L25 10
M20 20 L30 0
M30 20 L40 5"}]])))

(defn one-bike [{:keys [dx] :as bike}]
  (update bike :x (fn [x]
                    (-> (+ x dx)
                        (mod 500)))))

(defn bike-step [bikes]
  (into bikes
        (for [[k v] bikes]
          ;; TODO: make more interesting, bigger steps, spinning wheels
          [k (one-bike v)])))

(defn new-bike []
  {:size (+ 5 (rand-int 3))
   :dx (* (rand-nth [1 -1])
          (+ 5 (rand-int 15)))
   :color (rand-nth ["red" "green" "blue" "gold"])
   :x (rand-int 500)
   :y (rand-int 100)})

(defn react-to-value-example-component []
  (let [app-state (reagent/atom
                   {:bikes (zipmap (repeatedly gensym)
                                   (repeatedly 5 new-bike))})]
    (fn a-react-to-value-example-component []
      [:svg
       {:width 560
        :height 120}
       [anim/interval #(swap! app-state update :bikes bike-step) 1000]
       (for [[k v] (:bikes @app-state)]
         ^{:key k}
         [bike-component v])])))

(defcard-rg react-to-value-example
  [react-to-value-example-component])

(dc/start-devcard-ui!)

(defn on-js-reload [])
