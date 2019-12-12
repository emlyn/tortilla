(ns tortilla.wrap
  (:require [clojure.string :as str])
  (:import [java.lang.reflect Executable Constructor Method Modifier]))

(set! *warn-on-reflection* true)

(defn class-methods [^Class klazz]
  (seq (.getMethods klazz)))

(defn class-constructors [^Class klazz]
  (seq (.getConstructors klazz)))

(defn method-varargs? [^Executable method]
  (.isVarArgs method))

(defn method-static? [^Executable method]
  (Modifier/isStatic (.getModifiers method)))

(defn method-class ^Class [^Executable method]
  (.getDeclaringClass method))

(defn vararg-type [^Executable method]
  (when (method-varargs? method)
    (.getComponentType ^Class (last (.getParameterTypes method)))))

(defn class-name [^Class klazz]
  (symbol (.getName klazz)))

(defprotocol MethodInfo
  (method-name* [_])
  (method-invocation* [_])
  ;; Minimum number of parameters a method accepts. It could take more if it has varargs
  (parameter-count* [_])
  ;; Possibly infinite (if method has varargs) list of parameter types accepted by method
  (parameter-types* [_])
  (return-type* [_]))

;; Wrap protocol methods in functions so we get spec instrumentation
(defn method-name [m]
  (method-name* m))

(defn method-invocation [m]
  (method-invocation* m))

(defn parameter-count [m]
  (parameter-count* m))

(defn parameter-types [m]
  (parameter-types* m))

(defn return-type [m]
  (return-type* m))

(extend-protocol MethodInfo
  Method
  (method-name* [^Method m]
    (.getName m))
  (method-invocation* [^Method m]
    (if (method-static? m)
      (symbol (-> m method-class class-name str) (method-name m))
      (symbol (str "." (method-name m)))))
  (parameter-count* [^Method m]
    (cond-> (.getParameterCount m)
      (not (method-static? m)) inc
      (method-varargs? m)      dec))
  (parameter-types* [^Method m]
    (concat (when-not (method-static? m)
              [(method-class m)])
            (cond-> (.getParameterTypes m)
              (method-varargs? m) butlast)
            (when (method-varargs? m)
              (repeat (vararg-type m)))))
  (return-type* [^Method m]
    (.getReturnType m))

  Constructor
  (method-name* [^Constructor c]
    (-> c method-class .getSimpleName))
  (method-invocation* [^Constructor c]
    (-> c method-class class-name (str ".") symbol))
  (parameter-count* [^Constructor c]
    (cond-> (.getParameterCount c)
      (method-varargs? c) dec))
  (parameter-types* [^Constructor c]
    (concat (cond-> (.getParameterTypes c)
              (method-varargs? c) butlast)
            (when (method-varargs? c)
              (repeat (vararg-type c)))))
  (return-type* [^Constructor c]
    (.getDeclaringClass c)))

(defn camel->kebab
  [string]
  (-> string
      (str/replace #"(.)([A-Z][a-z]+)" "$1-$2")
      (str/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (str/lower-case)))

(defn primitive-class [sym]
  ('{byte java.lang.Byte/TYPE
     short java.lang.Short/TYPE
     int java.lang.Integer/TYPE
     long java.lang.Long/TYPE
     float java.lang.Float/TYPE
     double java.lang.Double/TYPE
     char java.lang.Character/TYPE
     boolean java.lang.Boolean/TYPE} sym sym))

(defn array-class [klz]
  (class (into-array klz [])))

(defn ensure-boxed [t]
  (get '{byte java.lang.Byte
         short java.lang.Short
         int java.lang.Integer
         long java.lang.Long
         float java.lang.Float
         double java.lang.Double
         char java.lang.Character
         boolean java.lang.Boolean
         void java.lang.Object}
       t t))

(defn ensure-boxed-long-double
  "Allow long and double, box everything else."
  [c]
  (let [t (if (instance? Class c)
            (class-name c)
            c)]
    (get '{byte java.lang.Byte
           short java.lang.Short
           int java.lang.Integer
           float java.lang.Float
           char java.lang.Character
           boolean java.lang.Boolean
           void java.lang.Object}
         t t)))

