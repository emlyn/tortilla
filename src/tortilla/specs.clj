(ns tortilla.specs
  (:require [tortilla.wrap :as w]
            [clojure.spec.alpha :as s]))

(s/def ::class
  #(instance? Class %))

(s/def ::method
  #(instance? java.lang.reflect.Executable %))

(s/fdef w/class-methods
  :args (s/cat :class ::class)
  :ret  (s/nilable (s/coll-of #(instance? java.lang.reflect.Method %))))

(s/fdef w/class-constructors
  :args (s/cat :class ::class)
  :ret  (s/nilable (s/coll-of #(instance? java.lang.reflect.Constructor %))))

(s/fdef w/method-varargs?
  :args (s/cat :method ::method)
  :ret  boolean?)

(s/fdef w/method-static?
  :args (s/cat :method ::method)
  :ret  boolean?)

(s/fdef w/method-class
  :args (s/cat :method ::method)
  :ret  ::class)

(s/fdef w/vararg-type
  :args (s/cat :method ::method)
  :ret  ::class)

(s/fdef w/class-name
  :args (s/cat :class ::class)
  :ret  simple-symbol?)

(s/fdef w/method-name
  :args (s/cat :method ::method)
  :ret  string?)

(s/fdef w/method-invocation
  :args (s/cat :method ::method)
  :ret  symbol?)

(s/fdef w/parameter-count
  :args (s/cat :method ::method)
  :ret  integer?)

(s/fdef w/parameter-types
  :args (s/cat :method ::method)
  :ret  (s/nilable (s/every ::class)))

(s/fdef w/return-type
  :args (s/cat :method ::method)
  :ret  ::class)

(s/fdef camel->kebab
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
