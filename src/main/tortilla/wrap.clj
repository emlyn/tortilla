(ns tortilla.wrap
  (:require [clojure.set :as set]
            [clojure.string :as str])
  (:import [java.lang.reflect Constructor Method Modifier]))

;; General

(defn camel->kebab
  [string]
  (-> string
      (str/replace #"(.)([A-Z][a-z]+)" "$1-$2")
      (str/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (str/lower-case)))

;; Class

(defn class-methods
  [^Class klazz]
  (seq (.getMethods klazz)))

(defn class-constructors
  [^Class klazz]
  (seq (.getConstructors klazz)))

(defn class-name
  [^Class klazz]
  (if (nil? klazz)
    "nil"
    (.getName klazz)))

(defn primitive?
  [^Class klazz]
  (.isPrimitive klazz))

(defn array-class?
  [^Class klazz]
  (.isArray klazz))

(defn array-of
  [klazz]
  (class (into-array klazz [])))

(defn array-component
  [^Class klazz]
  (.getComponentType klazz))

(defn class-repr
  [klazz]
  (if (array-class? klazz)
    `(array-of ~(class-repr (array-component klazz)))
    (let [name (class-name klazz)]
      (if (primitive? klazz)
        (case name
          "byte"    'java.lang.Byte/TYPE
          "short"   'java.lang.Short/TYPE
          "int"     'java.lang.Integer/TYPE
          "long"    'java.lang.Long/TYPE
          "float"   'java.lang.Float/TYPE
          "double"  'java.lang.Double/TYPE
          "char"    'java.lang.Character/TYPE
          "boolean" 'java.lang.Boolean/TYPE
          "void"    'java.lang.Void/TYPE
          (throw (IllegalArgumentException. (str "Unrecognised primitive type: " name))))
        (symbol name)))))

(defn ensure-boxed
  [klazz]
  (get {Byte/TYPE      Byte
        Short/TYPE     Short
        Integer/TYPE   Integer
        Long/TYPE      Long
        Float/TYPE     Float
        Double/TYPE    Double
        Character/TYPE Character
        Boolean/TYPE   Boolean
        Void/TYPE      Object}
       klazz klazz))

(defn boxed-except-long-double
  "Allow long and double, box everything else."
  [klazz]
  (if (#{Long/TYPE Double/TYPE} klazz)
    klazz
    (ensure-boxed klazz)))

(defn as-tag
  [klazz]
  (let [name (class-name klazz)]
    (if (array-class? klazz)
      name
      (symbol name))))

;; Member (method, constructor)

(defrecord MemberInfo
    [id
     type
     name
     declaring-class
     return-type
     raw-parameter-types
     raw-parameter-count
     modifiers
     invocation-args])

(defprotocol ToMemberInfo
  (to-member-info [member]))

(extend-protocol ToMemberInfo
  Method
  (to-member-info [member]
    (->MemberInfo -1
                  :method
                  (.getName member)
                  (.getDeclaringClass member)
                  (.getReturnType member)
                  (.getParameterTypes member)
                  (.getParameterCount member)
                  (let [mods (.getModifiers member)]
                    (cond-> #{}
                      (.isVarArgs member)      (conj :vararg)
                      (Modifier/isStatic mods) (conj :static)))
                  nil))

  Constructor
  (to-member-info [member]
    (->MemberInfo -1
                  :constructor
                  (.getSimpleName (.getDeclaringClass member))
                  (.getDeclaringClass member)
                  (.getDeclaringClass member)
                  (.getParameterTypes member)
                  (.getParameterCount member)
                  (let [mods (.getModifiers member)]
                    (cond-> #{}
                      (.isVarArgs member)      (conj :vararg)
                      (Modifier/isStatic mods) (conj :static)))
                  nil)))

(defn member-form
  "Emit the form to construct a MemberInfo, optionally overriding the id"
  [member & [id]]
  `(->MemberInfo ~(or id (:id member))
                 ~(:type member)
                 ~(:name member)
                 ~(class-repr (:declaring-class member))
                 ~(class-repr (:return-type member))
                 ~(mapv class-repr (:raw-parameter-types member))
                 ~(:raw-parameter-count member)
                 ~(:modifiers member)
                 ~(:invocation-args member)))

(defn member-name
  [member]
  (str (class-name (:declaring-class member))
       "::"
       (:name member)))

(defn member-info
  [member]
  (to-member-info member))

(defn member-constructor?
  [member]
  (= :constructor (:type member)))

(defn member-varargs?
  [member]
  (contains? (:modifiers member) :vararg))

(defn member-static?
  [member]
  (contains? (:modifiers member) :static))

(defn vararg-type
  [member]
  (when (member-varargs? member)
    (array-component (last (:raw-parameter-types member)))))

(defn parameter-count
  [member]
  (cond-> (-> member :raw-parameter-count inc)
    (member-constructor? member) dec
    (member-static? member) dec
    (member-varargs? member) dec))

(defn parameter-types
  [member]
  (concat (when-not (or (member-constructor? member)
                        (member-static? member))
            [(:declaring-class member)])
          (cond-> (:raw-parameter-types member)
            (member-varargs? member) butlast)
          (when (member-varargs? member)
            (repeat (vararg-type member)))))

(defn member-symbol
  [member]
  (cond
    (= :constructor (:type member)) (-> member :declaring-class class-name (str \.) symbol)
    (member-static? member)         (symbol (-> member :declaring-class class-name)
                                            (-> member :name))
    :else                           (->> member :name (str \.) symbol)))

;; Impl

(defn tagged
  [sym tag]
  (vary-meta sym assoc :tag (-> tag boxed-except-long-double as-tag)))

(defn tagged-local
  [sym tag]
  (condp = tag
    Long/TYPE   `(long ~sym)
    Double/TYPE `(double ~sym)
    (tagged sym tag)))

(defn compatible-type?
  [^Class typ val]
  (or (instance? (ensure-boxed typ) val)
      (and (nil? val)
           (not (primitive? typ)))))

(defn ^:no-gen type-error
  [name & args]
  (throw (IllegalArgumentException.
          ^String
          (str "Unrecognised types for " name ": "
               (str/join ", " (map (comp class-name type) args))))))

(defn args-compatible
  ([member args]
   (when (every? identity
                 (map compatible-type?
                      (parameter-types member)
                      args))
     (assoc member :invocation-args args)))
  ([member args coercer]
   (when-let [iargs (reduce (fn [r [arg typ]]
                              (let [coerced (coercer arg (ensure-boxed typ))]
                                (if (compatible-type? typ coerced)
                                  (conj r coerced)
                                  (reduced nil))))
                            []
                            (map vector
                                 args
                                 (parameter-types member)))]
     (assoc member :invocation-args iargs))))

(defn most-specific-type [^Class clz1 ^Class clz2]
  (cond
    (.isAssignableFrom clz2 clz1) clz1
    (.isAssignableFrom clz1 clz2) clz2
    :else nil))

(defn ^:no-gen most-specific-overloads
  [args members]
  (loop [i 0
         candidates (set members)]
    (if (or (empty? candidates)
            (>= i (count args)))
      candidates
      (let [types (map #(nth (parameter-types %) i)
                       members)
            thistype (reduce #(or (most-specific-type %1 %2)
                                  (reduced nil)) types)
            new-cands (filter #(= thistype (nth (parameter-types %) i))
                              members)]
        (recur (inc i)
               (set/intersection candidates (set new-cands)))))))

(defn ^:no-gen prefer-non-vararg-overloads
  [_args members]
  (let [{fixarg false vararg true} (group-by member-varargs? members)]
    (or fixarg vararg)))

(defn invocation-args
  [member args]
  (if member
    (into [(:id member)] (:invocation-args member))
    (into [-1]           args)))

(defn ^:no-gen select-overload
  [args members]
  (let [members (->> members
                     (remove nil?)
                     (prefer-non-vararg-overloads args))]
    (if (<= (count members) 1)
      (invocation-args (first members) args)
      (let [members (most-specific-overloads args members)]
        (if (<= 1 (count members))
          (invocation-args (first members) args)
          (invocation-args nil args))))))

(defn ^:no-gen member-invocation
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
                       (array-of (vararg-type member)))])))

