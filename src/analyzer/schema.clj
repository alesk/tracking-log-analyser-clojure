(ns analyzer.schema
  (:require [schema.core :as s])
  )

(def GeoIp {
            :lng (s/maybe s/Num)
            :lat (s/maybe s/Num)
            :timeZone (s/maybe s/Str)
            :areaCode (s/maybe s/Str)
            :metroCode (s/maybe s/Str)
            :regionName (s/maybe s/Str)
            :regionCode (s/maybe s/Str)
            :postalCode (s/maybe s/Str)
            :countryName (s/maybe s/Str)
            :countryCode (s/maybe s/Str)
            :city (s/maybe s/Str)
            })
(def ExperimentTag {
                    :random java.lang.Boolean
                    :variant s/Str
                    :name s/Str
                    :scope s/Str
                    })

(def LogRecord
  "Celtra's log received from runner."
  {
   :name s/Str
   :receiver s/Str
   :receiverHostname s/Str
   :sessionId s/Str
   :timestamp (s/either s/Num s/Str)

   (s/optional-key :clientTimestamp) (s/maybe (s/either s/Num s/Str))
   (s/optional-key :index) s/Int
   (s/optional-key :instantiation) s/Str
   (s/optional-key :objectClazz) (s/maybe s/Str)
   (s/optional-key :objectLocalId) (s/maybe s/Int)
   (s/optional-key :objectName) (s/maybe s/Str)
   (s/optional-key :position) s/Num

   (s/optional-key :initiationTimestamp) s/Num

   (s/optional-key :screenIsMaster) java.lang.Boolean
   (s/optional-key :screenLocalId) s/Int
   (s/optional-key :screenTitle) s/Str

   (s/optional-key :url) s/Str
   (s/optional-key :duration) s/Num
   (s/optional-key :to) s/Num
   (s/optional-key :source) s/Str
   (s/optional-key :sourceType) s/Str
   (s/optional-key :label) s/Str
   (s/optional-key :videoPlayerMode) s/Str
   (s/optional-key :from) s/Num
   (s/optional-key :gpsPassed) java.lang.Boolean
   (s/optional-key :xForwardedFor) (s/maybe s/Str)
   (s/optional-key :purpose) s/Str
   (s/optional-key :referrer) s/Str
   (s/optional-key :ip) s/Str
   (s/optional-key :geoip) GeoIp
   (s/optional-key :version) s/Int
   (s/optional-key :externalSiteId) (s/maybe s/Str)

   (s/optional-key :first) java.lang.Boolean
   (s/optional-key :sdk) s/Str
   (s/optional-key :userAgent) s/Str
   (s/optional-key :unitName) s/Str
   (s/optional-key :videoSession) s/Str
   (s/optional-key :path) s/Str
   (s/optional-key :creativeId) s/Str
   (s/optional-key :creativeVersion) s/Str
   (s/optional-key :externalPlacementId) (s/maybe s/Str)
   (s/optional-key :externalPlacementName) (s/maybe s/Str)
   (s/optional-key :externalCreativeName) (s/maybe s/Str)
   (s/optional-key :externalCreativeId) (s/maybe s/Str)
   (s/optional-key :experimentTags) [ExperimentTag]
   (s/optional-key :placementId) (s/maybe s/Str)
   (s/optional-key :externalSiteName) (s/maybe s/Str)
   (s/optional-key :externalAdServer) (s/maybe s/Str)
   (s/optional-key :loadAvg) s/Num
   (s/optional-key :customSegments) {}

   })
