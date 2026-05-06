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
  (-> (pull/pull-channels! sample-channels) pull/all-channel-messages)))


(def v5_l47 (count messages))


(def v6_l49 (def base-timeline (anon/anonymized-timeline messages)))


(def v7_l52 base-timeline)


(def v8_l54 (tc/row-count base-timeline))


(deftest t9_l56 (is (= v8_l54 (count messages))))


(def v11_l70 (def timeline (nar/with-time-columns base-timeline)))


(def v12_l72 (-> timeline tc/column-names sort))


(def
 v13_l74
 (every?
  (set (tc/column-names timeline))
  [:month-date :year-month :year]))


(deftest t14_l77 (is (= v13_l74 true)))


(def
 v16_l82
 (->
  timeline
  (tc/select-columns [:timestamp :month-date :year-month :year])
  (tc/order-by :timestamp :desc)
  (tc/head 5)))


(def
 v18_l91
 (let
  [ts (-> timeline :timestamp first)]
  {:ts ts,
   :month-date (nar/ts->month-date ts),
   :year-month (nar/ts->year-month ts),
   :year (nar/ts->year ts)}))


(def v20_l105 (def lifecycles (nar/channel-lifecycle timeline)))


(def v21_l107 lifecycles)


(def v23_l112 (tc/row-count lifecycles))


(deftest
 t24_l114
 (is (= v23_l112 (-> timeline :channel distinct count))))


(def
 v26_l124
 (nar/channels-by-name-pattern timeline #"civitas|gratitude"))


(deftest t27_l126 (is (= v26_l124 ["clojurecivitas" "gratitude"])))


(def
 v29_l141
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
 t30_l144
 (is (= v29_l141 ["clojurecivitas" "events" "scicloj-webpublic"])))


(def
 v32_l153
 (def
  civitas-first-posters
  (nar/first-posters-of-channel timeline "clojurecivitas" 5)))


(def v33_l156 civitas-first-posters)


(def v34_l158 (tc/row-count civitas-first-posters))


(deftest t35_l160 (is (= v34_l158 5)))


(def
 v37_l176
 (nar/prior-channels-of-newcomers timeline "clojurecivitas" "2025-10"))


(def
 v39_l185
 (def
  civitas-monthly
  (nar/channel-monthly-activity timeline #{"clojurecivitas"})))


(def v40_l188 civitas-monthly)


(def v42_l192 (reduce + (:msgs civitas-monthly)))


(deftest
 t43_l194
 (is
  (=
   v42_l192
   (->
    lifecycles
    (tc/select-rows
     (fn* [p1__48916#] (= "clojurecivitas" (:channel p1__48916#))))
    :total
    first))))
