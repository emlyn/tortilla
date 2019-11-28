(ns tortilla.wrap-test
  (:require [clojure.test :refer [deftest is]]
            [tortilla.wrap :as w])
  (:import [tortilla TestClass]))

(declare foo)

(deftest defwrapper-test
  (is (w/defwrapper TestClass))
  (is (= "foo2_123_456"
         (foo 123 456)))
  (let [tc (TestClass.)]
    (is (= "foo1_42"
           (foo tc 42)))
    ;; TODO: Handle varargs properly
    (is (= "foo3_abc_def"
           (foo tc (into-array ["abc" "def"]))))))
