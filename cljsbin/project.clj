(defproject cljsbin "0.1.0-SNAPSHOT"
  :description "httpbin implemented in ClojureScript"
  :url "http://example.com/FIXME"
  :dependencies [[bidi "2.1.1"]
                 [com.cemerick/piggieback "0.2.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [hiccups "0.3.0"]
                 [macchiato/core "0.1.8"]
                 [macchiato/env "0.0.6"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]
                 [camel-snake-kebab "0.4.0"]
                 [javax.xml.bind/jaxb-api "2.3.1"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-doo "0.1.7"]
            [macchiato/lein-npm "0.6.2"]
            [lein-figwheel "0.5.9"]
            [lein-cljsbuild "1.1.4"]]
  :npm {:dependencies [[source-map-support "0.4.6"]
                       [compression "^1.6.2"]
                       [morgan "^1.8.1"]
                       [passport "^0.3.2"]
                       [passport-http "0.3.0"]
                       [response-time "^2.3.2"]
                       [serve-favicon "^2.4.0"]
                       [body-parser "^1.16.1"]]
        :write-package-json true}
  :source-paths ["src" "target/classes"]
  :clean-targets ["target"]
  :target-path "target"
  :profiles
  {:dev
   {:npm {:package {:main "target/out/cljsbin.js"
                    :scripts {:start "node target/out/cljsbin.js"}}}
    :cljsbuild
    {:builds {:dev
              {:source-paths ["env/dev" "src"]
               :figwheel     true
               :compiler     {:main          cljsbin.app
                              :output-to     "target/out/cljsbin.js"
                              :output-dir    "target/out"
                              :target        :nodejs
                              :optimizations :none
                              :pretty-print  true
                              :source-map    true
                              :source-map-timestamp false}}}}
    :figwheel
    {:http-server-root "public"
     :nrepl-port 7000
     :reload-clj-files {:clj false :cljc true}
     :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
    :source-paths ["env/dev"]
    :repl-options {:init-ns user}}
   :test
   {:cljsbuild
    {:builds
     {:test
      {:source-paths ["env/test" "src" "test"]
       :compiler     {:main cljsbin.app
                      :output-to     "target/test/cljsbin.js"
                      :target        :nodejs
                      :optimizations :none
                      :pretty-print  true
                      :source-map    true}}}}
    :doo {:build "test"}}
   :release
   {:npm {:package {:main "target/release/cljsbin.js"
                    :scripts {:start "node target/release/cljsbin.js"}}}
    :cljsbuild
    {:builds
     {:release
      {:source-paths ["env/prod" "src"]
       :compiler     {:main          cljsbin.app
                      :output-to     "target/release/cljsbin.js"
                      :target        :nodejs
                      :optimizations :simple
                      :pretty-print  false}}}}}}
  :aliases
  {"build" ["do"
            ["clean"]
            ["npm" "install"]
            ["figwheel" "dev"]]
   "package" ["do"
              ["clean"]
              ["npm" "install"]
              ["with-profile" "release" "npm" "init" "-y"]
              ["with-profile" "release" "cljsbuild" "once"]]
   "test" ["do"
           ["npm" "install"]
           ["with-profile" "test" "doo" "node"]]})
