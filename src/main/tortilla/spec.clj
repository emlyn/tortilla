(ns tortilla.spec
  (:require [tortilla.wrap :as w]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.core.specs.alpha :as cs]))

(def example-classes
  #{Boolean/TYPE Byte/TYPE Short/TYPE Integer/TYPE Long/TYPE Float/TYPE Double/TYPE Character/TYPE
    Void/TYPE Boolean Byte Short Integer Long Float Double Character Void
    Object Number String Math Class Process Package Enum Exception Thread System
    java.io.File java.io.Reader java.io.Writer java.io.InputStream java.io.OutputStream
    java.math.BigDecimal java.math.BigInteger
    java.net.URI java.net.URL java.net.InetAddress java.nio.Buffer
    java.time.Clock java.time.Duration java.time.Instant java.time.ZonedDateTime
    java.util.Arrays java.util.ArrayList java.util.HashMap java.util.UUID})

(def example-members
  (set (mapcat #(concat (.getMethods ^Class %)
                        (.getConstructors ^Class %))
               example-classes)))

(def primitive-names
  '#{void boolean byte short int long float double char})

(s/def ::class
  (s/with-gen
    #(instance? Class %)
    #(s/gen example-classes)))

(s/def ::class-name
  (s/with-gen
    (s/and simple-symbol?
           (s/or :prim primitive-names
                 :obj #(instance? Class (resolve %))))
    #(s/gen (->> example-classes
                 (remove w/primitive?)
                 (map (comp symbol w/class-name))
                 (set)))))

(s/def ::member
  (s/with-gen
    #(instance? java.lang.reflect.Executable %)
    #(s/gen example-members)))

(s/def ::coercer
  (s/with-gen
    fn?
    #(s/gen #{(fn nop-coercer [val _typ]
                val)
              (fn int-coercer [val typ]
                (if (and (number? val) (= typ Integer))
                  (try (int val)
                       (catch IllegalArgumentException _
                         val))
                  val))})))

(s/fdef w/class-methods
  :args (s/cat :class ::class)
  :ret  (s/nilable (s/coll-of (s/and ::member
                                     #(instance? java.lang.reflect.Method %)))))

(s/fdef w/class-constructors
  :args (s/cat :class ::class)
  :ret  (s/nilable (s/coll-of (s/and ::member
                                     #(instance? java.lang.reflect.Constructor %)))))

(s/fdef w/member-varargs?
  :args (s/cat :member ::member)
  :ret  boolean?)

(s/fdef w/member-static?
  :args (s/cat :member ::member)
  :ret  boolean?)

(s/fdef w/member-class
  :args (s/cat :member ::member)
  :ret  ::class)

(s/fdef w/vararg-type
  :args (s/cat :member ::member)
  :ret  (s/nilable ::class))

(s/fdef w/class-name
  :args (s/cat :class ::class)
  :ret  string?)

(s/fdef w/member-name
  :args (s/cat :member ::member)
  :ret  string?)

(s/fdef w/member-symbol
  :args (s/cat :member ::member)
  :ret  symbol?)

(s/fdef w/parameter-count
  :args (s/cat :member ::member)
  :ret  integer?)

(s/fdef w/parameter-types
  :args (s/cat :member ::member)
  :ret  (s/nilable (s/every ::class)))

(s/fdef w/return-type
  :args (s/cat :member ::member)
  :ret  ::class)

(s/fdef w/camel->kebab
  :args (s/cat :string string?)
  :ret  string?)

(s/fdef w/array-class
  :args (s/cat :class (s/and ::class #(not= Void/TYPE %)))
  :ret  (s/and ::class
               #(.isArray ^Class %))
  :fn   #(= (.getComponentType ^Class (:ret %))
            (-> % :args :class)))

(s/fdef w/primitive?
  :args (s/cat :klazz ::class)
  :ret  boolean?)

(s/fdef w/type-symbol
  :args (s/cat :klazz (s/and ::class #(not= % Void/TYPE)))
  :ret  symbol?
  :fn   #(if (-> % :args :klazz w/primitive?)
           (= "TYPE" (-> % :ret name))
           (= (-> % :ret name)
              (-> % :args :klazz w/class-name))))

(s/fdef w/ensure-boxed
  :args (s/cat :klazz (s/and ::class #(not= % Void/TYPE)))
  :ret  simple-symbol?
  :fn   #(or ('#{java.lang.Long java.lang.Double
                 java.lang.Byte java.lang.Short java.lang.Integer
                 java.lang.Float java.lang.Character java.lang.Boolean}
              (-> % :ret))
             (= (-> % :ret name)
                (-> % :args :klazz w/class-name))))

(s/fdef w/ensure-boxed-long-double
  :args (s/cat :klazz ::class)
  :ret  simple-symbol?
  :fn   #(or ('#{java.lang.Object
                 java.lang.Byte java.lang.Short java.lang.Integer
                 java.lang.Float java.lang.Character java.lang.Boolean}
              (-> % :ret))
             (= (-> % :ret name)
                (-> % :args :klazz w/class-name))))

(s/fdef w/tagged
  :args (s/cat :value (s/or :sym simple-symbol?
                            :vec vector?)
               :tag ::class)
  :ret #(->> % meta :tag))

(s/fdef w/tagged-local
  :args (s/cat :value (s/or :sym simple-symbol?
                            :coerce seq?)
               :tag ::class)
  :ret (s/or :long-double (s/and seq? #(-> % first #{`long `double}))
             :other #(->> % meta :tag)))

(s/fdef w/compatible-type?
  :args (s/cat :klazz ::class
               :value any?)
  :ret boolean?)

(s/fdef w/type-error
  :args (s/cat :name string?
               :args (s/* any?))
  :ret (constantly false))

(s/fdef w/args-compatible
  :args (s/cat :id nat-int?
               :args (s/coll-of any? :kind vector? :min-count 1)
               :types (s/coll-of ::class :kind vector? :min-count 1)
               :coercer (s/? ::coercer))
  :ret (s/nilable (s/and (s/coll-of any? :kind vector? :min-count 2)
                         #(nat-int? (first %))))
  :fn #(or (-> % :ret nil?)
           (and (= (-> % :args :id)
                   (-> % :ret first))
                (= (-> % :args :args count)
                   (-> % :ret count dec)))))

(s/fdef w/select-overload
  :args (s/with-gen
          (s/cat :args (s/coll-of any? :kind any? :min-count 1)
                 :matches (s/coll-of (s/and (s/coll-of any? :kind vector? :min-count 2)
                                            #(nat-int? (first %)))
                                     :kind vector?))
          #(gen/let [len (gen/fmap inc gen/nat)]
             (gen/tuple (gen/vector gen/simple-type len)
                        (gen/fmap (fn [vecs]
                                    (mapv (fn [id v]
                                            (into [id] v))
                                          (range)
                                          vecs))
                                  (gen/vector (gen/vector gen/simple-type len)
                                              0 5)))))
  :ret (s/and (s/coll-of any? :kind vector?)
              #(int? (first %)))
  :fn (s/or :no-match (s/and #(= -1 (-> % :ret first))
                             #(= (-> % :args :args)
                                 (-> % :ret rest)))
            :match (s/and #(-> % :ret first nat-int?)
                          #(some (partial = (:ret %))
                                 (-> % :args :matches)))))

(s/fdef w/member-invocation
  :args (s/cat :member ::member
               :args (s/coll-of simple-symbol? :kind vector?)
               :more (s/? any?))
  :ret (s/and seq? #(-> % first symbol?)))

(s/fdef w/arity-wrapper-form
  :args (s/cat :arity nat-int?
               :uniadics (s/nilable (s/coll-of (s/and ::member #(not (w/member-varargs? %)))
                                               :kind vector? :distinct true))
               :variadics (s/coll-of (s/and ::member w/member-varargs?)
                                     :kind vector? :distinct true)
               :options (s/keys)))

(s/fdef w/variadic-wrapper-form
  :args (s/cat :min-arity nat-int?
               :members (s/coll-of (s/and ::member w/member-varargs?)
                                   :kind vector? :distinct true :min-count 1)
               :options (s/keys)))

(s/fdef w/defwrapper
  :args (s/cat :klazz (s/and simple-symbol?
                             #(instance? Class (resolve %)))
               :opts (s/keys)))

(s/def ::defn
  (s/spec (s/cat :defn #{`defn}
                 :args ::cs/defn-args)))

(s/fdef w/defwrapperfn
  :args (s/cat :class ::class-name)
  :ret  (s/cat :do #{'do}
               :funcs (s/* ::defn)))
