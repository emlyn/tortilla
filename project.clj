(defproject tortilla "0.1.0-SNAPSHOT"
  :description "A thin wrapper for accessing Java classes from Clojure"

  :url "https://github.com/emlyn/tortilla"

  :scm {:name "git"
        :url "https://github.com/emlyn/tortilla"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies []

  :source-paths ["src/main"]
  :test-paths ["src/test"]

  :repl-options {:init-ns tortilla.wrap-test}

  :deploy-branches ["master"]

  :aliases
  {"run" ["with-profile" "+cli" "run"]

   "clj-kondo" ["with-profile" "+clj-kondo" "run" "-m" "clj-kondo.main"]
   "lint" ["clj-kondo" "--lint" "src"]

   "kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
   "test" ["kaocha"]}

  :profiles
  {:provided
   {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :cli
   {:dependencies [[org.clojure/tools.cli "0.4.2"]
                   [fipp "0.6.22"]
                   [com.cemerick/pomegranate "1.1.0"]]
    :source-paths ["src/cli"]
    :java-source-paths ["src/java"]
    :main tortilla.main}

   :dev
   [:cli
    {:main tortilla.core
     :dependencies [[org.clojure/test.check "0.10.0"]
                    [orchestra "2019.02.06-1"]]}]

   :clj-kondo
   {:dependencies [[clj-kondo "2019.11.23"]]}

   :kaocha
   {:dependencies [[lambdaisland/kaocha "0.0-554"]
                   [lambdaisland/kaocha-cloverage "0.0-41"]]}})
