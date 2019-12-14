(ns tortilla.main-test
  (:require [clojure.test :refer [deftest is]]
            [tortilla.main :as m]))

(deftest main-test
  (let [stdout (with-out-str (m/-main "Object" "java.lang.Number"))]
    (is (re-find #"(?m)^=+ Object =+$" stdout))
    (is (re-find #"(?m)^=+ java.lang.Number =+$" stdout))
    (is (re-find #"(?m)^ \(clojure.core/defn" stdout))
    (is (re-find #"(?m)\bint-value\b" stdout)))
  (let [stdout (with-out-str (m/-main "invalid.missing.NonexistentClass_"))]
    (is (re-find #"(?m)^=+ invalid.missing.NonexistentClass_ =+$" stdout))
    (is (re-find #"(?m)^Error loading class$" stdout))))
