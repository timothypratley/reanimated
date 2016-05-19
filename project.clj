(defproject timothypratley/reanimated "0.2.0"
  :description "Reanimated is an animation library for Reagent (ClojureScript)."
  :url "http://github.com/timothypratley/reanimated"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [reagent "0.6.0-alpha2"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["js/compiled/out"
                                    "js/compiled/out-dev"
                                    :target-path]

  :scm {:name "git"
        :url "https://github.com/timothypratley/reanimated"}

  :deploy-repositories [["releases" :clojars]]

  :signing {:gpg-key "0A7E8B95"}

  :profiles
  {:dev
   {:dependencies [[devcards "0.2.0-8"]]
    :plugins [[lein-cljsbuild "1.1.0"]
              [lein-figwheel "0.4.0"]
              [lein-codox "0.9.0"]]
    :resource-paths ["resources" "."]
    :codox {:output-path "codox"
            :source-paths ["src"]
            :language :clojurescript}

    :cljsbuild
    {:builds
     {:dev
      {:source-paths ["src" "src-examples"]
       :figwheel {:on-jsload "timothypratley.reanimated.examples/on-js-reload"
                  :devcards true}
       :compiler {:main timothypratley.reanimated.examples
                  :asset-path "js/compiled/out-dev"
                  :output-to "js/compiled/reanimated-dev.js"
                  :output-dir "js/compiled/out-dev"
                  :optimizations :none
                  :source-map-timestamp true}}
      :website
      {:source-paths ["src" "src-examples"]
       :compiler {:main timothypratley.reanimated.examples
                  :asset-path "js/compiled/out"
                  :output-to "js/compiled/reanimated.js"
                  :output-dir "js/compiled/out"
                  :devcards true
                  :recompile-dependents true
                  :optimizations :advanced}}}}}}

  :figwheel {:css-dirs ["public/css"]
             :http-server-root "."
             :nrepl-port 7888})
