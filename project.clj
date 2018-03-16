(defproject
  reanimated
  "0.6.1"
  :comment
  "Generated from build.boot for Cursive"
  :dependencies
  [[org.clojure/clojure "1.9.0"]
   [org.clojure/clojurescript "1.9.908"]
   [boot-codox "0.10.3" :scope "test"]
   [pandeiro/boot-http "0.8.3" :scope "test"]
   [adzerk/boot-reload "0.5.2" :scope "test"]
   [adzerk/boot-cljs "2.1.4" :scope "test"]
   [adzerk/bootlaces "0.1.13" :scope "test"]
   [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
   [com.cemerick/piggieback "0.2.2" :scope "test"]
   [weasel "0.7.0" :scope "test"]
   [org.clojure/tools.nrepl "0.2.13" :scope "test"]
   [reagent "0.7.0"]]
  :repositories
  [["clojars" {:url "https://repo.clojars.org/"}]
   ["maven-central" {:url "https://repo1.maven.org/maven2"}]]
  :source-paths
  ["src"])