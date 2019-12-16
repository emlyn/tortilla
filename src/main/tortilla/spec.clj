(ns tortilla.spec
  (:require [tortilla.wrap :as w]
            [clojure.spec.alpha :as s]
            [clojure.core.specs.alpha :as cs]))

(def example-classes
  #{Object Number Integer Long Float Double Byte Boolean String Character
    Void Math Class Process Package Enum Exception Thread System
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
  '#{byte short int long float double char boolean void})

(s/def ::class
  (s/with-gen
    #(instance? Class %)
    #(s/gen example-classes)))

(s/def ::class-name
  (s/with-gen
    (s/and simple-symbol?
           (s/or :prim primitive-names
                 :obj #(instance? Class (resolve %))))
    #(s/gen (set (map (fn [^Class cls]
                        (symbol (.getName cls)))
                     example-classes)))))

(s/def ::member
  (s/with-gen
    #(instance? java.lang.reflect.Executable %)
    #(s/gen example-members)))

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
  :ret  ::class-name)

(s/fdef w/member-name
  :args (s/cat :member ::member)
  :ret  string?)

(s/fdef w/member-invocation
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

(s/fdef w/primitive-class
  :args (s/cat :sym simple-symbol?)
  :ret  symbol?
  :fn   #(or ('#{java.lang.Byte/TYPE java.lang.Short/TYPE java.lang.Integer/TYPE
                 java.lang.Long/TYPE java.lang.Float/TYPE java.lang.Double/TYPE
                 java.lang.Character/TYPE java.lang.Boolean/TYPE}
              (-> % :ret))
             (= (-> % :ret)
                (-> % :args :sym))))

(s/fdef w/array-class
  :args (s/cat :class ::class)
  :ret  (s/and ::class
               #(.isArray ^Class %))
  :fn   #(= (.getComponentType ^Class (:ret %))
            (-> % :args :class)))

(s/fdef w/ensure-boxed
  :args (s/cat :sym simple-symbol?)
  :ret  simple-symbol?
  :fn   #(or ('#{java.lang.Byte java.lang.Short java.lang.Integer
                 java.lang.Long java.lang.Float java.lang.Double
                 java.lang.Character java.lang.Boolean}
              (-> % :ret))
             (= (-> % :ret)
                (-> % :args :sym))))

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
