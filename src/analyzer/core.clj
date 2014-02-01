(ns analyzer.core
  (:require
   [chesire/core :as json]
   [clojure.java/io :as io])

  (:import [java.lang.Math])
)

(def MAX_PLAYING_DIFF 0.02) ;; how much can sum of segments differ from total-playing time

(declare video-data to-segments)

(def file-name "var/spool/tracking-logs/2014-02-01_14-40_akotnik.events")

(def parsed (cheshire.core/parsed-seq (clojure.java.io/reader file-name) true))

(def sessions (group-by :sessionId (filter :videoSession parsed)))

(def session (first sessions))
(def session-data (second session))

(defn error? [r] (= (:type r) :error))

(defn parse-session [[session-id session-data]]
  (let [
        video-data (extract-video-data session-data)
        video-data-checks (validate-video-data session-data video-data)
        ]

  (merge
   { :session-id session-id
     :video-data video-data
     :video-data-valid (not (some error? video-data-checks))
     :video-data-checks video-data-checks
  })))


;; filters

(defn filter-by-name [name] (fn [r] (= (r :name) name)))
(def video-duration-update? (filter-by-name "videoDurationUpdate"))
(def video-play-attempted? (filter-by-name "videoPlayAttempted"))
(defn false-duration? [duration] (contains? [0 1 100 300 6000] duration))


(defn extract-video-data [session-data]
  (let [
        durations (map :duration (filter video-duration-update? session-data))
        max-duration (apply max (remove false-duration? durations))
        segments (map to-segments session-data)
        segments-duration (reduce
                           (fn [acc [from to]] (+ acc (- to from)))
                           0
                           (partition 2 (mapcat second segments)))

        time-stamps (map :timestamp session-data)
        total-playing-time (- (apply max time-stamps) (apply min time-stamps))
       ]

      {
       :duration max-duration
       :segments-duration segments-duration
       :total-playing-time total-playing-time
       :segments segments
       :video-play-attempted (some video-play-attempted? session-data)
      }
   )
)

(defn validate-video-data [session-data {:keys [durations total-playing-time segments-duration] }]
  (let [
        diff (Math/abs (- total-playing-time segments-duration)) ;; diff between total-playing-time and recorded segments
       ]

  (remove empty? [
       (when (some false-duration? durations)
         {:type :warning
          :message (str "Session contains false durations.") })

       (when (> (/  diff total-playing-time MAX_PLAYING_DIFF))
         {:type :error
          :message (str "Total playing time " total-playing-time " and " segments-duration " differ more than " (* 100 MAX_PLAYING_DIFF) "%")})
   ])))




;; converts record to (from) or (from, to) segment if applicable
(defn to-segments [record]
  (let [event-name (:name record)]
   (case event-name

     "videoPlaying"   [event-name  [(:position record)]]
     "videoFinished"  [event-name  [(:position record)]]
     "videoPaused"    [event-name  [(:position record)]]
     "videoScrubbing" [event-name  [(:from record) (:to record)]]
     (list)
   ))
)


;; ----------------------------


(parse-session session)





