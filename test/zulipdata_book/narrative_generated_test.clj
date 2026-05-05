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


(def v7_l48 (tc/row-count base-timeline))


(deftest t8_l50 (is (= v7_l48 (count messages))))


(def v10_l64 (def timeline (nar/with-time-columns base-timeline)))


(def v11_l66 (-> timeline tc/column-names sort))


(def
 v12_l68
 (every?
  (set (tc/column-names timeline))
  [:month-date :year-month :year]))


(deftest t13_l71 (is (= v12_l68 true)))


(def
 v15_l76
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/order-by :timestamp :desc)
  (tc/head 5)))


(def
 v17_l85
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v19_l99 (def lifecycles (nar/channel-lifecycle timeline)))


(def v20_l101 lifecycles)


(def v22_l106 (tc/row-count lifecycles))


(deftest
 t23_l108
 (is (= v22_l106 (-> timeline :channel distinct count))))


(def
 v25_l118
 (nar/channels-by-name-pattern timeline #"civitas|gratitude"))


(deftest t26_l120 (is (= v25_l118 ["clojurecivitas" "gratitude"])))


(def
 v28_l135
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
 t29_l138
 (is (= v28_l135 ["clojurecivitas" "events" "scicloj-webpublic"])))


(def
 v31_l147
 (def
  civitas-first-posters
  (nar/first-posters-of-channel timeline "clojurecivitas" 5)))


(def v32_l150 civitas-first-posters)


(def v33_l152 (tc/row-count civitas-first-posters))


(deftest t34_l154 (is (= v33_l152 5)))


(def
 v36_l170
 (nar/prior-channels-of-newcomers timeline "clojurecivitas" "2025-10"))


(def
 v38_l179
 (def
  civitas-monthly
  (nar/channel-monthly-activity timeline #{"clojurecivitas"})))


(def v39_l182 civitas-monthly)


(def v41_l186 (reduce + (:msgs civitas-monthly)))


(deftest
 t42_l188
 (is
  (=
   v41_l186
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__40165#] (= "clojurecivitas" (:channel p1__40165#))))
    :total
    first))))
