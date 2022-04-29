(defproject emlyn/tortilla "0.1.5"
  :description "A thin wrapper for accessing Java classes from Clojure"

  :url "https://github.com/emlyn/tortilla"

  :scm {:name "git"
        :url "https://github.com/emlyn/tortilla"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies []

  :source-paths ["src/main"]
  :test-paths ["src/test"]

  :javac-options ["-source" "1.8"]

  :target-path "target/%s/"

  :repl-options {:init-ns tortilla.wrap-test}

  :deploy-branches ["main"]

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
   {:dependencies [[org.clojure/clojure "1.11.1"]]}

   :checks
   {:global-vars {*warn-on-reflection* true}
    :source-paths ["src/test"]}

   :cli
   {:dependencies [[org.clojure/tools.cli "1.0.206"]
                   [org.clojure/test.check "1.1.1"]
                   [orchestra "2021.01.01-1"]
                   [expound "0.9.0"]
                   [fipp "0.6.25"]
                   [com.cemerick/pomegranate "1.1.0"]
                   [trptcolin/versioneer "0.2.0"]]
    :source-paths ["src/cli"]
    :java-source-paths ["src/java"]
    :main tortilla.main}

   :uberjar
   ;; duplicate :cli deps to workaround technomancy/leiningen#2683
   {:dependencies [[org.clojure/clojure "1.11.1"]
                   [org.clojure/tools.cli "1.0.206"]
                   [org.clojure/test.check "1.1.1"]
                   [orchestra "2021.01.01-1"]
                   [expound "0.9.0"]
                   [fipp "0.6.25"]
                   [com.cemerick/pomegranate "1.1.0"]
                   [trptcolin/versioneer "0.2.0"]]
    :source-paths ["src/cli"]
    :java-source-paths ["src/java"]
    :main tortilla.main
    :aot [tortilla.main]}

   :dev
   ;; [:cli] Have to duplicate :cli profile due to technomancy/leiningen#2683
   {:dependencies [[org.clojure/tools.cli "1.0.206"]
                   [org.clojure/test.check "1.1.1"]
                   [orchestra "2021.01.01-1"]
                   [expound "0.9.0"]
                   [fipp "0.6.25"]
                   [com.cemerick/pomegranate "1.1.0"]
                   [trptcolin/versioneer "0.2.0"]]
    :source-paths ["src/cli"]
    :java-source-paths ["src/java"]
    :main tortilla.main}

   :gen
   {:source-paths ["src/gen"]}

   :clj-kondo
   {:dependencies [[clj-kondo "2022.04.25"]]}

   :kaocha
   {:dependencies [[lambdaisland/kaocha "1.66.1034"]
                   [lambdaisland/kaocha-cloverage "1.0.75"]]}}

  :deploy-repositories
  {"releases" :clojars})