(defn common-supertype
  [t1 t2]
  (loop [seen #{}
         ^Class t1 t1
         ^Class t2 t2]
    (cond
      (= t1 t2) (or t1 Object)
      (seen t1) t1
      (seen t2) t2
      :else     (recur (conj seen t1 t2)
                       (when t1 (.getSuperclass t1))
                       (when t2 (.getSuperclass t2))))))

(defn merge-return-types
  [types]
  (->> types
       (map #(if (= % Void/TYPE) Object %))
       (reduce common-supertype)))

;; Generate form for one arity of a member
(defn ^:no-gen arity-wrapper-form [arity uniadics variadics {:keys [coerce]}]
  (let [arg-vec (mapv #(gensym (str "p" % "_")) (range arity))
        arg-sym (gensym "args_")
        members (concat uniadics variadics)
        ret     (merge-return-types (map :return-type members))]
    `(~(tagged `[~@arg-vec] ret)
      ~(if (zero? arity)
         ;; Even though there may be more than one members compatible with zero args
         ;; (e.g. diff. typed varargs) there is no way to differentiate them on argument types,
         ;; so just emit a call and let the compiler deal with it:
         (member-invocation (first members) [])
         (if-let [mem (and (= 1 (count members))
                           (first members))]
           `(if-let [~arg-vec
                     (:invocation-args
                      (args-compatible ~(member-form mem)
                                       ~arg-vec
                                       ~@(when coerce [coerce])))]
              ~(member-invocation mem arg-vec)
              (type-error ~(member-name mem) ~@arg-vec))
           `(let [~arg-sym ~arg-vec
                  [id# ~@arg-vec]
                  (select-overload
                   ~arg-sym
                   [~@(map-indexed (fn [id member]
                                     `(args-compatible ~(member-form member id) ~arg-sym ~@(when coerce [coerce])))
                                   members)])]
              (case (long id#)
                ~@(mapcat (fn [id mem]
                            [id (member-invocation mem arg-vec)])
                          (range)
                          members)
                (type-error ~(member-name (first members)) ~@arg-vec))))))))

;; Generate form for the highest/variadic arity of a member
(defn ^:no-gen variadic-wrapper-form [min-arity members {:keys [coerce]}]
  (let [fix-args (mapv #(gensym (str "p" % "_")) (range min-arity))
        more-arg (gensym "more_")
        arg-sym (gensym "args_")
        arg-vec (conj fix-args '& more-arg)
        ret (merge-return-types (map :return-type members))]
    `(~(tagged `[~@arg-vec] ret)
      ~(if-let [mem (and (= 1 (count members))
                         (first members))]
         `(if-let [~arg-vec
                   (:invocation-args
                    (args-compatible ~(member-form mem)
                                     (into ~fix-args ~more-arg)
                                     ~@(when coerce [coerce])))]
            ~(member-invocation mem fix-args more-arg)
            (apply type-error ~(member-name mem) ~@fix-args ~more-arg))
         `(let [~arg-sym (into ~fix-args ~more-arg)
                [id# ~@arg-vec]
                (select-overload
                 ~arg-sym
                 [~@(map-indexed (fn [id member]
                                   `(args-compatible ~(member-form member id) ~arg-sym ~@(when coerce [coerce])))
                                 members)])]
            (case (long id#)
              ~@(mapcat (fn [id mem]
                          [id (member-invocation mem fix-args more-arg)])
                        (range)
                        members)
              (apply type-error ~(member-name (first members)) ~@fix-args ~more-arg)))))))

(defn- arg-name
  [arg]
  (cond
    (nil? arg)            "nil"
    (symbol? arg)         (name arg)
    (instance? Class arg) (class-name arg)
    (sequential? arg)     (mapv arg-name arg)
    :else (throw (Exception. (str "Unexpected type in arglist: " (type arg) " (" arg ")")))))

;; Generate defn form for all arities of a named member
(defn member-wrapper-form [fname members opts]
  (let [arities (group-by parameter-count members)]
    `(defn ~fname
       {:arglists '~(sort-by arg-name
                             (map (fn [member]
                                    (tagged (cond-> (vec (take (parameter-count member)
                                                               (parameter-types member)))
                                              (member-varargs? member) (conj '& [(vararg-type member)]))
                                            (:return-type member)))
                                  members))}
       ~@(loop [[[arity membs] & more :as all-membs] (sort arities)
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
               (recur all-membs
                      variadics
                      (conj results (arity-wrapper-form (inc last-arity) [] variadics opts))
                      (inc last-arity))
               ;; else generate form for the next set of members
               (let [{fixarg false vararg true} (group-by member-varargs? membs)
                     variadics (into variadics vararg)]
                 (recur more
                        variadics
                        (conj results (arity-wrapper-form arity fixarg variadics opts))
                        (long arity)))))))))

(defn ^:no-gen compile-time-fn
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
       ((juxt class-constructors class-methods))
       (apply concat)
       (remove (set (class-methods Object)))
       (map member-info)
       (filter (compile-time-fn filter-fn))))

(defn function-sym
  [prefix member]
  (->> member :name camel->kebab (str prefix) symbol))

(defmacro defwrapper
  ([klazz]
   `(defwrapper ~klazz {}))
  ([klazz {:keys [prefix] :as opts}]
   (let [klazz (cond-> klazz (symbol? klazz) resolve)
         members (group-by (partial function-sym prefix)
                           (class-members klazz opts))]
     `(do
        ~@(for [[fname membs] (sort members)]
            (member-wrapper-form fname membs opts))
        nil))))

(defn- defwrapperfn
  "Wrap macro in a function so it gets picked up by the automatic spec test.check generation"
  [cls]
  (macroexpand-1 `(defwrapper ~cls {})))

(def just-for-testing
  ;; prevent linter complaining about unused private var
  [defwrapperfn])
