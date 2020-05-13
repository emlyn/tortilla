(ns tortilla.main
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [cemerick.pomegranate :refer [add-dependencies]]
            [cemerick.pomegranate.aether :refer [maven-central]]
            [expound.alpha :as expound]
            [fipp.clojure :as fipp]
            [orchestra.spec.test :as st]
            [tortilla.wrap :as w :refer [defwrapper]]
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
    :default-desc ""
    :assoc-fn (fn [m k v] (update m k conj v))]

   ["-m" "--members"
    :desc "Print list of class members instead of wrapper code (useful for checking -i/-x)."]

   ["-i" "--include REGEX"
    :desc "Only wrap members that match REGEX. Match members in format name(arg1.type,arg2.type):return.type"
    :parse-fn re-pattern]

   ["-x" "--exclude REGEX"
    :desc "Exclude members that match REGEX from wrapping."
    :parse-fn re-pattern]

   ["-n" "--namespace NAMESPACE"
    :desc "Generate ns form at start of output with given name."
    :parse-fn #(when (not-empty %)
                 (symbol %))
    :default nil
    :default-desc ""]

   ["-o" "--out FILE"
    :desc "Write generated output to FILE."]

   [nil "--[no-]metadata"
    :desc "Include metadata in output."
    :default true]

   [nil "--[no-]instrument"
    :desc "Instrument specs."
    :default true]

   [nil "--[no-]coerce"
    :desc "Include coercion function."
    :default true]

   [nil "--[no-]unwrap-do"
    :desc "Unwrap 'do' form around defns."
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

   ["-h" "--help"
    :desc "Display this help."]])

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

(defn member-str
  [member]
  (str (:name member)
       \(
       (str/join \, (map #(w/class-name %)
                         (take (cond-> (w/parameter-count member)
                                 (w/member-varargs? member) inc)
                               (w/parameter-types member))))
       (when (w/member-varargs? member) "...")
       \)
       \:
       (w/class-name (:return-type member))))

(def ^:dynamic *filter-in* nil)
(def ^:dynamic *filter-out* nil)

(defn filter-fn
  [member]
  (let [mstr (member-str member)]
    ;; Normally using dynamic vars wouldn't work here,
    ;; as this function would be called at compile time.
    ;; It only works here because we explicitly call macroexpand-1 at run time
    (and (or (nil? *filter-in*)
             (re-find *filter-in* mstr))
         (or (nil? *filter-out*)
             (not (re-find *filter-out* mstr))))))

(defn ns-form
  [{:keys [namespace coerce]}]
  `(~'ns ~namespace
    (:require [tortilla.wrap]
              ~@(when coerce [`[tortilla.main]]))))

(defn print-class-form
  [cls {:keys [include exclude members coerce unwrap-do metadata width]}]
  (println "\n;; ====" cls "====")
  (binding [*filter-in* include
            *filter-out* exclude
            s/*explain-out* (expound/custom-printer {:show-valid-values? true})]
    (if members
      (doseq [member (w/class-members cls {:filter-fn filter-fn})]
        (println (member-str member)))
      (let [form (if coerce
                   (macroexpand-1 `(defwrapper ~cls {:coerce coerce
                                                     :filter-fn filter-fn}))
                   (macroexpand-1 `(defwrapper ~cls {:coerce nil
                                                     :filter-fn filter-fn})))]
        (if (and unwrap-do
                 (seq? form)
                 (= 'do (first form)))
          (doseq [fn-form (rest form)]
            (println)
            (fipp/pprint fn-form
                         {:print-meta metadata
                          :width width}))
          (fipp/pprint form
                       {:print-meta metadata
                        :width width}))))))

(defn print-output
  [options]
  (when (and (:namespace options)
             (not (:members options)))
    (fipp/pprint (ns-form options)))
  (doseq [cls (:class options)]
    (print-class-form cls options)))

(defn load-deps
  [coords]
  (println "Adding dependencies to classpath: " coords)
  (ensure-compiler-loader)
  (add-dependencies :coordinates coords
                    :repositories (merge maven-central
                                         {"clojars" "https://clojars.org/repo"})
                    :classloader @clojure.lang.Compiler/LOADER))

(defn exit
  [code message]
  (when message (println message))
  (System/exit code))

(defn -main
  [& args]
  (let [options (validate-args args)]
    (when-let [code (:exit options)]
      (exit code (:message options)))
    (when (:instrument options)
      (st/instrument))
    (when-let [dep (:dep options)]
      (load-deps dep))
    ;; Now map class names to Class instances. We couldn't do this earlier
    ;; (e.g. in a cli parse-fn) because they might come from a dynamically
    ;; loaded dependency.
    (let [options (update options :class
                          (partial mapv #(or (try (Class/forName %)
                                                  (catch ClassNotFoundException _))
                                             (exit 1 (str "Invalid class: " %)))))]
      (if-let [out-file (not-empty (:out options))]
        (do (io/make-parents out-file)
            (spit out-file (with-out-str (print-output options))))
        (print-output options)))))
