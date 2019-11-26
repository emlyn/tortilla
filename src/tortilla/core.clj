(ns tortilla.core
  (:require [tortilla.wrap :refer [defwrapper]]
            [clojure.pprint :refer [pprint]]))

(defn -main
  [& classes]
  (doseq [cls classes]
    (println "\n====" cls "====")
    (if (resolve (symbol cls))
      (binding [*print-meta* true]
        (pprint (macroexpand-1 `(defwrapper ~(symbol cls)))))
      (println "Error loading class"))))
