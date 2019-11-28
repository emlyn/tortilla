(ns tortilla.core-test
  (:require [clojure.test :refer [deftest is]]
            [tortilla.core :as c]))

(deftest main-test
  (let [stdout (with-out-str (c/-main "Object" "java.lang.Number"))]
    (is (re-find #"(?m)^=+ Object =+$" stdout))
    (is (re-find #"(?m)^=+ java.lang.Number =+$" stdout))
    (is (re-find #"(?m)^ \(clojure.core/defn$" stdout))
    (is (re-find #"(?m)^ *int-value$" stdout)))
  (let [stdout (with-out-str (c/-main "invalid.missing.NonexistentClass_"))]
    (is (re-find #"(?m)^=+ invalid.missing.NonexistentClass_ =+$" stdout))
    (is (re-find #"(?m)^Error loading class$" stdout))))
