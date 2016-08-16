(set-env!
 :resource-paths #{"src"}
 :dependencies
 '[;; Clojure
   [org.clojure/clojure "1.9.0-alpha10"]
   [org.clojure/clojurescript "1.9.89"]
   [org.clojure/core.async "0.2.385"]
   ;; Boot commands
   [boot-codox "0.9.5" :scope "test"]
   [pandeiro/boot-http "0.7.3" :scope "test"]
   [adzerk/boot-reload "0.4.12" :scope "test"]
   [adzerk/boot-cljs "1.7.228-1" :scope "test"]
   [adzerk/bootlaces "0.1.13" :scope "test"]
   [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
   ;; boot-cljs-repl deps
   [com.cemerick/piggieback "0.2.1" :scope "test"]
   [weasel "0.7.0" :scope "test"]
   [org.clojure/tools.nrepl "0.2.12" :scope "test"]
   ;; Production Dependencies
   [reagent "0.6.0-rc" :exclusions [cljsjs/react]]
   [cljsjs/react-with-addons "15.3.0-0"]]
 :site-dependencies
 '[[devcards "0.2.1-7" :exclusions [cljsjs/react cljsjs/react-dom]]
   [data-frisk-reagent "0.2.5"]
   [fipp "0.6.6"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.bootlaces :refer [bootlaces!]]
         '[pandeiro.boot-http :refer [serve]]
         '[codox.boot :refer [codox]])
(import java.io.File)

(defn site-env! []
  (merge-env! :dependencies (get-env :site-dependencies)
              :resource-paths #{"resources"}))

(def +target-dir+ "target")
(def +public-dir+ "public")
(def +codox-dir+ "codox")
(def +version+ "0.5.0")
(bootlaces! +version+)


(task-options!
 target {:dir         #{+target-dir+}}
 serve  {:port        3550
         :dir         +target-dir+}
 push   {:repo        "clojars"
 ;; such a bunch of fail, my git says clean but my boot won't push
         :ensure-clea false}
 pom    {:project     'reanimated
         :version     +version+
         :description "Reanimated is an animation library for Reagent (ClojureScript)."
         :url         "http://github.com/timothypratley/reanimated"
         :scm         {:name "git" :url "https://github.com/timothypratley/reanimated"}
         :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 jar    {:main        'reanimated.core}
 codox  {:name        "reanimated"
         :language    :clojurescript
         :output-path +codox-dir+
         :source-paths #{"src/reanimated"}})

(deftask docs
  "Generates codox docs"
  [w write bool "Write the docs to the public dir?"]
  (comp (sift :include [#"reanimated"])
        (codox)
        (target :dir #{+public-dir+}
                :no-clean true)))

(deftask dev
  "Start hot reload dev server."
  [p port VAL int "Port to serve files on"]
  (when port
    (task-options! serve {:port port}))
  (site-env!)
  (comp (serve)
        (watch)
        (reload :on-jsload 'examples.core/on-jsload)
        (cljs-repl)
        (cljs :optimizations    :none
              :compiler-options {:devcards true})
        (target)))

(deftask site
  "Build clojurescript needed for site in production, using advanced compilation."
  []
  (site-env!)
  (comp (cljs :optimizations    :advanced
              :compiler-options {:devcards true})
        (target :dir #{+public-dir+})
        (docs)))

(deftask lib
  "Package library code into a jar, ignoring dev & site code."
  []
  (comp
   (sift :include [#"reanimated"])
   (pom)
   (jar)
   (target)))

(deftask release []
  ;; Push keys need to be handled somehow
  ;; https://github.com/boot-clj/boot/wiki/Repository-Credentials-and-Deploying
  (set-env! :repositories [["clojars" {:url "https://clojars.org/repo/"}]])
  (push))
