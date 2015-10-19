# Reanimated

An animation library for Reagent (ClojureScript)

<img src="https://raw.githubusercontent.com/timothypratley/reanimated/master/resources/public/img/monster_zombie_hand-512.png"
 alt="Zombie hand" title="Zombie hand" align="right" />

## Overview

* Reactive value interpolation.
* Local time state, not part of your model.
* Only calculates while animating.
* Concise expressions.

React style UIs have many advantages, but it is not always clear how to animate them.
Existing animation libraries focus on manipulating DOM elements,
which conflicts with the lifecycle that React imposes on them.
Reanimated avoids this problem by making animation part of your UI definition.


## Setup

 Add to your project.clj file:

    [timothypratley/reanimated "0.1.0-SNAPSHOT"]


## Usage

* Wrap components in `pop-when` to animate their creation/destruction.
* Use `interpolate-if` to create a ratom that animates between two values.
* Wrap a component with `interpolate-arg` and it will interpolate to the argument value.
* To trigger events from ratoms, use `watch`.

```Clojure
(ns my-ns
  (:require [timothypratley.reanimated :refer [pop-when interpolate-if interpolate-arg toggle-handler]]))

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

(defn interpolate-if-example []
  (let [selected? (reagent/atom false)
        radius (interpolate-if selected? 40 20)]
    (fn an-interpolate-if-example []
      [:div
       [:button {:on-click (toggle-handler selected?)} "Pop!"]
       [:svg [:circle {:r @radius :cx 40 :cy 40 :fill "blue"}]]])))

(defn interpolate-arg-example []
  (let [selected? (reagent/atom false)]
    (fn an-interpolate-arg-example []
      [:div
       [:button {:on-click (toggle-handler selected?)} "Pop!"]
       [interpolate-arg circle (if @selected? 40 20)]])))
```

See examples in core.cljs


## Development

    lein figwheel
    open http://localhost:3449

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min


## License

Copyright © 2015 Timothy Pratley

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
