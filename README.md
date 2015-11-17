# Reanimated

Animate Reagent reactions (ClojureScript)

<img src="https://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
 alt="Reanimated" title="Reanimated" align="right" />


## Overview

* Concise expressions.
* Springs.
* Reactive value interpolation.
* Local time state, not part of your model.
* Calculates only while animating.


## The approach

React style UIs have many advantages, but it is not always clear how to animate them.
Existing animation libraries focus on manipulating DOM elements,
which conflicts with the lifecycle that React imposes on them.
Reanimated avoids this problem by making animation part of your UI definition.

Reanimated is my opinionated claim that reaction closures are simpler
and more expressive than React's existing animation capabilities.


## Examples

[Live demos and examples](http://timothypratley.github.io/reanimated/#!/timothypratley.reanimated.examples)
and [examples code.](https://github.com/timothypratley/reanimated/blob/master/src-examples/timothypratley/reanimated/examples.cljs)

## Documentation



## Setup

 Add to your project.clj file:

[![Clojars Project](http://clojars.org/timothypratley/reanimated/latest-version.svg)](http://clojars.org/timothypratley/reanimated)


## How it works

Reactions. You should read about them [here.](https://github.com/Day8/re-frame)
Physical spring simulation.
Animation atom which recalculates until it reaches a steady state.


## Development

Pull requests are welcome.

When working on src-examples navigate to dev.html:

    lein figwheel
    open http://localhost:3449/dev.html

To compile the website (keep figwheel running):

    lein cljsbuild once website
    open http://localhost:3449

Merge to gh-pages to release the website.

`lein release` to release the library.


## License

Copyright © 2015 Timothy Pratley

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
