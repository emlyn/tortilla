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
  [["-c" "--class CLASS"
    :desc "Class to generate a wrapper. May be specified multiple times."
    :default []
    :parse-fn #(symbol %)
    :assoc-fn (fn [m k v] (update m k conj v))]

   [nil "--[no-]metadata"
    :desc "Include metadata in output."
    :default true]

   [nil "--[no-]instrument"
    :desc "Instrument specs."
    :default true]

   [nil "--[no-]coerce"
    :desc "Include coercion function."
    :default true]

   ["-w" "--width CHARS"
    :desc "Limit output width."
    :default 100
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be positive"]]

   ["-d" "--dep COORD"
    :desc (str "Add jars to classpath. May be specified multiple times. "
               "COORD may be in leiningen format ('[group/artifact \"version\"]') "
               "or maven format (group:artifact:version). "
               "In both cases the group part is optional, and defaults to the artifact ID.")
    :parse-fn parse-coords
    :assoc-fn (fn [m k v] (update m k (fnil conj []) v))]

   ["-h" "--help"]])

(defn message
  [summary & [error]]
  (->> [(when error "Error:")
        error
        (when error "")
        "Usage: tortilla [options]"
        ""
        "Options:"
        summary]
       (remove nil?)
       (str/join \newline)))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit 0
       :message (message summary)}

      errors
      {:exit 1
       :message (message summary (str/join \newline errors))}

      (seq arguments)
      {:exit 1
       :message (message summary "Options must start with a hyphen")}

      (not (seq (:class options)))
      {:exit 1
       :message (message summary "Must supply at least one class to wrap")}

      :else
      options)))

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
    (doseq [cls (:class options)]
      (when-not (instance? Class (resolve cls))
        (println "Invalid class:" cls)
        (System/exit 1)))
    (doseq [cls (:class options)]
      (println "\n;; ====" cls "====")
      (fipp/pprint (if (:coerce options)
                     (macroexpand-1 `(defwrapper ~cls {:coerce coerce}))
                     (macroexpand-1 `(defwrapper ~cls {:coerce nil})))
                   {:print-meta (:metadata options)
                    :width (:width options)}))))
