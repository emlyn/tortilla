(ns tortilla.main
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [cemerick.pomegranate :refer [add-dependencies]]
            [cemerick.pomegranate.aether :refer [maven-central]]
            [fipp.clojure :as fipp]
            [orchestra.spec.test :as st]
            [tortilla.wrap :refer [defwrapper]]
            [tortilla.spec])
  (:gen-class))

(defprotocol Coercer
  (coerce [val typ]))

(extend-protocol Coercer
  Object
  (coerce [val _] val)

  Number
  (coerce [val typ]
    (condp = typ
      Integer (int val)
      Float   (float val)
      val)))

(defn parse-coords
  [coord-str]
  (if (re-matches #"\[.*\]" coord-str)
    (edn/read-string coord-str)
    (let [parts (str/split coord-str #":")]
      (vector (symbol (str/join "/" (butlast parts)))
              (last parts)))))

(def cli-options
  [["-m" "--[no-]metadata" "Print metadata"
    :default true]

   ["-i" "--[no-]instrument" "Instrument specs"
    :default true]

   ["-c" "--[no-]coerce" "Include coercion function"
    :default true]

   ["-w" "--width CHARS" "Output width in chars"
    :default 100
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be positive"]]

   ["-d" "--dep COORDS" "Add jars to classpath"
    :default []
    :parse-fn parse-coords
    :assoc-fn (fn [m k v] (update m k (fnil conj []) v))]

   #_["-f" "--filter" "Filter method names by regex"]

   ["-h" "--help"]])

(defn usage
  [summary]
  (str/join \newline
            ["Usage: tortilla [options] classes..."
             ""
             "Options:"
             summary
             ""
             "Classes: names of one or more classes for which to generate Clojure wrappers"]))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit 0
       :message (usage summary)}

      errors
      {:exit 1
       :message (str/join \newline
                          ["Error:"
                           errors
                           ""
                           (usage summary)])}

      (zero? (count arguments))
      {:exit 1
       :message (str/join \newline
                          ["Error:"
                           "Must supply at least one class to wrap"
                           ""
                           (usage summary)])}

      :else
      (assoc options
             :classes arguments))))

(defn ensure-compiler-loader
  "Ensures the clojure.lang.Compiler/LOADER var is bound to a DynamicClassLoader,
  so that we can add to Clojure's classpath dynamically."
  []
  (when-not (bound? Compiler/LOADER)
    (.bindRoot Compiler/LOADER (clojure.lang.DynamicClassLoader. (clojure.lang.RT/baseLoader)))))

(defn -main
  [& args]
  (let [options (validate-args args)]
    (when (:exit options)
      (println (:message options))
      (System/exit (:exit options)))
    (when (:instrument options)
      (st/instrument))
    (when-let [dep (:dep options)]
      (println "Adding dependencies to classpath: " dep)
      (ensure-compiler-loader)
      (add-dependencies :coordinates dep
                        :repositories (merge maven-central
                                             {"clojars" "https://clojars.org/repo"})
                        :classloader @clojure.lang.Compiler/LOADER))
    (doseq [cls (:classes options)]
      (println "\n;; ====" cls "====")
      (if (resolve (symbol cls))
        (fipp/pprint (if (:coerce options)
                       (macroexpand-1 `(defwrapper ~(symbol cls) {:coerce coerce}))
                       (macroexpand-1 `(defwrapper ~(symbol cls) {:coerce nil})))
                     {:print-meta (:metadata options)
                      :width (:width options)})
        (println ";; Error loading class")))))
