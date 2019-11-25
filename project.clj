(defproject tortilla "0.1.0-SNAPSHOT"
  :description "A thin wrapper for accessing Java classes from Clojure"

  :url "https://github.com/emlyn/tortilla"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies []

  :test-paths ["test/clj"]

  :repl-options {:init-ns tortilla.wrap-test}

  :profiles
  {:provided
   {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :test
   {:java-source-paths ["test/java"]}})
