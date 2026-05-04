(ns
 zulipdata-book.narrative-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l37
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def
 v4_l40
 (def
  messages
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l45 (count messages))


(def v6_l47 (def base-timeline (anon/anonymized-timeline messages)))


(def v7_l50 (tc/row-count base-timeline))


(deftest t8_l52 (is (= v7_l50 (count messages))))


(def v10_l66 (def timeline (nar/with-time-columns base-timeline)))


(def v11_l68 (-> timeline tc/column-names sort))


(def
 v12_l70
 (every?
  (set (tc/column-names timeline))
  [:month-date :year-month :year]))


(deftest t13_l73 (is (= v12_l70 true)))


(def
 v15_l78
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/order-by :timestamp :desc)
  (tc/head 5)))


(def
 v17_l87
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v19_l101 (def lifecycles (nar/channel-lifecycle timeline)))


(def v20_l103 lifecycles)


(def v22_l108 (tc/row-count lifecycles))


(deftest
 t23_l110
 (is (= v22_l108 (-> timeline :channel distinct count))))


(def
 v25_l120
 (nar/channels-by-name-pattern timeline #"civitas|gratitude"))


(deftest t26_l122 (is (= v25_l120 ["clojurecivitas" "gratitude"])))


(def
 v28_l137
 (nar/channels-by-shared-users
  timeline
  "clojurecivitas"
  :share
  0.5
  :min-msgs
  5
  :top-n
  5))


(deftest
 t29_l140
 (is (= v28_l137 ["clojurecivitas" "events" "scicloj-webpublic"])))


(def
 v31_l149
 (def
  civitas-first-posters
  (nar/first-posters-of-channel timeline "clojurecivitas" 5)))


(def v32_l152 civitas-first-posters)


(def v33_l154 (tc/row-count civitas-first-posters))


(deftest t34_l156 (is (= v33_l154 5)))


(def
 v36_l172
 (nar/prior-channels-of-newcomers timeline "clojurecivitas" "2025-10"))


(def
 v38_l181
 (def
  civitas-monthly
  (nar/channel-monthly-activity timeline #{"clojurecivitas"})))


(def v39_l184 civitas-monthly)


(def v41_l188 (reduce + (:msgs civitas-monthly)))


(deftest
 t42_l190
 (is
  (=
   v41_l188
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__53537#] (= "clojurecivitas" (:channel p1__53537#))))
    :total
    first))))
