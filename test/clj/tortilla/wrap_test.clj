(ns tortilla.wrap-test
  (:require [clojure.test :refer [deftest is testing]]
            [tortilla.wrap :as w])
  (:import [tortilla TestClass]))

;; These will be defined later by defwrapper, declare them now to keep the linter happy:
(declare foo)
(declare bar)
(declare baz)
(declare qux)
(declare hex)
(declare hexy)
(declare flibble)

(defn coerce
  "Simple function to handle long->int coercion"
  [val typ]
  (if (and (int? val)
           (= typ Integer))
    (int val)
    val))

(deftest defwrapper-test
  (testing "Instantiating wrapper functions"
    (is (w/defwrapper TestClass {:coerce coerce})))

  (testing "Calling a static wrapper function"
    (is (= "foo2_123_456"
           (foo 123 456))))

  (testing "Can automatically coerce from long to int/Integer"
    (is (= "2a"
           (hex (int 42))
           (hex 42)))
    (is (= "7b"
           (hexy (int 123))
           (hexy 123))))

  (testing "Can coerce to int in varargs"
    (is (= 10 (qux 10)))
    (is (= 5  (qux 10 5)))
    (is (= 0  (qux 10 (int 5) 5))))

  (testing "Checking array arguments"
    (is (= 3
           (bar (into-array Long [2 3 4])
                (long-array [3 2 1])))))

  (testing "testing overlapping types"
    ;; This one doesn't work reliably, as it will just take the first matching overload,
    ;; not necessarily the best match.
    #_(is (= "baz1_abc"
           (baz "abc")))
    (is (= "baz2_123"
           (baz 123))))

  (testing "Checking void return type"
    (is (nil? (flibble 42))))

  (let [tc (TestClass.)]
    (testing "Calling some non-static wrappers"
      (is (= "foo1_42"
             (foo tc 42)))
      (is (= "foo3_abc_def"
             (foo tc "abc" "def"))))

    (testing "Bad argument types cause an exception"
      (is (thrown-with-msg? IllegalArgumentException
                            #"Unrecognised types for tortilla.TestClass.foo: clojure.lang.Keyword"
                            (foo :x)))
      (is (thrown-with-msg? IllegalArgumentException
                            #"Unrecognised types for tortilla.TestClass.foo: tortilla.TestClass, java.lang.Long, java.lang.String"
                            (foo tc 123 "456")))
      (is (thrown-with-msg? IllegalArgumentException
                            #"Unrecognised types for tortilla.TestClass.foo: tortilla.TestClass, java.lang.String, java.lang.String, java.lang.String, java.lang.Long"
                            (foo tc "1" "2" "3" 4)))))

  (testing "Instantiating some more wrappers for better coverage"
    (w/defwrapper System    {:prefix "tortilla-system-"})
    (w/defwrapper Exception {:prefix "tortilla-exception-"})
    (w/defwrapper String    {:prefix "tortilla-string-"})))