(defn tagged [value tag]
  (let [tag (if (and (instance? Class tag) (.isArray ^Class tag))
              `(array-class ~(primitive-class (class-name (.getComponentType ^Class tag))))
              tag)]
    (vary-meta value assoc :tag (ensure-boxed-long-double tag))))

(defn tagged-local [value tag]
  (let [tag (ensure-boxed-long-double tag)]
    (cond
      (= 'long tag)
      `(long ~value)

      (= 'double tag)
      `(double ~value)

      :else
      (vary-meta value assoc :tag tag))))

;; Generate form for one arity of a method
(defn arity-wrapper-form [arity uniadics variadics {:keys [coerce]}]
  (let [arg-vec (mapv #(gensym (str "p" % "_")) (range arity))
        methods (concat uniadics variadics)
        ret     (if (apply = (map return-type methods))
                  (return-type (first methods))
                  java.lang.Object)]
    `(~(tagged `[~@arg-vec] ret)
      (cond
        ~@(mapcat
           (fn [method]
             `[(and ~@(map (fn [sym ^Class klz]
                             `(instance? ~(ensure-boxed (class-name klz))
                                         ~(if coerce
                                            `(~coerce ~sym ~(ensure-boxed (class-name klz)))
                                            sym)))
                           arg-vec
                           (parameter-types method)))
               (~(method-invocation method)
                ~@(map (fn [sym ^Class klz]
                         (tagged-local (if coerce `(~coerce ~sym ~(ensure-boxed (class-name klz))) sym) klz))
                       arg-vec
                       (parameter-types method)))])
           uniadics)
        ~@(mapcat
           (fn [method]
             `[(and ~@(map (fn [sym ^Class klz]
                             `(instance? ~(ensure-boxed (class-name klz))
                                         ~(if coerce
                                            `(~coerce ~sym ~(ensure-boxed (class-name klz)))
                                            sym)))
                           arg-vec
                           (parameter-types method)))
               (~(method-invocation method)
                ~@(map (fn [sym ^Class klz]
                         (tagged-local (if coerce `(~coerce ~sym ~(ensure-boxed (class-name klz))) sym) klz))
                       (take (parameter-count method) arg-vec)
                       (parameter-types method))
                ~(tagged-local `(into-array ~(vararg-type method)
                                            ~(mapv (fn [sym]
                                                     (if coerce
                                                       `(~coerce ~sym ~(ensure-boxed (class-name (vararg-type method))))
                                                       sym))
                                                   (drop (parameter-count method) arg-vec)))
                               (array-class (vararg-type method))))])
           variadics)
        :else (throw (IllegalArgumentException.
                      ^String
                      (str ~(str "Unrecognised types for " (-> methods first method-class class-name)
                                 \. (-> methods first method-name) ": ")
                           ~@(mapcat (fn [p#] [`(.getName ^Class (type ~p#)) ", "]) (butlast arg-vec))
                           (.getName ^Class (type ~(last arg-vec))))))))))

;; Generate form for the highest/variadic arity of a method
(defn variadic-wrapper-form [min-arity methods {:keys [coerce]}]
  (let [more-arg (gensym "more_")
        arg-vec (into (mapv #(gensym (str "p" % "_")) (range min-arity))
                      ['& more-arg])
        ret (if (apply = (map return-type methods))
              (return-type (first methods))
              java.lang.Object)]
    `(~(tagged `[~@arg-vec] ret)
      (cond
        ~@(mapcat
           (fn [method]
             `[(and ~@(map (fn [sym ^Class klz]
                             `(instance? ~(ensure-boxed (class-name klz))
                                         ~(if coerce
                                            `(~coerce ~sym ~(ensure-boxed (class-name klz)))
                                            sym)))
                           (take min-arity arg-vec)
                           (parameter-types method))
                    (every? (partial instance? ~(ensure-boxed (class-name (vararg-type method))))
                            ~(if coerce
                               `(map #(~coerce % ~(ensure-boxed (class-name (vararg-type method))))
                                     ~more-arg)
                               more-arg)))
               (~(method-invocation method)
                ~@(map (fn [sym ^Class klz]
                         (tagged-local (if coerce
                                         `(~coerce ~sym ~(ensure-boxed (class-name klz)))
                                         sym)
                                       klz))
                       (take (parameter-count method) arg-vec)
                       (parameter-types method))
                ~(tagged-local `(into-array ~(vararg-type method)
                                            (into ~(mapv (fn [sym]
                                                           (if coerce
                                                             `(~coerce ~sym ~(ensure-boxed (class-name (vararg-type method))))
                                                             sym))
                                                         (subvec arg-vec
                                                                 (parameter-count method)
                                                                 min-arity))
                                                  ~(if coerce
                                                    `(map #(~coerce % ~(ensure-boxed (class-name (vararg-type method))))
                                                          ~more-arg)
                                                    more-arg)))
                               (array-class (vararg-type method))))])
           methods)
        :else (throw (IllegalArgumentException.
                      ^String
                      (str ~(str "Unrecognised types for " (-> methods first method-class class-name)
                                 \. (-> methods first method-name) ": ")
                           ~@(mapcat (fn [p#] [`(.getName ^Class (type ~p#)) ", "]) (take min-arity arg-vec))
                           (str/join ", " (map (fn [p#] (.getName ^Class (type p#))) ~more-arg)))))))))

;; Generate defn form for all arities of a named method
(defn method-wrapper-form [fname methods opts]
  (let [arities (group-by parameter-count methods)]
    `(defn ~fname
       {:arglists '~(map (fn [method]
                           (cond-> (vec (take (parameter-count method)
                                              (parameter-types method)))
                             (method-varargs? method) (conj '& [(vararg-type method)])))
                         methods)}
       ~@(loop [[[arity meths] & more] (sort arities)
                variadics []
                results []
                last-arity -1]
           (if (nil? arity) ;; no more methods, generate variadic form if necessary
             (if (seq variadics)
               (conj results (variadic-wrapper-form last-arity variadics opts))
               results)
             (if (and (seq variadics) (> arity (inc last-arity)))
               (recur [[arity meths] more]
                      variadics
                      (conj results (arity-wrapper-form (inc last-arity) [] variadics opts))
                      (inc last-arity))
               (let [{vararg true fixarg false} (group-by method-varargs? meths)
                     variadics (into variadics vararg)]
                 (recur more
                        variadics
                        (conj results (arity-wrapper-form arity fixarg variadics opts))
                        (long arity)))))))))

(defmacro defwrapper [klazz {:keys [prefix] :as opts}]
  (let [methods (->> klazz
                     resolve
                     ((juxt class-constructors class-methods))
                     (apply concat)
                     (remove (set (class-methods Object)))
                     (group-by method-name))]
    `(do
       ~@(for [[mname meths] methods
               :let [fname (symbol (str prefix (camel->kebab mname)))]]
           (method-wrapper-form fname meths opts)))))
