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
 v3_l32
 (def
  fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def
 v4_l35
 (def
  messages
  (->>
   (pull/pull-channels! fixture-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l40 (count messages))


(def v6_l42 (def base-timeline (anon/anonymized-timeline messages)))


(def v7_l45 (tc/row-count base-timeline))


(deftest t8_l47 (is (= v7_l45 (count messages))))


(def v10_l61 (def timeline (nar/with-time-columns base-timeline)))


(def v11_l63 (tc/column-names timeline))


(def
 v13_l67
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/head 3)))


(def
 v15_l75
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v17_l89 (def lifecycles (nar/channel-lifecycle timeline)))


(def v18_l91 lifecycles)


(def v20_l96 (tc/row-count lifecycles))


(deftest t21_l98 (is (= v20_l96 (-> timeline :channel distinct count))))


(def v23_l108 (nar/channels-by-name-pattern timeline #"clay|tableplot"))


(def
 v25_l122
 (nar/channels-by-shared-users
  timeline
  "clay-dev"
  :share
  0.4
  :min-msgs
  30
  :top-n
  30))


(def v27_l131 (nar/first-posters-of-channel timeline "kindly-dev" 5))


(def
 v29_l147
 (nar/prior-channels-of-newcomers timeline "kindly-dev" "2024-09"))


(def
 v31_l156
 (def
  kindly-monthly
  (nar/channel-monthly-activity timeline #{"kindly-dev"})))


(def v32_l159 (tc/head kindly-monthly 3))


(def v34_l163 (reduce + (:msgs kindly-monthly)))


(deftest
 t35_l165
 (is
  (=
   v34_l163
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__55155#] (= "kindly-dev" (:channel p1__55155#))))
    :total
    first))))
