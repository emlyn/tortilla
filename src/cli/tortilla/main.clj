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
            [tortilla.spec]
            [trptcolin.versioneer.core :as version])
  (:gen-class))

(defn parse-coords
  [coord-str]
  (if (re-matches #"\[.*\]" coord-str)
    (edn/read-string coord-str)
    (let [parts (str/split coord-str #":")]
      (vector (symbol (str/join "/" (butlast parts)))
              (last parts)))))

(def cli-options
  [["-c" "--class CLASS"
    :id :classes
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
    :parse-fn #(when-not (empty? %) (symbol %))
    :default nil
    :default-desc ""]

   [nil "--[no-]refer-clojure"
    :desc "Generate refer-clojure clause excluding any wrapped names."
    :default true]

   [nil "--coerce SYMBOL"
    :desc "Use SYMBOL for coercion (or 'none' to disable, empty for default)."
    :parse-fn #(case %
                 "" nil
                 "none" :none
                 ":none" :none
                 (symbol %))
    :default nil
    :default-desc ""]

   ["-p" "--prefix PREFIX"
    :desc "Prefix generated function names (useful to avoid conflicts with clojure.core names.)"
    :default ""]

   ["-o" "--out FILE"
    :desc "Write generated output to FILE."]

   [nil "--[no-]metadata"
    :desc "Include metadata in output."
    :default true]

   [nil "--[no-]instrument"
    :desc "Instrument specs."
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

   ["-v" "--version"
    :desc "Display version information."]

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

      (:version options)
      {:exit 0
       :message (format "Tortilla version %s (%s)"
                        (version/get-version "emlyn" "tortilla" "unknown")
                        (subs (version/get-revision "emlyn" "tortilla" "unknown") 0 7))}

      (empty? (:classes options))
      {:exit 1
       :message (message summary "Must supply at least one class to wrap")}

      :else
      options)))

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
  [{:keys [classes namespace coerce refer-clojure filter-fn prefix]}]
  (let [core-symbols (-> 'clojure.core ns-publics keys set)
        exclusions (->> classes
                        (mapcat #(w/class-members % {:filter-fn filter-fn}))
                        (map #(w/function-sym prefix %))
                        (filter core-symbols)
                        distinct
                        sort
                        seq)]
    `(~'ns ~namespace
           ~@(when (and refer-clojure exclusions)
               `[(:refer-clojure :exclude [~@exclusions])])
           (:require [tortilla.wrap]
                     ~@(some-> (w/resolve-coercer coerce)
                               clojure.core/namespace
                               symbol
                               vector
                               vector)))))

(defmacro with-filter
  [[include exclude] & body]
  `(binding [*filter-in* ~include
             *filter-out* ~exclude
             s/*explain-out* (expound/custom-printer {:show-valid-values? true})]
     (do ~@body)))

(defn class-form
  [cls {:keys [include exclude coerce prefix]}]
  (with-filter [include exclude]
    (macroexpand-1 `(defwrapper ~cls {:coerce ~coerce
                                      :prefix ~prefix
                                      :filter-fn filter-fn}))))

(defn print-class-form
  [cls {:keys [include exclude members unwrap-do metadata width] :as options}]
  (println "\n;; ====" cls "====")
  (if members
    (with-filter [include exclude]
      (->> (w/class-members cls {:filter-fn filter-fn})
           (map member-str)
           (sort)
           (map println)
           (doall)))
    (let [form (class-form cls options)]
      (if (and unwrap-do
               (seq? form)
               (= 'do (first form)))
        (doseq [fn-form (cond-> (rest form)
                          (= 'nil (last form)) butlast)]
          (println)
          (fipp/pprint fn-form
                       {:print-meta metadata
                        :width width}))
        (fipp/pprint form
                     {:print-meta metadata
                      :width width})))))

(defn print-output
  [options]
  (when (and (:namespace options)
             (not (:members options)))
    (fipp/pprint (ns-form options)))
  (doseq [cls (:classes options)]
    (print-class-form cls options)))

(defn ensure-compiler-loader
  "Ensures the clojure.lang.Compiler/LOADER var is bound to a DynamicClassLoader,
  so that we can add to Clojure's classpath dynamically."
  []
  (when-not (instance? clojure.lang.DynamicClassLoader (clojure.lang.RT/baseLoader))
    (.bindRoot Compiler/LOADER (clojure.lang.RT/makeClassLoader))))

(defn load-deps
  [coords]
  (if coords
    (do (ensure-compiler-loader)
        (let [loader (clojure.lang.RT/baseLoader)]
          (add-dependencies :coordinates coords
                            :repositories (merge maven-central
                                                 {"clojars" "https://clojars.org/repo"})
                            :classloader loader)
          loader))
    (clojure.lang.RT/baseLoader)))

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
    ;; Now map class names to Class instances. We couldn't do this earlier
    ;; (e.g. in a cli parse-fn) because they might come from a dynamically
    ;; loaded dependency.
    (let [loader (load-deps (:dep options))
          options (update options :classes
                          (partial mapv #(try (.loadClass ^ClassLoader loader %)
                                              (catch ClassNotFoundException _
                                                (exit 1 (str "Invalid class: " %))))))]
      (if-let [out-file (not-empty (:out options))]
        (do (io/make-parents out-file)
            (spit out-file (with-out-str (print-output options))))
        (print-output options)))))
