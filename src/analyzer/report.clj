(ns analyzer.report
  (:require
   [clojure.string]
   [selmer.parser]))


(defn md [session]
  (selmer.parser/render-file "session-report.md" session)
)
