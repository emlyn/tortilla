(ns tortilla.wrap
  (:require [clojure.string :as str])
  (:import [java.lang.reflect Method Modifier]))

(set! *warn-on-reflection* true)

(defn class-methods [^Class class]
  (seq (.getMethods class)))

(defn constructors [^Class klazz]
  (.getConstructors klazz))

(defn return-type [^Method method]
  (.getReturnType method))

(defn method-static? [^Method method]
  (Modifier/isStatic (.getModifiers method)))

(defn method-varargs? [^Method method]
  (.isVarArgs method))

;; Minimum number of parameters a method accepts. It could take more if it has varargs
(defn parameter-count [^Method method]
  (cond-> (.getParameterCount method)
    (not (method-static? method)) inc
    (method-varargs? method)      dec))

(defn vararg-type [^Method method]
  (when (method-varargs? method)
    (.getComponentType ^Class (last (.getParameterTypes method)))))

;; Possibly infinite (if method has varargs) list of parameter types accepted by method
(defn parameter-types [^Class klazz ^Method method]
  (concat (when-not (method-static? method)
            [klazz])
          (cond-> (.getParameterTypes method)
            (method-varargs? method) butlast)
          (when (method-varargs? method)
            (repeat (vararg-type method)))))

(defn method-name [^Method method]
  (.getName method))

(defn class-name [^Class klazz]
  (symbol (.getName klazz)))

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

(defn method-invocation [klazz method]
  (if (method-static? method)
    (symbol (str (class-name klazz)) (method-name method))
    (symbol (str "." (method-name method)))))

;; Generate form for one arity of a method
(defn arity-wrapper-form [arity klazz uniadics variadics]
  (let [more-arg (gensym "more_")
        arg-vec (mapv #(gensym (str "p" % "_")) (range arity))
        methods (concat uniadics variadics)
        ret     (if (apply = (map return-type methods))
                  (return-type (first methods))
                  java.lang.Object)]
    `(~(tagged `[~@arg-vec] ret)
      (cond
        ~@(mapcat
           (fn [method]
             `[(and ~@(map (fn [sym ^Class klz]
                             `(instance? ~(ensure-boxed (class-name klz)) ~sym))
                           arg-vec
                           (parameter-types klazz method)))
               (let [~@(mapcat (fn [sym ^Class klz]
                                 [sym (tagged-local sym klz)])
                               arg-vec
                               (parameter-types klazz method))]
                 (~(method-invocation klazz method)
                  ~@arg-vec))])
           uniadics)
        ~@(mapcat
           (fn [method]
             `[(and ~@(map (fn [sym ^Class klz]
                             `(instance? ~(ensure-boxed (class-name klz)) ~sym))
                           arg-vec
                           (parameter-types klazz method)))
               (let [~@(mapcat (fn [sym ^Class klz]
                                 [sym (tagged-local sym klz)])
                               (take (parameter-count method) arg-vec)
                               (parameter-types klazz method))
                     ~more-arg (into-array ~(vararg-type method)
                                           ~(subvec arg-vec (parameter-count method)))
                     ~more-arg ~(tagged-local more-arg (array-class (vararg-type method)))]
                 (~(method-invocation klazz method)
                  ~@(if (method-varargs? method)
                      (concat (take (parameter-count method) arg-vec)
                              [more-arg])
                      arg-vec)))])
           variadics)
        :else (throw (IllegalArgumentException.
                      (str ~(str "Unrecognised types for " (class-name klazz) \. (method-name (first methods)) ": ")
                           ~@(mapcat (fn [p#] [`(.getName ^Class (type ~p#)) ", "]) (butlast arg-vec))
                           (.getName ^Class (type ~(last arg-vec))))))))))

;; Generate form for the highest/variadic arity of a method
(defn variadic-wrapper-form [min-arity klazz methods]
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
                             `(instance? ~(ensure-boxed (class-name klz)) ~sym))
                           (take min-arity arg-vec)
                           (parameter-types klazz method))
                    (every? (partial instance? ~(vararg-type method)) ~more-arg))
               (let [~@(mapcat (fn [sym ^Class klz]
                                 [sym (tagged-local sym klz)])
                               (take (parameter-count method) arg-vec)
                               (parameter-types klazz method))
                     ~more-arg (into-array ~(vararg-type method)
                                           (into ~(subvec arg-vec
                                                          (parameter-count method)
                                                          min-arity)
                                                 ~more-arg))
                     ~more-arg ~(tagged-local more-arg (array-class (vararg-type method)))]
                 (~(method-invocation klazz method)
                  ~@(take (parameter-count method) arg-vec)
                  ~more-arg))])
           methods)
        :else (throw (IllegalArgumentException.
                      (str ~(str "Unrecognised types for " (class-name klazz) \. (method-name (first methods)) ": ")
                           ~@(mapcat (fn [p#] [`(.getName ^Class (type ~p#)) ", "]) (take min-arity arg-vec))
                           (str/join ", " (map (fn [p#] (.getName ^Class (type p#))) ~more-arg)))))))))

;; Generate defn form for all arities of a named method
(defn method-wrapper-form [fname klazz methods]
  (let [arities (group-by parameter-count methods)]
    `(defn ~fname
       {:arglists '~(map (fn [method]
                           (cond-> (vec (take (parameter-count method)
                                              (parameter-types klazz method)))
                             (method-varargs? method) (conj '& [(vararg-type method)])))
                         methods)}
       ~@(loop [[[arity meths] & more] (sort arities)
                variadics []
                results []
                last-arity -1]
           (if (nil? arity) ;; no more methods, generate variadic form if necessary
             (if (seq variadics)
               (conj results (variadic-wrapper-form last-arity klazz variadics))
               results)
             (if (and (seq variadics) (> arity (inc last-arity)))
               (recur [[arity meths] more]
                      variadics
                      (conj results (arity-wrapper-form (inc last-arity) klazz [] variadics))
                      (inc last-arity))
               (let [{vararg true fixarg false} (group-by method-varargs? meths)
                     variadics (into variadics vararg)]
                 (recur more
                        variadics
                        (conj results (arity-wrapper-form arity klazz fixarg variadics))
                        (long arity)))))))))

(defmacro defwrapper [klazz {:keys [prefix]}]
  (let [klazz (resolve klazz)
        methods (->> klazz
                     class-methods
                     (remove (set (class-methods Object)))
                     (group-by method-name))]
    `(do
       ~@(for [[mname meths] methods
               :let [fname (symbol (str prefix (camel->kebab mname)))]]
           (method-wrapper-form fname klazz meths)))))
