(defproject emlyn/tortilla "0.1.1-SNAPSHOT"
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

   "bin" ["with-profile" "uberjar" "bin"]

   "check" ["with-profile" "+checks" "check"]

   "clj-kondo" ["with-profile" "+clj-kondo" "run" "-m" "clj-kondo.main"]
   "lint" ["clj-kondo" "--lint" "src"]

   "kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
   "test" ["kaocha"]}

  :plugins [[lein-binplus "0.6.6"]]

  :bin {:name "tortilla"
        :bin-path "./bin"}

  :profiles
  {:provided
   {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :checks
   {:global-vars {*warn-on-reflection* true}
    :source-paths ["src/test"]}

   :cli
   {:dependencies [[org.clojure/tools.cli "1.0.194"]
                   [org.clojure/test.check "1.0.0"]
                   [orchestra "2019.02.06-1"]
                   [expound "0.8.4"]
                   [fipp "0.6.23"]
                   [com.cemerick/pomegranate "1.1.0"]
                   [trptcolin/versioneer "0.2.0"]]
    :source-paths ["src/cli"]
    :java-source-paths ["src/java"]
    :main tortilla.main}

   :uberjar
   [:cli
    {:dependencies [[org.clojure/clojure "1.10.1"]]
     :aot :all}]

   :dev
   ;; [:cli] Have to duplicate :cli profile due to technomancy/leiningen#2683
   {:dependencies [[org.clojure/tools.cli "1.0.194"]
                   [org.clojure/test.check "1.0.0"]
                   [orchestra "2019.02.06-1"]
                   [expound "0.8.4"]
                   [fipp "0.6.23"]
                   [com.cemerick/pomegranate "1.1.0"]
                   [trptcolin/versioneer "0.2.0"]]
    :source-paths ["src/cli"]
    :java-source-paths ["src/java"]
    :main tortilla.main}

   :gen
   {:source-paths ["src/gen"]}

   :clj-kondo
   {:dependencies [[clj-kondo "2020.06.12"]]}

   :kaocha
   {:dependencies [[lambdaisland/kaocha "1.0.632"]
                   [lambdaisland/kaocha-cloverage "1.0-45"]]}}

  :deploy-repositories
  [["releases" {:url "https://repo.clojars.org"
                :creds :gpg}]])
