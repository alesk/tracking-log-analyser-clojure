(ns analyzer.validate
  (:require [analyzer.transform :refer [
                                        start-segment-event?
                                        START_SEGMENT_EVENTS
                                        false-duration?
                                        MAX_PLAYING_DIFF]])
  (:import [java.lang.Math])

  )

(defn validate-session2 [session-data]
  (println session-data))

(defn validate-session [{:keys [
                                  screen-shown-at
                                  video-play-attempted-at
                                  video-play-succeeded-at
                                  durations
                                  total-playing-time
                                  segments-duration
                                  segments
                                ] :as session-data}]
  (let [
        diff (Math/abs (- total-playing-time segments-duration)) ;; diff between total-playing-time and recorded segments
       ]

  (remove empty? [

       (when (and
              (number? video-play-attempted-at)
              (number? screen-shown-at)
              (> video-play-attempted-at screen-shown-at))
         {:type :error
          :code :e001
          :message "Detected inline video session initiated before it's screen has been shown. Setting initiationTimestamp to clientTimestamp of videoPlayAttempted."
          })

       (when (nil? video-play-succeeded-at)
         {:type :error
          :code :e002
          :message "Session doesn't contain videoPlaySucceeded event."
          })

       (when (not (start-segment-event? (first (first segments))))
         {:type :error
          :code :e003
          :message (str "Segment must start with: " (clojure.string/join ", " START_SEGMENT_EVENTS) " -- " (first (first segments)))
          })


       (when (some false-duration? durations)
         {:type :error
          :code :e004
          :message (str "Session contains false durations.")
          })

       (when (and (> total-playing-time 0) (> (/  diff total-playing-time MAX_PLAYING_DIFF)))
         {:type :error
          :code :e005
          :message (str "Total playing time " total-playing-time " and " segments-duration " differ more than " (* 100 MAX_PLAYING_DIFF) "%")
          })

   ])))
