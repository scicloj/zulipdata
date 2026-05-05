(ns
 zulipdata-book.narrative-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l40 (def sample-channels (pull/web-public-channel-names)))


(def
 v4_l43
 (def
  messages
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l48 (count messages))


(def v6_l50 (def base-timeline (anon/anonymized-timeline messages)))


(def v7_l53 base-timeline)


(def v8_l55 (tc/row-count base-timeline))


(deftest t9_l57 (is (= v8_l55 (count messages))))


(def v11_l71 (def timeline (nar/with-time-columns base-timeline)))


(def v12_l73 (-> timeline tc/column-names sort))


(def
 v13_l75
 (every?
  (set (tc/column-names timeline))
  [:month-date :year-month :year]))


(deftest t14_l78 (is (= v13_l75 true)))


(def
 v16_l83
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/order-by :timestamp :desc)
  (tc/head 5)))


(def
 v18_l92
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v20_l106 (def lifecycles (nar/channel-lifecycle timeline)))


(def v21_l108 lifecycles)


(def v23_l113 (tc/row-count lifecycles))


(deftest
 t24_l115
 (is (= v23_l113 (-> timeline :channel distinct count))))


(def
 v26_l125
 (nar/channels-by-name-pattern timeline #"civitas|gratitude"))


(deftest t27_l127 (is (= v26_l125 ["clojurecivitas" "gratitude"])))


(def
 v29_l142
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
 t30_l145
 (is (= v29_l142 ["clojurecivitas" "events" "scicloj-webpublic"])))


(def
 v32_l154
 (def
  civitas-first-posters
  (nar/first-posters-of-channel timeline "clojurecivitas" 5)))


(def v33_l157 civitas-first-posters)


(def v34_l159 (tc/row-count civitas-first-posters))


(deftest t35_l161 (is (= v34_l159 5)))


(def
 v37_l177
 (nar/prior-channels-of-newcomers timeline "clojurecivitas" "2025-10"))


(def
 v39_l186
 (def
  civitas-monthly
  (nar/channel-monthly-activity timeline #{"clojurecivitas"})))


(def v40_l189 civitas-monthly)


(def v42_l193 (reduce + (:msgs civitas-monthly)))


(deftest
 t43_l195
 (is
  (=
   v42_l193
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__51260#] (= "clojurecivitas" (:channel p1__51260#))))
    :total
    first))))
