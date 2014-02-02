(ns analyzer.core
  (:require
   [cheshire.core]
   [clojure.java.io :as io]
   [analyzer.transform :refer [transform-session error?]]
   [analyzer.validate :refer [validate-session]]
   [analyzer.report :as report])

 )

;; See http://stackoverflow.com/questions/7756909/in-clojure-1-3-how-to-read-and-write-a-file

(defn analyze [input-file report-file]

  (with-open
    [w (io/writer report-file :append true)

     ]

     (let [
           records (cheshire.core/parsed-seq (io/reader input-file) true)

           ]
     (doseq [[session-id, session-data] (group-by :sessionId records)]
       (let [
             transformed-session (transform-session session-data)
             validation-data  (validate-session transformed-session)
             bundle  (merge transformed-session {
                     :session-id session-id
                     :transformed-session transformed-session
                     :session-valid (not (some error? validation-data))
                     :validation-data validation-data
                     })
            ]
        (.write w (report/md bundle))
      )))))


(analyze "var/spool/tracking-logs/2014-02-01_14-00_akotnik.events" "out.md")

