(ns tortilla.wrap-test
  (:require [clojure.test :refer [deftest is]]
            [tortilla.wrap :as w])
  (:import [tortilla TestClass]))

(declare foo)

(deftest defwrapper-test
  (is (w/defwrapper TestClass {}))
  (is (= "foo2_123_456"
         (foo 123 456)))
  (let [tc (TestClass.)]
    (is (= "foo1_42"
           (foo tc 42)))
    (is (= "foo3_abc_def"
           (foo tc "abc" "def")))
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unrecognised types for tortilla.TestClass.foo: clojure.lang.Keyword"
                          (foo :x)))
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unrecognised types for tortilla.TestClass.foo: tortilla.TestClass, java.lang.Long, java.lang.String"
                          (foo tc 123 "456")))
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unrecognised types for tortilla.TestClass.foo: tortilla.TestClass, java.lang.String, java.lang.String, java.lang.String, java.lang.Long"
                          (foo tc "1" "2" "3" 4))))

  ;; Ensure a few classes can be instantiated without error
  (w/defwrapper System    {:prefix "tortilla-system-"})
  (w/defwrapper Exception {:prefix "tortilla-exception-"})
  (w/defwrapper String    {:prefix "tortilla-string-"}))
