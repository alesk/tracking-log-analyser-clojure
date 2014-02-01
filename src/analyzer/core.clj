(ns analyzer.core
  (:require
   [chesire/core :as json]
   [chesire.strgin ]
   [clojure.java/io :as io])

  (:import [java.lang.Math])
)

(def MAX_PLAYING_DIFF 0.02) ;; how much can sum of segments differ from total-playing time
(def START_SEGMENT_EVENTS #{"videoPlaying", "videoScrubbing"})

(declare video-data to-segments)

(def file-name "var/spool/tracking-logs/2014-02-01_14-40_akotnik.events")

(def parsed (cheshire.core/parsed-seq (clojure.java.io/reader file-name) true))

(def sessions (group-by :sessionId (filter :videoSession parsed)))

(def session (second sessions))
(def session-data (second session))

(defn error? [r] (= (:type r) :error))

(defn parse-session [[session-id session-data]]
  (let [
        video-data (extract-video-data session-data)
        video-data-checks (validate-video-data session-data video-data)
        ]

   { :session-id session-id
     :video-data video-data
     :video-data-valid (not (some error? video-data-checks))
     :video-data-checks video-data-checks
  }))


;; filters

(defn filter-by-name [name] (fn [r] (= (r :name) name)))
(def video-duration-update? (filter-by-name "videoDurationUpdate"))
(def video-play-attempted? (filter-by-name "videoPlayAttempted"))
(def video-play-succeeded? (filter-by-name "videoPlaySucceeded"))
(def screen-shown? (filter-by-name "screenShown"))
(defn false-duration? [duration] (contains? [0 1 100 300 6000] duration))
(defn start-segment-event? [event-name] (START_SEGMENT_EVENTS event-name))


(defn extract-video-data [session-data]
  (let [
        durations (map :duration (filter video-duration-update? session-data))
        max-duration (apply max (remove false-duration? durations))
        segments (map to-segments (sort-by #(- (:clientTimestamp %)) session-data))
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


(defn validate-video-data [session-data {:keys [durations total-playing-time segments-duration segments] }]
  (let [
        video-play-attempted (filter video-play-attempted? session-data)
        screen-shown (filter screen-shown? session-data)
        diff (Math/abs (- total-playing-time segments-duration)) ;; diff between total-playing-time and recorded segments
       ]

  (remove empty? [

       (when (and video-play-attempted screen-shown (> (video-play-attempted :initiationTimestamp) (screen-shown :clinetTimestamp)))
         {:type :error
          :code :e001
          :message "Detected inline video session initiated before it's screen has been shown. Setting initiationTimestamp to clientTimestamp of videoPlayAttempted."
          })

       (when (not (some video-play-succeeded? session-data))
         {:type :error
          :code :e002
          :message "Session doesn't contain videoPlaySucceded event."
          })

       (when (not (start-segment-event? (:name (first segments))))
         {:type :error
          :code :e003
          :message (str "Segment must start with: " (clojure.string/join ", " START_SEGMENT_EVENTS))
          })


       (when (some false-duration? durations)
         {:type :error
          :code :e004
          :message (str "Session contains false durations.")
          })

       (when (> (/  diff total-playing-time MAX_PLAYING_DIFF))
         {:type :error
          :code :e005
          :message (str "Total playing time " total-playing-time " and " segments-duration " differ more than " (* 100 MAX_PLAYING_DIFF) "%")
          })

   ])))


;; ----------------------------
;; (parse-session session)



