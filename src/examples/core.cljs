(ns examples.core
  (:require-macros
    [examples.macros :refer [example]]
    [cljs.test :refer [testing is]]
    [devcards.core :refer [defcard deftest defcard-rg start-devcard-ui!]])
  (:require
    [examples.scroll]
    [examples.deprecated]
    [examples.storyboard]
    [reagent.core :as reagent]
    [reagent.ratom :as ratom]
    [reanimated.core :as anim]
    [devcards.core]
    [clojure.string :as string]))

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

(example spring-example
  "Springs follow the value of a Reagent atom, with a transition.
Here we let `size-spring` be a reaction that animates toward the current value held by `size`.
So size-spring produces animated values from the previous size to the current size.
Clicking the button increases `size` by 10, `spring-size` bounces toward the new value."
  (let [size (reagent/atom 100)
        size-spring (anim/spring size)]
    (fn a-spring-example-component []
      [:p
       {:on-click (fn golem-click [e] (swap! size + 10))}
       [:img
        {:width @size-spring
         :src "img/golem2-512.png"}]
       "Click me!!"])))

(defcard-rg integrate-rk4-plot
  "A spring iterates a rk4 intergral of a mass on a spring with dampness and stiffness, which can be passed as options.
That's a fancy way to say they are bouncy, and you can change their bounciness."
  [:div
   [:svg
    [:text {:x 15 :y 15} "mass 10"]
    [:path
     {:stroke "blue"
      :fill "none"
      :d (str
           "M 0 0"
           (string/join
             " "
             (->> [0 0]
                  (iterate (fn [[x v]]
                             (anim/integrate-rk4 50 1 x v {:mass 10
                                                           :stiffness 1
                                                           :damping 1})))
                  (take 100)
                  (map first)
                  (map vector (range))
                  (map (fn [[t x]] (str "L" t " " x))))))}]]
   [:svg
    [:text {:x 15 :y 15} "mass 20"]
    [:path
     {:stroke "blue"
      :fill "none"
      :d (str
           "M 0 0"
           (string/join
             " "
             (->> [0 0]
                  (iterate (fn [[x v]]
                             (anim/integrate-rk4 50 1 x v {:mass 20
                                                           :stiffness 1
                                                           :damping 1})))
                  (take 100)
                  (map first)
                  (map vector (range))
                  (map (fn [[t x]] (str "L" t " " x))))))}]]])

(example spring-example2
  "Spring is the key feature of Reanimated, so here is another example of it."
  (let [x (reagent/atom 150)
        x-spring (anim/spring x)]
    (fn a-spring-example2-component []
      [:div
       [:button {:on-click (fn [e] (swap! x - 50))} "<"]
       [:button {:on-click (fn [e] (swap! x + 50))} ">"]
       [:svg
        [:circle {:r 20 :cx @x-spring :cy 50 :fill "green"}]]])))

(example timeline-example
  "A timeline can contain elements you want to show, times you want to wait, or functions you want to perform."
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

(example interpolate-to-example
  "Sometimes you really don't want bounce. I get it. Don't worry, you can use `interpolate-to` instead.
You can pass a custom interpolater if the linear one is too boring, and springs are too exciting."
  (let [x (reagent/atom 150)
        cx (anim/interpolate-to x)]
    (fn a-spring-example2-component []
      [:div
       [:button {:on-click (fn [e] (swap! x - 50))} "<"]
       [:button {:on-click (fn [e] (swap! x + 50))} ">"]
       [:svg
        [:circle {:r 20 :cx @cx :cy 50 :fill "green"}]]])))

(example css-transition-group-carousel-example
  "To make elements appear and disappear, use a css-transition-group.
  I didn't implement these, they are part of ReactJS.
  But I thought an example might help you decide if you want to use them.
  https://facebook.github.io/react/docs/animation.html"
  (let [pics ["img/full-moon-icon-hi.png"
              "img/golem2-512.png"
              "img/monster_zombie_hand-512.png"]
        img-src (reagent/atom (cycle pics))]
    (fn a-carousel []
      [:div {:style {:height 300}}
       [anim/css-transition-group
        {:transition-name "carousel"
         :transition-enter-timeout 500
         :transition-leave-timeout 500}
        [:img.carousel
         {:key (first @img-src)
          :src (first @img-src)
          :width 300
          :height 300}]]
       [anim/interval #(swap! img-src next) 3000]
       (comment You might want to put these styles in your CSS file)
       [:style
        "img.carousel { position: absolute; }"
        ".carousel-enter { opacity: 0.01; }"
        ".carousel-enter.carousel-enter-active { opacity: 1; transition: opacity 500ms ease-in; }"
        ".carousel-leave { opacity: 1; }"
        ".carousel-leave.carousel-leave-active { opacity: 0.01; transition: opacity 300ms ease-in; }"]])))

(example css-transition-group-todo-example
  "Note that you need elements to have a key so that React can keep track of what's going in and out."
  (let [items (reagent/atom ["milk" "bread" "cheese"])]
    (fn a-todo-list []
      [:div
       [:button {:on-click #(swap! items conj (str "more" (rand-int 1000)))} "add"]
       [anim/css-transition-group
        {:transition-name "todo"
         :transition-enter-timeout 500
         :transition-leave-timeout 500
         :component "ul"
         :class "todo-list"}
        (doall
          (map-indexed
            (fn [idx item]
              [:li
               {:key idx}
               item
               [:button
                {:style {:float "right"}
                 :on-click
                 (fn [e] (swap! items #(vec (concat (take idx %) (drop (inc idx) %)))))}
                "x"]])
            @items))]
       (comment You might want to put these styles in your CSS file)
       [:style
        "ul.todo-list li { background-color: #44ee22; padding: 10px; margin: 1px; width: 80%; border-radius: 15px; list-style: none; }"
        ".todo-enter { opacity: 0.01; }"
        ".todo-enter.todo-enter-active { opacity: 1; transition: opacity 500ms ease-in; }"
        ".todo-leave { opacity: 1; }"
        ".todo-leave.todo-leave-active { opacity: 0.01; transition: opacity 300ms ease-in; }"]])))

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
(example interval-example
  "You can also specify timeouts or intervals that do stuff as dom elements.
I don't think you'll need it, use timeline instead."
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

(deftest interpolate-test
  (is (= 1 (anim/interpolate 1 2 100 -10)))
  (is (= 2 (anim/interpolate 1 2 100 110)))
  (is (= 1.5 (anim/interpolate 1 2 100 50))))

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

(defn on-jsload []
  (start-devcard-ui! {:enable-key-nav true}))
