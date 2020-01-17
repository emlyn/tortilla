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

(def example-member-infos
  (set (map w/member-info example-members)))

(def primitive-names
  '#{void boolean byte short int long float double char})

(s/def ::class
  (s/with-gen
    #(instance? Class %)
    #(s/gen example-classes)))

(s/def ::non-void-class
  (s/and ::class
         #(not= Void/TYPE %)))

(s/def ::array-class
  (s/with-gen
    (s/and ::non-void-class w/array-class?)
    #(gen/fmap w/array-of (s/gen ::non-void-class))))

(s/def ::class-name
  (s/with-gen
    (s/and simple-symbol?
           (s/or :prim primitive-names
                 :obj #(instance? Class (resolve %))))
    #(s/gen (->> example-classes
                 (remove w/primitive?)
                 (map (comp symbol w/class-name))
                 (set)))))

(s/def ::raw-member
  (s/with-gen
    #(instance? java.lang.reflect.Executable %)
    #(s/gen example-members)))

(s/def ::member
  (s/with-gen
    #(instance? tortilla.wrap.MemberInfo %)
    #(s/gen example-member-infos)))

(s/def ::iobj
  (s/with-gen
    #(instance? clojure.lang.IObj %)
    #(s/gen (s/or :sym simple-symbol?
                  :lst list?
                  :vec vector?
                  :map map?))))

(defn nop-coercer
  [val _typ]
  val)

(s/def ::coercer
  (s/with-gen
    (s/or :fn fn?
          :sym symbol?
          :var var?)
    #(s/gen #{nop-coercer
              `nop-coercer
              #'nop-coercer})))

(s/def ::defn
  (s/spec (s/cat :defn #{`defn}
                 :args ::cs/defn-args)))

;; General

(s/fdef w/camel->kebab
  :args (s/cat :string string?)
  :ret  string?)

;; Class

(s/fdef w/class-methods
  :args (s/cat :class ::class)
  :ret  (s/nilable (s/coll-of (s/and ::raw-member
                                     #(instance? java.lang.reflect.Method %)))))

(s/fdef w/class-constructors
  :args (s/cat :class ::class)
  :ret  (s/nilable (s/coll-of (s/and ::raw-member
                                     #(instance? java.lang.reflect.Constructor %)))))

(s/fdef w/class-name
  :args (s/cat :class (s/nilable ::class))
  :ret  string?)

(s/fdef w/primitive?
  :args (s/cat :klazz ::class)
  :ret  boolean?)

(s/fdef w/class-symbol
  :args (s/cat :klazz (s/with-gen ::class #(s/gen ::non-void-class)))
  :ret  symbol?
  :fn   #(if (-> % :args :klazz w/primitive?)
           (= "TYPE" (-> % :ret name))
           (= (-> % :ret name)
              (-> % :args :klazz w/class-name))))

(s/fdef w/array-class?
  :args (s/cat :klazz ::class)
  :ret  boolean?)

(s/fdef w/array-of
  :args (s/cat :klazz ::non-void-class)
  :ret  ::array-class
  :fn   #(= (.getComponentType ^Class (:ret %))
            (-> % :args :klazz)))

(s/fdef w/array-component
  :args (s/cat :klazz (s/with-gen
                        ::non-void-class
                        #(s/gen (s/or :a ::array-class :b ::non-void-class))))
  :ret  (s/nilable ::non-void-class)
  :fn #(if (-> % :args :klazz w/array-class?)
         (-> % :ret)
         (-> % :ret nil?)))

(s/fdef w/ensure-boxed
  :args (s/cat :klazz ::non-void-class)
  :ret  (s/and ::non-void-class (comp not w/primitive?))
  :fn   #(or (#{Byte Short Integer Float Character Boolean Long Double}
              (-> % :ret))
             (= (-> % :ret)
                (-> % :args :klazz))))

(s/fdef w/boxed-except-long-double
  :args (s/cat :klazz ::non-void-class)
  :ret  ::non-void-class
  :fn   #(or (#{Byte Short Integer Float Character Boolean Object}
              (-> % :ret))
             (= (-> % :ret)
                (-> % :args :klazz))))

(s/fdef w/as-tag
  :args (s/cat :klazz ::non-void-class)
  :ret (s/or :sym simple-symbol?
             :str string?))

;; Member

(s/fdef w/member-info
  :args (s/cat :member ::raw-member)
  :ret  ::member)

(s/fdef w/member-constructor?
  :args (s/cat :member ::member)
  :ret  boolean?)

(s/fdef w/member-varargs?
  :args (s/cat :member ::member)
  :ret  boolean?)

(s/fdef w/member-static?
  :args (s/cat :member ::member)
  :ret  boolean?)

(s/fdef w/vararg-type
  :args (s/cat :member ::member)
  :ret  (s/nilable ::class))

(s/fdef w/parameter-count
  :args (s/cat :member ::member)
  :ret  nat-int?)

(s/fdef w/parameter-types
  :args (s/cat :member ::member)
  :ret  (s/nilable (s/every ::class)))

(s/fdef w/member-symbol
  :args (s/cat :member ::member)
  :ret  symbol?)

;; Impl

(s/fdef w/tagged
  :args (s/cat :value ::iobj
               :tag ::non-void-class)
  :ret #(->> % meta :tag))

(s/fdef w/tagged-local
  :args (s/cat :value ::iobj
               :tag ::non-void-class)
  :ret (s/or :long-double (s/and seq? #(-> % first #{`long `double}))
             :other #(->> % meta :tag)))

(s/fdef w/compatible-type?
  :args (s/cat :klazz ::non-void-class
               :value any?)
  :ret boolean?)

(s/fdef w/type-error
  :args (s/cat :name string?
               :args (s/* any?))
  :ret (constantly false))

(s/fdef w/args-compatible
  :args (s/cat :id nat-int?
               :args (s/coll-of any? :kind vector? :min-count 1)
               :types (s/coll-of ::non-void-class :kind vector? :min-count 1)
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
                 :matches (s/coll-of (s/nilable
                                      (s/and (s/coll-of any? :kind vector? :min-count 2)
                                             #(nat-int? (first %))))
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
  :fn (s/or :no-match (s/and #(-> % :ret first (= -1))
                             #(= (-> % :args :args)
                                 (-> % :ret rest)))
            :match (s/and #(-> % :ret first nat-int?)
                          #(some (partial = (:ret %))
                                 (-> % :args :matches)))))

(s/fdef w/member-invocation
  :args (s/cat :member ::member
               :args (s/coll-of simple-symbol? :kind vector?)
               :more (s/? simple-symbol?))
  :ret (s/and seq? #(-> % first symbol?)))

(s/fdef w/merge-return-types
  :args (s/cat :types (s/coll-of ::class :min-count 1))
  :ret (s/and ::non-void-class))

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

(s/fdef w/member-wrapper-form
  :args (s/cat :fname simple-symbol?
               :members (s/coll-of ::member :kind vector? :distinct true :min-count 1)
               :opts (s/keys))
  :ret ::defn)

(s/fdef w/compile-time-fn
  :args (s/cat :fun any?)
  :ret fn?)

(s/fdef w/class-members
  :args (s/cat :klazz ::class
               :opts (s/keys))
  :ret (s/coll-of ::member))

(s/fdef w/function-sym
  :args (s/cat :prefix (s/nilable string?)
               :member ::member)
  :ret simple-symbol?)

(s/fdef w/defwrapper
  :args (s/cat :klazz (s/or :symbol ::class-name
                            :class  ::class)
               :opts (s/keys))
  :ret  (s/cat :do #{'do}
               :funcs (s/* ::defn)))

(s/fdef w/defwrapperfn
  :args (s/cat :klazz (s/or :symbol ::class-name
                            :class  ::class))
  :ret  (s/cat :do #{'do}
               :funcs (s/* ::defn)))
