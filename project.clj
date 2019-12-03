(defproject tortilla "0.1.0-SNAPSHOT"
  :description "A thin wrapper for accessing Java classes from Clojure"

  :url "https://github.com/emlyn/tortilla"

  :scm {:name "git"
        :url "https://github.com/emlyn/tortilla"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies []

  :test-paths ["test/clj"]

  :repl-options {:init-ns tortilla.wrap-test}

  :deploy-branches ["master"]

  :aliases
  {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
   "clj-kondo" ["with-profile" "+clj-kondo" "run" "-m" "clj-kondo.main"]
   "lint" ["clj-kondo" "--lint" "src:test"]}

  :profiles
  {:provided
   {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :dev
   {:main tortilla.core
    :dependencies [[org.clojure/test.check "0.10.0"]
                   [orchestra "2019.02.06-1"]]
    :injections [(require 'tortilla.specs) ;; loads all instrumented fns
                 (require 'orchestra.spec.test)
                 (orchestra.spec.test/instrument)]
    :java-source-paths ["test/java"]}

   :kaocha
   {:dependencies [[lambdaisland/kaocha "0.0-554"]
                   [lambdaisland/kaocha-cloverage "0.0-41"]]}

   :clj-kondo
   {:dependencies [[clj-kondo "2019.11.23"]]}})
