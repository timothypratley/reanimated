# Reanimated

An animation library for Reagent (ClojureScript)

<img src="https://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
 alt="Reanimated" title="Reanimated" align="right" />


## Overview

You know how to write HTML.
Reanimated allows you to express value transitions in your HTML.

* Concise expressions.
* Springs.
* Reactive value interpolation.
* Local time state, not part of your model.
* Calculates only while animating.

React style UIs have many advantages, but it is not always clear how to animate them.
Existing animation libraries focus on manipulating DOM elements,
which conflicts with the lifecycle that React imposes on them.
Reanimated avoids this problem by making animation part of your UI definition.


## Examples

[Live demos](http://timothypratley.github.io/reanimated/#!/timothypratley.reanimated.examples)


## Setup

 Add to your project.clj file:

[![Clojars Project](http://clojars.org/timothypratley/reanimated/latest-version.svg)](http://clojars.org/timothypratley/reanimated)


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


## How it works

Reactions. You should read about them [here.](https://github.com/Day8/re-frame)
Physical spring simulation.
Animation atom which recalculates until it reaches a steady state.


## Development

Pull requests are welcome.

When working on src-examples:

    lein figwheel
    open http://localhost:3449/dev.html

To compile the website (keep figwheel running):

    lein cljsbuild once min
    open http://localhost:3449

Merge to gh-pages to release the website.

`lein release` to release the library.


## License

Copyright © 2015 Timothy Pratley

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
