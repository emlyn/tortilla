(ns tortilla.coerce-test
  (:require [clojure.test :refer [deftest is]]
            [tortilla.coerce :as c]
            [tortilla.spec]))

(deftest coerce-fn-macro-test
  (is (= `(defmethod c/coerce-fn Runnable)
         (take 3 (macroexpand-1 `(c/coerce-fn-impl Runnable)))))
  (is (thrown? Exception (macroexpand-1 `(c/coerce-fn-impl String))))
  (is (thrown? Exception (macroexpand-1 `(c/coerce-fn-impl CharSequence)))))

(deftest coerce-long
  (is (instance? Integer (c/coerce 42 Integer)))
  (is (instance? Integer (c/coerce (long Integer/MIN_VALUE) Integer)))
  (is (instance? Long    (c/coerce (inc Integer/MAX_VALUE) Integer)))
  (is (instance? Long    (c/coerce 99 String))))

(deftest coerce-double
  (is (instance? Float  (c/coerce 3.14 Float)))
  (is (instance? Float  (c/coerce ##Inf Float)))
  (is (instance? Float  (c/coerce ##-Inf Float)))
  (is (instance? Float  (c/coerce ##NaN Float)))
  (is (instance? Double (c/coerce (* 2 Float/MAX_VALUE) Float)))
  (is (instance? Double (c/coerce 1.5 String))))

(deftest coerce-keyword
  (is (= Thread$State/NEW (c/coerce :NEW Thread$State)))
  (is (= :NOT-A-STATE     (c/coerce :NOT-A-STATE Thread$State)))
  (is (= :NEW             (c/coerce :NEW Long))))

(deftest coerce-vector
  (let [str_arr_type  (type (into-array String [""]))
        long_arr_type (type (into-array Long [1]))]
    (is (instance? str_arr_type
                   (c/coerce ["Hello" "world"] str_arr_type)))
    (is (instance? long_arr_type
                   (c/coerce [1 2 3] long_arr_type)))
    (is (instance? clojure.lang.PersistentVector
                   (c/coerce ["Hello" "world"] long_arr_type)))
    (is (instance? clojure.lang.PersistentVector
                   (c/coerce ["Hello" "world"] String)))))

(deftest coerce-fn
  (is (instance? java.util.function.Function
                 (c/coerce inc java.util.function.Function)))
  (is (instance? java.util.function.Predicate
                 (c/coerce #(<= % 42) java.util.function.Predicate)))
  (is (instance? clojure.lang.AFunction
                 (c/coerce (fn plustwo [x] (+ x 2)) String))))

(deftest coerce-other
  (is (= "99" (c/coerce "99" Long)))
  (is (nil? (c/coerce nil String))))