(ns tortilla.core
  (:require [tortilla.wrap :refer [defwrapper]]
            [clojure.pprint :refer [pprint]])
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
  (doseq [cls classes]
    (println "\n====" cls "====")
    (if (resolve (symbol cls))
      (binding [*print-meta* true]
        (pprint (macroexpand-1 `(defwrapper ~(symbol cls)
                                  {:coerce coerce}))))
      (println "Error loading class"))))
