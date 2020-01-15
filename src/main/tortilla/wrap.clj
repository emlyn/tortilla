(ns tortilla.wrap
  (:require [clojure.string :as str])
  (:import [java.lang.reflect Executable Constructor Method Modifier]))

(defn class-methods [^Class klazz]
  (seq (.getMethods klazz)))

(defn class-constructors [^Class klazz]
  (seq (.getConstructors klazz)))

(defn member-varargs? [^Executable member]
  (.isVarArgs member))

(defn member-static? [^Executable member]
  (Modifier/isStatic (.getModifiers member)))

(defn member-class ^Class [^Executable member]
  (.getDeclaringClass member))

(defn vararg-type [^Executable member]
  (when (member-varargs? member)
    (.getComponentType ^Class (last (.getParameterTypes member)))))

(defn class-name [^Class klazz]
  (if (nil? klazz)
    "nil"
    (.getName klazz)))

(defprotocol MemberInfo
  (member-name* [_])
  (member-symbol* [_])
  ;; Minimum number of parameters a member accepts. It could take more if it has varargs
  (parameter-count* [_])
  ;; Possibly infinite (if member has varargs) list of parameter types accepted by member
  (parameter-types* [_])
  (return-type* [_]))

;; Wrap protocol members in functions so we get spec instrumentation
(defn member-name [m]
  (member-name* m))

(defn member-symbol [m]
  (member-symbol* m))

(defn parameter-count [m]
  (parameter-count* m))

(defn parameter-types [m]
  (parameter-types* m))

(defn return-type [m]
  (return-type* m))

(extend-protocol MemberInfo
  Method
  (member-name* [^Method m]
    (.getName m))
  (member-symbol* [^Method m]
    (if (member-static? m)
      (symbol (-> m member-class class-name) (member-name m))
      (symbol (str "." (member-name m)))))
  (parameter-count* [^Method m]
    (cond-> (.getParameterCount m)
      (not (member-static? m)) inc
      (member-varargs? m)      dec))
  (parameter-types* [^Method m]
    (concat (when-not (member-static? m)
              [(member-class m)])
            (cond-> (.getParameterTypes m)
              (member-varargs? m) butlast)
            (when (member-varargs? m)
              (repeat (vararg-type m)))))
  (return-type* [^Method m]
    (.getReturnType m))

  Constructor
  (member-name* [^Constructor c]
    (-> c member-class .getSimpleName))
  (member-symbol* [^Constructor c]
    (-> c member-class class-name (str ".") symbol))
  (parameter-count* [^Constructor c]
    (cond-> (.getParameterCount c)
      (member-varargs? c) dec))
  (parameter-types* [^Constructor c]
    (concat (cond-> (.getParameterTypes c)
              (member-varargs? c) butlast)
            (when (member-varargs? c)
              (repeat (vararg-type c)))))
  (return-type* [^Constructor c]
    (.getDeclaringClass c)))

(defn camel->kebab
  [string]
  (-> string
      (str/replace #"(.)([A-Z][a-z]+)" "$1-$2")
      (str/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (str/lower-case)))

(defn array-class [klz]
  (class (into-array klz [])))

(defn primitive?
  [^Class cls]
  (.isPrimitive cls))

(defn type-symbol
  [t]
  (if (primitive? t)
    (case (class-name t)
      "byte"    'java.lang.Byte/TYPE
      "short"   'java.lang.Short/TYPE
      "int"     'java.lang.Integer/TYPE
      "long"    'java.lang.Long/TYPE
      "float"   'java.lang.Float/TYPE
      "double"  'java.lang.Double/TYPE
      "char"    'java.lang.Character/TYPE
      "boolean" 'java.lang.Boolean/TYPE
      (throw (IllegalArgumentException. (str "Unrecognised type: " t))))
    (-> t class-name symbol)))

(defn ensure-boxed [t]
  (let [sym (symbol (class-name t))]
    (get '{byte java.lang.Byte
           short java.lang.Short
           int java.lang.Integer
           long java.lang.Long
           float java.lang.Float
           double java.lang.Double
           char java.lang.Character
           boolean java.lang.Boolean
           void java.lang.Object}
         sym sym)))

(defn ensure-boxed-long-double
  "Allow long and double, box everything else."
  [t]
  (let [sym (symbol (class-name t))]
    (get '{byte java.lang.Byte
           short java.lang.Short
           int java.lang.Integer
           float java.lang.Float
           char java.lang.Character
           boolean java.lang.Boolean
           void java.lang.Object}
         sym sym)))

