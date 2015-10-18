(defproject timothypratley/reanimated "0.1.0-SNAPSHOT"
  :description "Animation library for Reagent (ClojureScript)"
  :url "http://github.com/timothypratley/reanimated"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [devcards "0.2.0-1"]
                 [reagent "0.5.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src"]

             :figwheel {:on-jsload "timothypratley.reanimated.example/on-js-reload"
                        :devcards true}

             :compiler {:main timothypratley.reanimated.example
                        :asset-path "js/compiled/out"
                        :output-to "resources/public/js/compiled/reanimated.js"
                        :output-dir "resources/public/js/compiled/out"
                        :source-map-timestamp true }}
            {:id "min"
             :source-paths ["src"]
             :compiler {:output-to "resources/public/js/compiled/reanimated.js"
                        :main timothypratley.reanimated.example
                        :optimizations :advanced
                        :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888})
