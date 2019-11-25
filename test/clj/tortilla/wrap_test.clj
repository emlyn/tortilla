(ns tortilla.wrap-test
  (:require [clojure.test :refer :all]
            [tortilla.wrap :as w])
  (:import [tortilla TestClass]))

(let [ns *ns*]
  (use-fixtures
    ;; Because symbols are quoted for passing in to macroexpand,
    ;; we have to ensure that the tests are run in the context
    ;; of this namespace, and not the namespace from where the
    ;; tests are run, so that any non-fully-qualified symbols
    ;; are resolved correctly.
    :once
    (fn [test-fn]
      (binding [*ns* ns]
        (test-fn)))))

(deftest defwrapper-test
  (let [result (macroexpand-1 '(w/defwrapper TestClass))]
    (is (seq? result))
    (is (= 2 (count result)) "Should have just `do` followed by one defn form")
    (is (= 'do (first result)))))
