(ns analyzer.transform
  (:require [schema.core :as s]
            [analyzer.schema :refer [GeoIp LogRecord]])

  (:import [java.lang.Math]
           [java.lang.Boolean])
)



(declare video-data to-segments extract-video-data  error?)

(def MAX_PLAYING_DIFF 0.02) ;; how much can sum of segments differ from total-playing time
(def START_SEGMENT_EVENTS #{"videoPlaying", "videoScrubbing"})


;; transforms session data

(defn transform-session [session-data]

  (let [
        video-data (extract-video-data session-data)
        ]


    (s/validate [LogRecord] session-data)
    (merge {:user-agent (first (remove empty? (map :userAgent session-data)))}
     video-data)
  ))



;; utils
(defn max-or-nil [xs]
  (if (empty? xs) nil (apply clojure.core/max xs)))

;; filters

(defn filter-by-name [name] (fn [r] (= (r :name) name)))
(def video-duration-update? (filter-by-name "videoDurationUpdate"))
(defn false-duration? [duration] (contains? [0 1 100 300 6000] duration))
(defn start-segment-event? [event-name] (START_SEGMENT_EVENTS event-name))
(defn error? [r] (= (:type r) :error))

(defn first-of-name [name records] (first (filter #(= (:name %) name) records)))

(defn extract-timestamp [record]
  (let [timestamp (:timestamp record)]

    (or
      (nil? timestamp) 0
      (string? timestamp) (- (read-string timestamp))
      (- timestamp)
      )
  )
  )

(defn extract-video-data [session-data]

  (let [
        durations (map :duration (filter video-duration-update? session-data))
        max-duration (max-or-nil (remove false-duration? durations))
        segments (remove empty? (map to-segments (sort-by #(- (extract-timestamp %)) session-data)))
        segments-duration (reduce
                           (fn [acc [from to]] (+ acc (- to from)))
                           0
                           (partition 2 (mapcat second segments)))

        time-stamps (map extract-timestamp session-data)
        total-playing-time (- (apply max time-stamps) (apply min time-stamps))
        video-play-attempted (first-of-name "videoPlayAttempted" session-data)
        video-play-succeeded (first-of-name "videoPlaySucceeded" session-data)
        screen-shown (first-of-name "screenShown" session-data)
       ]

      {

       :screen-show-at (:clinetTimestamp screen-shown)
       :video-play-attempted-at (:initiationTimestamp video-play-attempted)
       :video-play-succeeded-at (:clinetTimestamp video-play-succeeded)
       :duration max-duration
       :segments-duration segments-duration
       :total-playing-time total-playing-time
       :segments segments
      }
   )
)

;; converts record to (from) or (from, to) segment if applicable
(defn to-segments [record]
  (let [event-name (:name record)]
   (case event-name

     "videoPlaying"   [event-name  [(:position record)]]
     "videoFinished"  [event-name  [(:position record)]]
     "videoPaused"    [event-name  [(:position record)]]
     "videoScrubbing" [event-name  [(:from record) (:to record)]]
     nil
   ))
)


