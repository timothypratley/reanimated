(defproject
  reanimated
  "0.4.0"
  :comment
  "Generated from build.boot for Cursive"
  :dependencies
  [[org.clojure/clojure "1.9.0-alpha10"]
   [org.clojure/clojurescript "1.9.89"]
   [org.clojure/core.async "0.2.385"]
   [boot-codox "0.9.5" :scope "test"]
   [pandeiro/boot-http "0.7.3" :scope "test"]
   [adzerk/boot-reload "0.4.12" :scope "test"]
   [adzerk/boot-cljs "1.7.228-1" :scope "test"]
   [adzerk/bootlaces "0.1.13" :scope "test"]
   [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
   [com.cemerick/piggieback "0.2.1" :scope "test"]
   [weasel "0.7.0" :scope "test"]
   [org.clojure/tools.nrepl "0.2.12" :scope "test"]
   [reagent "0.6.0-rc"]
   [devcards "0.2.1-7" [cljsjs/react-dom cljsjs/react]]
   [data-frisk-reagent "0.2.5"]
   [fipp "0.6.6"]]
  :repositories
  [["clojars" {:url "https://clojars.org/repo/"}]
   ["maven-central" {:url "https://repo1.maven.org/maven2"}]]
  :source-paths
  ["src"])
