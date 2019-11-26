(ns tortilla.spec.wrap-test
  (:require [tortilla.wrap :as w]
            [clojure.spec.alpha :as s]
            [clojure.core.specs.alpha :as cs])
  (:import [tortilla TestClass]))

(s/def ::class
  (s/with-gen (s/and simple-symbol?
                     #(instance? Class (resolve %)))
    #(s/gen #{'TestClass 'Object 'Long 'String 'Boolean 'System 'Thread 'Exception 'Class
              'java.net.URI 'java.io.File 'java.nio.Buffer 'java.math.BigDecimal
              'java.util.ArrayList 'java.util.UUID})))

(s/def ::defn
  (s/spec (s/cat :defn #{`defn}
                 :args ::cs/defn-args)))

(s/fdef defwrapper
  :args (s/cat :class ::class)
  :ret  (s/cat :do #{'do}
               :funcs (s/* ::defn)))

(defn defwrapper
  "Wrap macro in a function so it gets picked up by the automatic spec test.check generation"
  [cls]
  (macroexpand-1 `(w/defwrapper ~cls)))
