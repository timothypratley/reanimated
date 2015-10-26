(defproject timothypratley/reanimated "0.1.0"
  :description "Reanimated is an animation library for Reagent (ClojureScript)."
  :url "http://github.com/timothypratley/reanimated"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [reagent "0.5.1"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" :target-path]

  :scm {:name "git"
        :url "https://github.com/timothypratley/reanimated"}

  :profiles
  {:dev
   {:dependencies [[devcards "0.2.0-8"]]
    :plugins [[lein-cljsbuild "1.1.0"]
              [lein-figwheel "0.4.0"]]
    :resource-paths ["resources" "example-resources"]
    :cljsbuild
    {:builds
     {:dev
      {:source-paths ["src" "src-examples"]
       :figwheel {:on-jsload "timothypratley.reanimated.examples/on-js-reload"
                  :devcards true}
       :compiler {:main timothypratley.reanimated.examples
                  :asset-path "js/compiled/out"
                  :output-to "example-resources/public/js/compiled/reanimated.js"
                  :output-dir "example-resources/public/js/compiled/out"
                  :optimizations :none
                  :source-map-timestamp true}}
      :website
      {:source-paths ["src" "src-examples"]
       :compiler {:main timothypratley.reanimated.examples
                  :asset-path "site/out"
                  :output-to "site/reanimated.js"
                  :output-dir "site/out"
                  :devcards true
                  :recompile-dependents true
                  :optimizations :advanced}}}}}}

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888})