(defn tagged [value tag]
  (vary-meta value assoc :tag (ensure-boxed-long-double tag)))

(defn tagged-local [value tag]
  (let [tag (ensure-boxed-long-double tag)]
    (cond
      (= 'long tag)
      `(long ~value)

      (= 'double tag)
      `(double ~value)

      :else
      (vary-meta value assoc :tag tag))))

(defn compatible-type?
  [^Class typ val]
  (or (instance? (resolve (ensure-boxed typ)) val)
      (and (nil? val)
           (not (primitive? typ)))))

(defn type-error
  [name & args]
  (throw (IllegalArgumentException.
          ^String
          (str "Unrecognised types for " name ": "
               (str/join ", " (map (comp class-name type) args))))))

(defn args-compatible
  ([id args arg-types]
   (reduce (fn [r [arg typ]]
             (if (compatible-type? typ arg)
               r
               (reduced nil)))
           (into [id] args)
           (map vector
                args
                (concat arg-types (repeat (last arg-types))))))
  ([id args arg-types coercer]
   (reduce (fn [r [arg typ]]
             (let [coerced (coercer arg (resolve (ensure-boxed typ)))]
               (if (compatible-type? typ coerced)
                 (conj r coerced)
                 (reduced nil))))
           [id]
           (map vector
                args
                (concat arg-types (repeat (last arg-types)))))))

(defn select-overload
  [args matches]
  (or (first (remove nil? matches))
      (into [-1] args)))

(defn member-invocation
  [member args & [more-arg]]
  `(~(member-symbol member)
    ~@(map (fn [sym ^Class klz]
             (tagged-local sym klz))
           (take (parameter-count member) args)
           (parameter-types member))
    ~@(when (member-varargs? member)
        [(tagged-local `(into-array ~(vararg-type member)
                                    ~(if more-arg
                                       `(into ~(subvec args (parameter-count member))
                                              ~more-arg)
                                       (subvec args (parameter-count member))))
                       (array-class (vararg-type member)))])))

;; Generate form for one arity of a member
(defn ^:no-gen arity-wrapper-form [arity uniadics variadics {:keys [coerce]}]
  (let [arg-vec (mapv #(gensym (str "p" % "_")) (range arity))
        arg-sym (gensym "args_")
        members (concat uniadics variadics)
        ret     (if (apply = (map return-type members))
                  (return-type (first members))
                  java.lang.Object)]
    `(~(tagged `[~@arg-vec] ret)
      ~(if-let [mem (and (= 1 (count members))
                         (first members))]
         (if (zero? arity)
           (member-invocation mem [])
           `(if-let [[~'_ ~@arg-vec] (args-compatible
                                      0 ~arg-vec
                                      ~(->> mem parameter-types (take arity) (mapv type-symbol))
                                      ~@(when coerce [coerce]))]
              ~(member-invocation mem arg-vec)
              (type-error ~(str (-> mem member-class class-name) \.
                                (-> mem member-name))
                          ~@arg-vec)))
         `(let [~arg-sym ~arg-vec
                [id# ~@arg-vec]
                (select-overload
                 ~arg-sym
                 [~@(map-indexed (fn [id member]
                                   (let [arg-types (->> (parameter-types member)
                                                        (take arity)
                                                        (mapv type-symbol))]
                                     `(args-compatible ~id ~arg-sym ~arg-types ~@(when coerce [coerce]))))
                                 members)])]
            (case (long id#)
              ~@(mapcat (fn [id mem]
                          [id (member-invocation mem arg-vec)])
                        (range)
                        members)
              (type-error ~(str (-> members first member-class class-name) \.
                                (-> members first member-name))
                          ~@arg-vec)))))))

