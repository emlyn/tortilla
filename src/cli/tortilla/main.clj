(ns tortilla.main
  (:require [tortilla.wrap :refer [defwrapper]]
            [tortilla.spec]
            [orchestra.spec.test :as st]
            [fipp.clojure :as fipp])
  (:gen-class))

(defprotocol Coercer
  (coerce [val typ]))

(extend-protocol Coercer
  Object
  (coerce [val _] val)

  Number
  (coerce [val typ]
    (if (= Integer typ)
      (int val)
      val)))

(defn -main
  [& classes]
  (st/instrument)
  (doseq [cls classes]
    (println "\n====" cls "====")
    (if (resolve (symbol cls))
      (fipp/pprint (macroexpand-1 `(defwrapper ~(symbol cls) {:coerce coerce}))
                   {:print-meta true
                    :width 100})
      (println "Error loading class"))))
