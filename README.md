# Reanimated

Reactive animation (ClojureScript Reagent library)

<img src="https://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
 alt="Reanimated" title="Reanimated" align="right" />


## Overview

* Concise spring animation expressions.
* Local time state, not part of your model.
* Calculates only while animating.
* This then that style timeline helper.


## The approach

React style UIs have many advantages,
but it is not always clear how to animate them.
Existing animation libraries focus on manipulating DOM elements,
which conflicts with the lifecycle that React imposes on them.
Reanimated avoids this problem by making animation part of your UI definition.

Reanimated is my opinionated claim that reaction closures are simpler
and more expressive than React's existing animation capabilities.


## Examples

[Live demos](http://timothypratley.github.io/reanimated/#!/timothypratley.reanimated.examples)
and [example code.](https://github.com/timothypratley/reanimated/blob/master/src/reanimated/examples.cljs)


## Setup

 Add to your project.clj file:

[![Clojars Project](http://clojars.org/reanimated/latest-version.svg)](http://clojars.org/reanimated)


## API Docs

http://timothypratley.github.io/reanimated/codox/index.html
See the examples above for usage.


## How it works

Reactions. You should read about them [here.](https://github.com/Day8/re-frame)
Physical spring simulation.
Animation atom which recalculates until it reaches a steady state.


## Development

Pull requests are welcome.

To see all available commands, run:

    boot -h

To start up a dev server, run:

    boot dev
    open http://localhost:3550

To run a production build of the site, run:

    boot site # generates codox
    open public/index.html

To release a new production build of the site, run:

    boot site # generates codox
    git subtree push --prefix public origin gh-pages

To refresh the docs without rebuilding the site, run:

    boot docs

To build the library, run:

    boot lib

To release a new version of the library, ensure you have the proper credentials, and run:

    boot lib release

## License

Copyright © 2016 Timothy Pratley

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