;; Generate form for the highest/variadic arity of a member
(defn ^:no-gen variadic-wrapper-form [min-arity members {:keys [coerce]}]
  (let [fix-args (mapv #(gensym (str "p" % "_")) (range min-arity))
        more-arg (gensym "more_")
        arg-sym (gensym "args_")
        arg-vec (conj fix-args '& more-arg)
        ret (if (apply = (map return-type members))
              (return-type (first members))
              java.lang.Object)]
    `(~(tagged `[~@arg-vec] ret)
      ~(if-let [mem (and (= 1 (count members))
                         (first members))]
         `(if-let [[~'_ ~@arg-vec] (args-compatible
                                    0 (into ~fix-args ~more-arg)
                                    ~(->> mem parameter-types (take (inc min-arity)) (mapv type-symbol))
                                    ~@(when coerce [coerce]))]
            ~(member-invocation mem fix-args more-arg)
            (apply type-error ~(str (-> mem member-class class-name) \.
                                    (-> mem member-name))
                   ~@fix-args
                   ~more-arg))
         `(let [~arg-sym (into ~fix-args ~more-arg)
                [id# ~@arg-vec]
                (select-overload
                 ~arg-sym
                 [~@(map-indexed (fn [id member]
                                   (let [arg-types (->> (parameter-types member)
                                                        (take (inc min-arity))
                                                        (mapv type-symbol))]
                                     `(args-compatible ~id ~arg-sym ~arg-types ~@(when coerce [coerce]))))
                                 members)])]
            (case (long id#)
              ~@(mapcat (fn [id mem]
                          [id (member-invocation mem fix-args more-arg)])
                        (range)
                        members)
              (apply type-error ~(str (-> members first member-class class-name) \.
                                      (-> members first member-name))
                     ~@fix-args
                     ~more-arg)))))))

;; Generate defn form for all arities of a named member
(defn member-wrapper-form [fname members opts]
  (let [arities (group-by parameter-count members)]
    `(defn ~fname
       {:arglists '~(map (fn [member]
                           (cond-> (vec (take (parameter-count member)
                                              (parameter-types member)))
                             (member-varargs? member) (conj '& [(vararg-type member)])))
                         members)}
       ~@(loop [[[arity membs] & more] (sort arities)
                variadics []
                results []
                last-arity -1]
           (if (nil? arity)
             ;; no more members so return, generating variadic form if necessary
             (if (seq variadics)
               (conj results (variadic-wrapper-form last-arity variadics opts))
               results)
             ;; there are more members, so continue
             (if (and (> arity (inc last-arity))
                      (seq variadics))
               ;; arity increase > 1, and we have varargs, so generate intermediate arity form
               (recur [[arity membs] more]
                      variadics
                      (conj results (arity-wrapper-form (inc last-arity) [] variadics opts))
                      (inc last-arity))
               ;; else generate form for the next set of members
               (let [{vararg true fixarg false} (group-by member-varargs? membs)
                     variadics (into variadics vararg)]
                 (recur more
                        variadics
                        (conj results (arity-wrapper-form arity fixarg variadics opts))
                        (long arity)))))))))

(defn compile-time-fn
  [fun]
  (let [fun (if (symbol? fun)
              (resolve fun)
              fun)
        fun (if (var? fun)
              (if (bound? fun)
                @fun
                (throw (IllegalArgumentException.
                        "compile-time-fn: var must be bound at macro-expansion time")))
              fun)]
    (cond
      (nil? fun) (constantly true)
      (fn? fun)  fun
      :else      (throw (IllegalArgumentException.
                         ^String
                         (str "compile-time-fn: expecting a (compile-time) function or nil, got: " fun))))))

(defn class-members
  [klazz {:keys [filter-fn]}]
  (->> klazz
       resolve
       ((juxt class-constructors class-methods))
       (apply concat)
       (remove (set (class-methods Object)))
       (filter (compile-time-fn filter-fn))))

(defmacro defwrapper [klazz {:keys [prefix coerce] :as opts}]
  (let [members (group-by member-name
                          (class-members klazz opts))]
    `(do
       ~@(for [[mname membs] members
               :let [fname (symbol (str prefix (camel->kebab mname)))]]
           (member-wrapper-form fname membs (assoc opts :coerce (if (symbol? coerce)
                                                                  (resolve coerce)
                                                                  coerce)))))))

(defn- defwrapperfn
  "Wrap macro in a function so it gets picked up by the automatic spec test.check generation"
  [cls]
  (macroexpand-1 `(defwrapper ~cls {})))

(def just-for-testing
  ;; prevent linter complaining about unused private var
  [defwrapperfn])
