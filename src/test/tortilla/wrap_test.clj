(ns tortilla.wrap-test
  (:require [clojure.test :refer [deftest is testing]]
            [tortilla.wrap :as w]
            [tortilla.spec])
  (:import [tortilla TestClass]))

;; These will be defined later by defwrapper, declare them now to keep the linter happy:
(declare foo)
(declare bar)
(declare baz)
(declare qux)
(declare hex)
(declare hexy)
(declare flibble)
(declare vararg)

(defn coerce
  "Simple function to handle long->int coercion"
  [val typ]
  (if (and (int? val)
           (= typ Integer))
    (int val)
    val))

(defn test-fn
  [])

(def not-a-fn :x)

(declare unbound-var)

(deftest compile-time-fn-test
  (is (fn? (w/compile-time-fn nil)))
  (is (fn? (w/compile-time-fn 'nil)))
  (is (fn? (w/compile-time-fn test-fn)))
  (is (fn? (w/compile-time-fn 'test-fn)))
  (is (fn? (w/compile-time-fn #'test-fn)))
  (is (thrown? IllegalArgumentException (w/compile-time-fn not-a-fn)))
  (is (thrown? IllegalArgumentException (w/compile-time-fn #'not-a-fn)))
  (is (thrown? IllegalArgumentException (w/compile-time-fn unbound-var)))
  (is (thrown? IllegalArgumentException (w/compile-time-fn #'unbound-var)))
  (is (thrown? IllegalArgumentException (w/compile-time-fn '(fn [x] (inc x))))))

(deftest type-symbol-test
  (is (= 'java.lang.Integer/TYPE (w/type-symbol Integer/TYPE)))
  (is (= 'java.lang.Integer      (w/type-symbol Integer)))
  (is (thrown-with-msg? IllegalArgumentException #"Unrecognised type: void"
                        (w/type-symbol Void/TYPE))))

(deftest defwrapper-test
  (testing "Instantiating wrapper functions"
    (is (w/defwrapper TestClass {:coerce coerce})))

  (testing "Calling a static wrapper function"
    (is (= "foo2_123_456"
           (foo 123 456))))

  (testing "Calling a vararg function"
    (is (= "vararg_0"
           (vararg)))
    (is (= "vararg_1"
           (vararg "a"))))

  (testing "Can automatically coerce from long to int/Integer"
    (is (= "2a"
           (hex (int 42))
           (hex 42)))
    (is (= "7b"
           (hexy (int 123))
           (hexy 123)))
    (is (= "<null>"
           (hexy nil))))

  (testing "Can pass in null for object parameters"
    (is (= "<null>"
           (hexy nil))))

  (testing "Can coerce to int in varargs"
    (is (= 10 (qux 10)))
    (is (= 5  (qux 10 5)))
    (is (= 0  (qux 10 (int 5) 5))))

  (testing "non-consecutive arg counts"
    (is (= 6 (qux 10 2 2)))
    (is (= 4 (qux 10 2 2 2)))
    (is (= "qux_z4" (qux "z" 2 2)))
    (is (= "qux_z3" (qux "z" 2 2 1))))

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
                            (foo tc "1" "2" "3" 4))))))

(declare x-with-primitives)
(deftest overloads-with-primitives
  (is (w/defwrapper TestClass {:prefix "x-"}))
  (is (= "boolean_false" (x-with-primitives false)))
  (is (= "char_x"        (x-with-primitives \x)))
  (is (= "byte_120"      (x-with-primitives (first (.getBytes "x")))))
  (is (= "short_99"      (x-with-primitives (short 99))))
  (is (= "int_88"        (x-with-primitives (int 88))))
  (is (= "long_77"       (x-with-primitives 77)))
  (is (= "float_2.5"     (x-with-primitives (float 2.5))))
  (is (= "double_3.5"    (x-with-primitives 3.5)))
  (is (= "String_z"      (x-with-primitives "z")))
  (is (thrown-with-msg?  IllegalArgumentException
                         #"Unrecognised types for tortilla.TestClass.withPrimitives"
                         (x-with-primitives :oops)))
  (is (= "String_<null>" (x-with-primitives nil))))

(declare y-without-primitives)
(deftest overloads-without-primitives
  (is (w/defwrapper TestClass {:prefix "y-"}))
  (is (= "Boolean_false" (y-without-primitives false)))
  (is (= "Character_x"   (y-without-primitives \x)))
  (is (= "Byte_120"      (y-without-primitives (first (.getBytes "x")))))
  (is (= "Short_99"      (y-without-primitives (short 99))))
  (is (= "Integer_88"    (y-without-primitives (int 88))))
  (is (= "Long_77"       (y-without-primitives 77)))
  (is (= "Float_2.5"     (y-without-primitives (float 2.5))))
  (is (= "Double_3.5"    (y-without-primitives 3.5)))
  (is (= "String_z"      (y-without-primitives "z")))
  (is (thrown-with-msg?  IllegalArgumentException
                         #"Unrecognised types for tortilla.TestClass.withoutPrimitives"
                         (y-without-primitives :oops))))

(declare tortilla-string-format)
(declare tortilla-exception-exception)
(declare tortilla-exception-get-message)
(declare tortilla-system-get-property)
(declare tortilla-file-file)
(declare tortilla-file-get-name)
(deftest more-types
  (testing "Instantiating some more wrappers for better coverage"
    (testing "java.lang.String.format"
      (is (w/defwrapper String       {:prefix "tortilla-string-"}))
      (is (= "foo bar 42 0.000001"
             (tortilla-string-format "foo %s %d %f" "bar" 42 1e-6))))
    (testing "java.lang.Exception.getMessage"
      (is (w/defwrapper Exception    {:prefix "tortilla-exception-"}))
      (is (= "this is a message"
             (-> "this is a message"
                 tortilla-exception-exception
                 tortilla-exception-get-message))))
    (testing "java.lang.System.getProperty"
      (is (w/defwrapper System       {:prefix "tortilla-system-"}))
      (is (= "value"
             (tortilla-system-get-property "tortilla#a.property-that_doesNot$exist" "value"))))
    (testing "java.io.File.getName"
      (is (w/defwrapper java.io.File {:prefix "tortilla-file-"}))
      (is (= "some_file"
             (-> "/path/to/some_file"
                 tortilla-file-file
                 tortilla-file-get-name))))))
