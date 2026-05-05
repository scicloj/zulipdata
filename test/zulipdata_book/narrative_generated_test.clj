(ns
 zulipdata-book.narrative-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l35 (def sample-channels (pull/web-public-channel-names)))


(def
 v4_l38
 (def
  messages
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l43 (count messages))


(def v6_l45 (def base-timeline (anon/anonymized-timeline messages)))


(def v7_l48 base-timeline)


(def v8_l50 (tc/row-count base-timeline))


(deftest t9_l52 (is (= v8_l50 (count messages))))


(def v11_l66 (def timeline (nar/with-time-columns base-timeline)))


(def v12_l68 (-> timeline tc/column-names sort))


(def
 v13_l70
 (every?
  (set (tc/column-names timeline))
  [:month-date :year-month :year]))


(deftest t14_l73 (is (= v13_l70 true)))


(def
 v16_l78
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/order-by :timestamp :desc)
  (tc/head 5)))


(def
 v18_l87
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v20_l101 (def lifecycles (nar/channel-lifecycle timeline)))


(def v21_l103 lifecycles)


(def v23_l108 (tc/row-count lifecycles))


(deftest
 t24_l110
 (is (= v23_l108 (-> timeline :channel distinct count))))


(def
 v26_l120
 (nar/channels-by-name-pattern timeline #"civitas|gratitude"))


(deftest t27_l122 (is (= v26_l120 ["clojurecivitas" "gratitude"])))


(def
 v29_l137
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
 t30_l140
 (is (= v29_l137 ["clojurecivitas" "events" "scicloj-webpublic"])))


(def
 v32_l149
 (def
  civitas-first-posters
  (nar/first-posters-of-channel timeline "clojurecivitas" 5)))


(def v33_l152 civitas-first-posters)


(def v34_l154 (tc/row-count civitas-first-posters))


(deftest t35_l156 (is (= v34_l154 5)))


(def
 v37_l172
 (nar/prior-channels-of-newcomers timeline "clojurecivitas" "2025-10"))


(def
 v39_l181
 (def
  civitas-monthly
  (nar/channel-monthly-activity timeline #{"clojurecivitas"})))


(def v40_l184 civitas-monthly)


(def v42_l188 (reduce + (:msgs civitas-monthly)))


(deftest
 t43_l190
 (is
  (=
   v42_l188
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__56931#] (= "clojurecivitas" (:channel p1__56931#))))
    :total
    first))))
