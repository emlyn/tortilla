(ns tortilla.main
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
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

(def cli-options
  [["-m" "--[no-]metadata" "Print metadata"
    :default true]

   ["-i" "--[no-]instrument" "Instrument specs"
    :default true]

   ["-c" "--[no-]coerce" "Include coercion function"
    :default true]

   ["-w" "--width CHARS" "Output width in chars"
    :default 100
    :parse-fn #(Integer/parseInt %)]

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

(defn -main
  [& args]
  (let [options (validate-args args)]
    (when (:exit options)
      (println (:message options))
      (System/exit (:exit options)))
    (when (:instrument options)
      (st/instrument))
    (doseq [cls (:classes options)]
      (println "\n;; ====" cls "====")
      (if (resolve (symbol cls))
        (fipp/pprint (if (:coerce options)
                       (macroexpand-1 `(defwrapper ~(symbol cls) {:coerce coerce}))
                       (macroexpand-1 `(defwrapper ~(symbol cls) {:coerce nil})))
                     {:print-meta (:metadata options)
                      :width (:width options)})
        (println ";; Error loading class")))))
