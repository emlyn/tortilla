(ns tortilla.main-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [tortilla.main :as m]))

(use-fixtures
  :once
  (fn [test-fn]
    ;; Ensure calling exit won't quit, but cause a test failure instead
    (with-redefs [m/exit (fn [code message]
                           (throw (Exception. (str "exit " code " " message))))]
      (test-fn))))

(deftest coercer-test
  (is (instance? Integer (m/coerce 42   Integer)))
  (is (instance? Float   (m/coerce 3.14 Float)))
  (is (instance? Long    (m/coerce 99   String)))
  (is (instance? String  (m/coerce "99" Integer))))

(deftest parse-coords-test
  (is (= '[foo/bar "1.2.3"]
         (m/parse-coords "[foo/bar \"1.2.3\"]")
         (m/parse-coords "foo:bar:1.2.3")))
  (is (= '[baz "2.3.4"]
         (m/parse-coords "[baz \"2.3.4\"]")
         (m/parse-coords "baz:2.3.4"))))

(deftest validate-args-test
  (testing "Help option exits with 0"
    (is (= 0 (:exit (m/validate-args ["-h"])))))
  (testing "Version"
    (let [{:keys [exit message]} (m/validate-args ["--version"])]
      (is (= 0 exit))
      (is (re-find #"Tortilla version [0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)? \([0-9a-f]+\)" message))))
  (testing "No options causes an error"
    (is (= 1 (:exit (m/validate-args [])))))
  (testing "Invalid option causes an error"
    (is (= 1 (:exit (m/validate-args ["--invalid-option"])))))
  (testing "Non-option argument causes an error"
    (is (= 1 (:exit (m/validate-args ["invalid"])))))
  (testing "Valid options don't cause an error"
    (is (= {:class ["java.lang.Number" "java.lang.String"]
            :metadata true
            :instrument true
            :coerce nil
            :prefix ""
            :namespace nil
            :unwrap-do true
            :width 80
            :dep '[[foo "1.0"]
                   [bar/baz "2.0"]]}
           (m/validate-args ["-c" "java.lang.Number" "-c" "java.lang.String" "-w" "80"
                             "-d" "foo:1.0" "-d" "[bar/baz \"2.0\"]"])))))

(deftest main-test
  (let [coerce-check #"\btortilla.main/coerce\b"]
    (testing "Listing members"
      (let [stdout (with-out-str (m/-main "--no-instrument" "-c" "java.lang.Number" "--members"))]
        (is (re-find #"(?m)^;; =+ java.lang.Number =+$" stdout))
        (is (re-find #"(?m)longValue\(java.lang.Number\):long" stdout))))
    (testing "Filtering members"
      (let [stdout (with-out-str (m/-main "--no-instrument" "--members"
                                          "-c" "java.lang.Number" "-i" "longValue"))]
        (is (re-find #"(?m)longValue\(java.lang.Number\):long" stdout))
        (is (not (re-find #"(?m)intValue\(java.lang.Number\):int" stdout))))
      (let [stdout (with-out-str (m/-main "--no-instrument" "--members"
                                          "-c" "java.lang.Number" "-x" "longValue"))]
        (is (not (re-find #"(?m)longValue\(java.lang.Number\):long" stdout)))
        (is (re-find #"(?m)intValue\(java.lang.Number\):int" stdout))))
    (testing "With coercer"
      (let [stdout (with-out-str (m/-main "--no-instrument" "--no-metadata" "--no-unwrap-do"
                                          "--coerce" "tortilla.main/coerce"
                                          "-w" "200" "-c" "java.lang.Object" "-c" "java.lang.Number"))]
        (is (re-find #"(?m)^;; =+ java.lang.Object =+$" stdout))
        (is (re-find #"(?m)^;; =+ java.lang.Number =+$" stdout))
        (is (re-find #"(?m)^ *\(clojure.core/defn" stdout))
        (is (re-find #"(?m)\bint-value\b" stdout))
        (is (re-find coerce-check stdout))))
    (testing "Without coercer"
      (let [stdout (with-out-str (m/-main "--no-instrument" "--no-metadata" "--no-unwrap-do"
                                          "-w" "200" "-c" "java.lang.Object" "-c" "java.lang.Number"))]
        (is (re-find #"(?m)^;; =+ java.lang.Object =+$" stdout))
        (is (re-find #"(?m)^;; =+ java.lang.Number =+$" stdout))
        (is (re-find #"(?m)^ *\(clojure.core/defn" stdout))
        (is (re-find #"(?m)\bint-value\b" stdout))
        (is (not (re-find coerce-check stdout)))))
    (testing "Writing to file"
      (let [temp (.getPath
                  (doto (java.io.File/createTempFile "tortilla_test" ".clj")
                    .delete))
            _ (m/-main "--no-instrument"
                       "-c" "tortilla.testing.TestClass"
                       "-n" "tortilla.testing.test-class"
                       "-p" "prefix-"
                       "-o" temp)
            stdout (slurp temp)]
        (is (re-find #"(?m)^\(ns tortilla.testing.test-class" stdout))
        (is (re-find #"(?m)^;; =+ tortilla.testing.TestClass =+$" stdout))
        (is (re-find #"(?m)defn prefix-foo" stdout))))
    (testing "Dynamically adding to classpath"
      (let [stdout (with-out-str (m/-main "--no-instrument"
                                          "-d" "org.jblas:jblas:1.2.4"
                                          "-c" "org.jblas.ComplexDoubleMatrix"))]
        (is (re-find #"(?m)^;; =+ org.jblas.ComplexDoubleMatrix =+$" stdout))))
    (testing "Invalid options quit with error"
      (is (thrown-with-msg? Exception #"^exit 0 Usage: tortilla "
                            (m/-main "-c" "java.lang.String" "-h")))
      (is (thrown-with-msg? Exception #"^exit 1 Error:\nUnknown option"
                            (m/-main "-c" "java.lang.String" "--bad-option")))
      (is (thrown-with-msg? Exception #"^exit 1 Error:\nOptions must start with a hyphen"
                            (m/-main "-c" "java.lang.String" "members")))
      (is (thrown-with-msg? Exception #"^exit 1 Error:\nMust supply at least one class to wrap"
                            (m/-main)))
      (is (thrown-with-msg? Exception #"^exit 1 Invalid class: invalid.Class"
                            (m/-main "-c" "invalid.Class"))))))
