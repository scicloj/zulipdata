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
 v3_l35
 (def
  fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def
 v4_l38
 (def
  messages
  (->>
   (pull/pull-channels! fixture-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l43 (count messages))


(def v6_l45 (def base-timeline (anon/anonymized-timeline messages)))


(def v7_l48 (tc/row-count base-timeline))


(deftest t8_l50 (is (= v7_l48 (count messages))))


(def v10_l64 (def timeline (nar/with-time-columns base-timeline)))


(def v11_l66 (tc/column-names timeline))


(def
 v13_l70
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/head 3)))


(def
 v15_l78
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v17_l92 (def lifecycles (nar/channel-lifecycle timeline)))


(def v18_l94 lifecycles)


(def v20_l99 (tc/row-count lifecycles))


(deftest
 t21_l101
 (is (= v20_l99 (-> timeline :channel distinct count))))


(def v23_l111 (nar/channels-by-name-pattern timeline #"clay|tableplot"))


(def
 v25_l125
 (nar/channels-by-shared-users
  timeline
  "clay-dev"
  :share
  0.4
  :min-msgs
  30
  :top-n
  30))


(def v27_l134 (nar/first-posters-of-channel timeline "kindly-dev" 5))


(def
 v29_l150
 (nar/prior-channels-of-newcomers timeline "kindly-dev" "2024-09"))


(def
 v31_l159
 (def
  kindly-monthly
  (nar/channel-monthly-activity timeline #{"kindly-dev"})))


(def v32_l162 (tc/head kindly-monthly 3))


(def v34_l166 (reduce + (:msgs kindly-monthly)))


(deftest
 t35_l168
 (is
  (=
   v34_l166
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__48942#] (= "kindly-dev" (:channel p1__48942#))))
    :total
    first))))
