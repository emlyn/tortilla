(ns tortilla.coerce
  (:import [java.lang.reflect Method Modifier]))

(defprotocol Coercible
  (coerce [val typ]))

(defmulti coerce-long   (fn [_val typ] typ))
(defmulti coerce-double (fn [_val typ] typ))
(defmulti coerce-kw     (fn [_val typ] typ))
(defmulti coerce-vector (fn [_val typ] typ))
(defmulti coerce-list   (fn [_val typ] typ))
(defmulti coerce-map    (fn [_val typ] typ))
(defmulti coerce-fn     (fn [_val typ] typ))
(defmulti coerce-nil    (fn [_val typ] typ))

(extend-protocol Coercible
  Long
  (coerce [val typ]
    (coerce-long val typ))

  Double
  (coerce [val typ]
    (coerce-double val typ))

  clojure.lang.Keyword
  (coerce [val typ]
    (coerce-kw val typ))

  clojure.lang.PersistentVector
  (coerce [val typ]
    (coerce-vector val typ))

  clojure.lang.PersistentList
  (coerce [val typ]
    (coerce-list val typ))

  clojure.lang.APersistentMap
  (coerce [val typ]
    (coerce-map val typ))

  clojure.lang.AFn
  (coerce [val typ]
    (coerce-fn val typ))

  nil
  (coerce [val typ]
    (coerce-nil val typ))

  Object
  (coerce [val _] val))

(defmethod coerce-long :default [val _typ] val)

(defmethod coerce-long Integer [val _typ]
  (let [coerced (unchecked-int val)]
    (if (= coerced val)
      coerced
      val)))

(defmethod coerce-double :default [val _typ] val)

(defmethod coerce-double Float [val _typ]
  (cond
    (<= (- Float/MAX_VALUE) val Float/MAX_VALUE)
    (float val)

    (Double/isInfinite val)
    (if (pos? val)
      Float/POSITIVE_INFINITY
      Float/NEGATIVE_INFINITY)

    (Double/isNaN val)
    Float/NaN

    :else
    val))

(defmethod coerce-kw :default [val _typ] val)

(defmethod coerce-kw Enum [val typ]
  (try
    (Enum/valueOf typ (name val))
    (catch IllegalArgumentException _
      val)))

(defmethod coerce-vector :default [val ^Class typ]
  (if (.isArray typ)
    (let [ctyp (.getComponentType typ)]
      (try
        (into-array ctyp (map #(coerce % ctyp) val))
        (catch IllegalArgumentException _
          val)))
    val))

(defmethod coerce-list :default [val _typ] val)

(defmethod coerce-map :default [val _typ] val)

(defmethod coerce-fn :default [val _typ] val)

(defmethod coerce-nil :default [val _typ] val)

(defmacro coerce-fn-impl [typ]
  ;; Ignore symbols that don't resolve,
  ;; they may be classes that were added in a later Java version.
  (when-let [klazz ^Class (resolve typ)]
    (let [die #(throw (Exception. (format "Not a functional interface: %s" typ)))
          _ (when-not (.isInterface klazz) (die))
          methods (filter #(-> ^Method % .getModifiers (Modifier/isAbstract))
                          (.getMethods klazz))
          _ (when-not (= 1 (count methods)) (die))
          method ^java.lang.reflect.Method (first methods)
          args (repeatedly (.getParameterCount method)
                           #(gensym "arg"))]
      `(defmethod coerce-fn ~typ [val# ~'_]
         (reify ~typ
           (~(symbol (.getName method)) [~'_ ~@args]
             (val# ~@args)))))))

;; We don't need to implement this for java.lang.Runnable, java.util.concurrent.Callable, or java.util.Comparator
;; as Clojure functions already implement these interfaces.

(coerce-fn-impl java.io.FileFilter)
(coerce-fn-impl java.io.FilenameFilter)
(coerce-fn-impl java.io.ObjectInputFilter) ;; Java 9+
(coerce-fn-impl java.lang.Thread$UncaughtExceptionHandler)
(coerce-fn-impl java.nio.file.DirectoryStream$Filter)
(coerce-fn-impl java.nio.file.PathMatcher)
(coerce-fn-impl java.security.PrivilegedAction)
(coerce-fn-impl java.security.PrivilegedExceptionAction)
(coerce-fn-impl java.time.temporal.TemporalAdjuster)
(coerce-fn-impl java.time.temporal.TemporalQuery)
(coerce-fn-impl java.util.concurrent.Flow$Publisher) ;; Java 9+
(coerce-fn-impl java.util.function.BiConsumer)
(coerce-fn-impl java.util.function.BiFunction)
(coerce-fn-impl java.util.function.BiPredicate)
(coerce-fn-impl java.util.function.BooleanSupplier)
(coerce-fn-impl java.util.function.Consumer)
(coerce-fn-impl java.util.function.DoubleBinaryOperator)
(coerce-fn-impl java.util.function.DoubleConsumer)
(coerce-fn-impl java.util.function.DoubleFunction)
(coerce-fn-impl java.util.function.DoublePredicate)
(coerce-fn-impl java.util.function.DoubleSupplier)
(coerce-fn-impl java.util.function.DoubleToIntFunction)
(coerce-fn-impl java.util.function.DoubleToLongFunction)
(coerce-fn-impl java.util.function.DoubleUnaryOperator)
(coerce-fn-impl java.util.function.Function)
(coerce-fn-impl java.util.function.IntBinaryOperator)
(coerce-fn-impl java.util.function.IntConsumer)
(coerce-fn-impl java.util.function.IntFunction)
(coerce-fn-impl java.util.function.IntPredicate)
(coerce-fn-impl java.util.function.IntSupplier)
(coerce-fn-impl java.util.function.IntToDoubleFunction)
(coerce-fn-impl java.util.function.IntToLongFunction)
(coerce-fn-impl java.util.function.IntUnaryOperator)
(coerce-fn-impl java.util.function.LongBinaryOperator)
(coerce-fn-impl java.util.function.LongConsumer)
(coerce-fn-impl java.util.function.LongFunction)
(coerce-fn-impl java.util.function.LongPredicate)
(coerce-fn-impl java.util.function.LongSupplier)
(coerce-fn-impl java.util.function.LongToDoubleFunction)
(coerce-fn-impl java.util.function.LongToIntFunction)
(coerce-fn-impl java.util.function.LongUnaryOperator)
(coerce-fn-impl java.util.function.ObjDoubleConsumer)
(coerce-fn-impl java.util.function.ObjIntConsumer)
(coerce-fn-impl java.util.function.ObjLongConsumer)
(coerce-fn-impl java.util.function.Predicate)
(coerce-fn-impl java.util.function.Supplier)
(coerce-fn-impl java.util.function.ToDoubleBiFunction)
(coerce-fn-impl java.util.function.ToDoubleFunction)
(coerce-fn-impl java.util.function.ToIntBiFunction)
(coerce-fn-impl java.util.function.ToIntFunction)
(coerce-fn-impl java.util.function.ToLongBiFunction)
(coerce-fn-impl java.util.function.ToLongFunction)
(coerce-fn-impl java.util.logging.Filter)
