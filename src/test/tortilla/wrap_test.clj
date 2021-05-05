(ns tortilla.wrap-test
  (:require [clojure.test :refer [deftest is testing]]
            [tortilla.wrap :as w]
            [tortilla.spec])
  (:import [tortilla.testing TestClass]))

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

(deftest class-name-test
  (is (= "java.lang.String"
         (w/class-name (class ""))))
  (is (= "java.lang.Long"
         (w/class-name (class 0))))
  (is (= "long"
         (w/class-name Long/TYPE)))
  (is (= "nil"
         (w/class-name (class nil)))))

(deftest array-test
  (is (= "[Ljava.lang.Long;"
         (w/class-name (w/array-of Long))))
  (is (= "[J"
         (w/class-name (w/array-of Long/TYPE))))
  (is (= "[[J"
         (w/class-name (w/array-of (w/array-of Long/TYPE)))))
  (doseq [t tortilla.spec/example-classes
          :when (not= t Void/TYPE)]
    (testing (str "Checking class " (w/class-name t))
      (is (= t (w/array-component (w/array-of t)))))))

(deftest tagged-local-test
  (let [tagged-String (w/tagged-local 'a String)]
    (is (symbol? tagged-String))
    (is (= 'java.lang.String
           (-> tagged-String meta :tag))))
  (let [tagged-Long-array (w/tagged-local 'a (class (into-array Long [])))]
    (is (symbol? tagged-Long-array))
    (is (= "[Ljava.lang.Long;"
           (-> tagged-Long-array meta :tag))))
  (let [tagged-long-array (w/tagged-local 'a (class (long-array [])))]
    (is (symbol? tagged-long-array))
    (is (= "[J"
           (-> tagged-long-array meta :tag))))
  (let [tagged-Long (w/tagged-local 'a Long)]
    (is (symbol? tagged-Long))
    (is (= 'java.lang.Long
           (-> tagged-Long meta :tag))))
  (let [tagged-long (w/tagged-local 'a Long/TYPE)]
    (is (seq? tagged-long))
    (is (= `long
           (first tagged-long))))
  (let [tagged-double (w/tagged-local 'a Double/TYPE)]
    (is (seq? tagged-double))
    (is (= `double
           (first tagged-double))))
  (let [tagged-int (w/tagged-local 'a Integer/TYPE)]
    (is (symbol? tagged-int))
    (is (= 'java.lang.Integer
           (-> tagged-int meta :tag)))))

(deftest arg-sym-test
  (is (= 'java.lang.String (#'w/arg-sym String)))
  (is (= 'java.lang.Long   (#'w/arg-sym Long)))
  (is (= 'long             (#'w/arg-sym Long/TYPE)))
  (is (= '[java.lang.Long] (#'w/arg-sym (type (into-array Long [])))))
  (is (= '[long]           (#'w/arg-sym (type (long-array [])))))
  (is (= '[[int]]          (#'w/arg-sym (type (into-array [(int-array [])]))))))

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

(deftest class-repr-test
  (is (= 'java.lang.Integer/TYPE (w/class-repr Integer/TYPE)))
  (is (= 'java.lang.Integer      (w/class-repr Integer)))
  (is (= 'java.lang.Void/TYPE    (w/class-repr Void/TYPE)))
  ;; There are no unknown primitive types to use for this test, so skip it:
  #_(is (thrown-with-msg? IllegalArgumentException #"Unrecognised primitive type: "
                          (w/class-repr ?)))
  (is (= `(w/array-of java.lang.Integer/TYPE)
         (w/class-repr (type (int-array 1)))))
  (is (= `(w/array-of java.lang.Integer)
         (w/class-repr (type (into-array [(int 1)]))))))

(deftest defwrapper-test
  (testing "Instantiating wrapper functions"
    (is (nil? (w/defwrapper TestClass {:coerce coerce}))))

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
    (is (= "qux1_z4" (qux "z" 2 2)))
    (is (= "qux1_z3" (qux "z" 2 2 1))))

  (testing "non-vararg is preferred"
    (is (= "qux2" (qux "z" 10 2 2 2))))

  (testing "Checking array arguments"
    (is (= 3
           (bar (into-array Long [2 3 4])
                (long-array [3 2 1])))))

  (testing "testing overlapping types"
    (is (= "baz1_abc"
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
                            #"Unrecognised types for tortilla.testing.TestClass::foo: clojure.lang.Keyword"
                            (foo :x)))
      (is (thrown-with-msg? IllegalArgumentException
                            #"Unrecognised types for tortilla.testing.TestClass::foo: tortilla.testing.TestClass, java.lang.Long, java.lang.String"
                            (foo tc 123 "456")))
      (is (thrown-with-msg? IllegalArgumentException
                            #"Unrecognised types for tortilla.testing.TestClass::foo: tortilla.testing.TestClass, java.lang.String, java.lang.String, java.lang.String, java.lang.Long"
                            (foo tc "1" "2" "3" 4))))))

(declare x-with-primitives)
(declare x-without-primitives)
(deftest many-overloads
  (is (nil? (w/defwrapper TestClass {:prefix "x-" :coerce :none})))
  (testing "With primitives"
    (is (= "boolean_1" (x-with-primitives false)))
    (is (= "char_1"    (x-with-primitives \x)))
    (is (= "byte_1"    (x-with-primitives (first (.getBytes "x")))))
    (is (= "short_1"   (x-with-primitives (short 99))))
    (is (= "int_1"     (x-with-primitives (int 88))))
    (is (= "long_1"    (x-with-primitives 77)))
    (is (= "float_1"   (x-with-primitives (float 2.5))))
    (is (= "double_1"  (x-with-primitives 3.5)))
    (is (= "String_1"  (x-with-primitives "z")))
    (is (= "String_1"  (x-with-primitives nil)))
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unrecognised types for tortilla.testing.TestClass::withPrimitives"
                          (x-with-primitives :oops))))
  (testing "Without primitives"
    (is (= "Boolean_1"   (x-without-primitives false)))
    (is (= "Character_1" (x-without-primitives \x)))
    (is (= "Byte_1"      (x-without-primitives (first (.getBytes "x")))))
    (is (= "Short_1"     (x-without-primitives (short 99))))
    (is (= "Integer_1"   (x-without-primitives (int 88))))
    (is (= "Long_1"      (x-without-primitives 77)))
    (is (= "Float_1"     (x-without-primitives (float 2.5))))
    (is (= "Double_1"    (x-without-primitives 3.5)))
    (is (= "String_1"    (x-without-primitives "z")))
    (is (thrown-with-msg? IllegalArgumentException
                          #"Unrecognised types for tortilla.testing.TestClass::withoutPrimitives"
                          (x-without-primitives :oops)))))

(declare tortilla-string-format)
(declare tortilla-exception-exception)
(declare tortilla-exception-get-message)
(declare tortilla-system-get-property)
(declare tortilla-file-file)
(declare tortilla-file-get-name)
(deftest more-types
  (testing "Instantiating some more wrappers for better coverage"
    (testing "java.lang.String.format"
      (is (nil? (w/defwrapper String       {:prefix "tortilla-string-"})))
      (is (= "foo bar 42 0.000001"
             (tortilla-string-format "foo %s %d %f" "bar" 42 1e-6))))
    (testing "java.lang.Exception.getMessage"
      (is (nil? (w/defwrapper Exception    {:prefix "tortilla-exception-"})))
      (is (= "this is a message"
             (-> "this is a message"
                 tortilla-exception-exception
                 tortilla-exception-get-message))))
    (testing "java.lang.System.getProperty"
      (is (nil? (w/defwrapper System       {:prefix "tortilla-system-"})))
      (is (= "value"
             (tortilla-system-get-property "tortilla#a.property-that_doesNot$exist" "value"))))
    (testing "java.io.File.getName"
      (is (nil? (w/defwrapper java.io.File {:prefix "tortilla-file-"})))
      (is (= "some_file"
             (-> "/path/to/some_file"
                 tortilla-file-file
                 tortilla-file-get-name))))))

  (declare compare-to)
  (deftest omit-opts
    (is (nil? (w/defwrapper Comparable)))
    (is (zero? (compare-to 0 0))))
