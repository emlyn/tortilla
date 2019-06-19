(ns defwrapper
  (:require [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn class-methods [^Class class]
  (seq (.getMethods class)))

(defn constructors [^Class klazz]
  (.getDeclaredConstructors klazz))

(defn return-type [^java.lang.reflect.Method method]
  (.getReturnType method))

(defn parameter-types [^java.lang.reflect.Method method]
  (seq (.getParameterTypes method)))

(defn parameter-count [^java.lang.reflect.Method method]
  (.getParameterCount method))

(defn method-name [^java.lang.reflect.Method method]
  (.getName method))

(defn class-name [^Class klazz]
  (symbol (.getName klazz)))

(defn camel->kebab
  [string]
  (-> string
      (clojure.string/replace #"(.)([A-Z][a-z]+)" "$1-$2")
      (clojure.string/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (clojure.string/lower-case)))

(defn class->name [^Class class]
  (->
   (if (.isArray class)
     (str (.getName (.getComponentType class)) "-array")
     (.getName class))
   (str/replace "." "-")))

(defn method-static? [^java.lang.reflect.Method method]
  (java.lang.reflect.Modifier/isStatic (.getModifiers method)))

(defn method-public? [^java.lang.reflect.Method method]
  (java.lang.reflect.Modifier/isPublic (.getModifiers method)))

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

(defn wrapper-multi-tail [klazz methods]
  (let [static? (method-static? (first methods))
        this    (gensym "this")
        arg-vec (take (parameter-count (first methods)) (repeatedly gensym))
        ret     (if (apply = (map return-type methods))
                  (return-type (first methods))
                  java.lang.Object)]
    `(~(tagged `[~@(when-not static? [this]) ~@arg-vec] ret)
      (cond
        ~@(mapcat
           (fn [method]
             `[(and ~@(map (fn [sym ^Class klz]
                             `(instance? ~(ensure-boxed (class-name klz)) ~sym))
                           arg-vec
                           (parameter-types method)))
               (let [~@(mapcat (fn [sym ^Class klz]
                                 [sym (tagged-local sym klz)])
                               arg-vec
                               (parameter-types method))]
                 (~(if static?
                     (symbol (str klazz) (method-name method))
                     (symbol (str "." (method-name method))))
                  ~@(when-not static? [(tagged this klazz)])
                  ~@arg-vec))])
           methods)))))

(defn wrapper-tail [klazz method]
  (let [nam     (method-name method)
        ret     (return-type method)
        par     (parameter-types method)
        static? (method-static? method)
        arg-vec (into (if static? [] [(tagged (gensym "this") klazz)])
                      (map #(tagged (gensym (class->name %)) %))
                      par)]
    `(~(tagged arg-vec ret)
      (~(if static?
          (symbol (str klazz) nam)
          (symbol (str "." nam))) ~@(map #(vary-meta % dissoc :tag) arg-vec)))))

(defn method-wrapper-form [fname klazz methods]
  (let [arities (group-by parameter-count methods)]
    `(defn ~fname
       {:arglists '~(map (comp (partial into [klazz])
                               parameter-types) methods)}
       ~@(map (fn [[cnt meths]]
                (if (= 1 (count meths))
                  (wrapper-tail klazz (first meths))
                  (wrapper-multi-tail klazz meths)))
              arities))))

(defmacro defwrapper [klazz & [prefix]]
  (let [methods (->> klazz
                     resolve
                     class-methods
                     (filter method-public?)
                     (remove (set (class-methods Object)))
                     (group-by method-name))]
    `(do
       ~@(for [[mname meths] methods
               :let [fname (symbol (str prefix (camel->kebab mname)))]]
           (method-wrapper-form fname klazz meths)))))


(comment
  #_(binding [*print-meta* true]
      (prn (macroexpand-1 '(defwrapper javax.sound.midi.MidiSystem "midi-sys-"))))

  (defwrapper javax.sound.midi.MidiSystem "midi-sys-")
  (defwrapper javax.sound.midi.Synthesizer "synth-")
  (defwrapper javax.sound.midi.MidiChannel "chan-")

  ;;; Play some tunes

  (def synth (midi-sys-get-synthesizer))

  (synth-open synth)

  (def chan (first (synth-get-channels synth)))

  (meta #'synth-get-channels)


  (do
    (chan-note-on chan 60 600)
    (chan-note-on chan 64 600)
    (chan-note-on chan 67 600)

    (Thread/sleep 1100)

    (chan-note-on chan 60 600)
    (chan-note-on chan 64 600)
    (chan-note-on chan 67 600)
    (chan-note-on chan 71 600)))


;; TODO:
;; - better varargs
;; - test if (long ... ) actually works
;; - prevent unnecessary boxing
;; - constructors
