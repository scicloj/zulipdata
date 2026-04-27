(ns
 zulipdata-book.channels-overview-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l20 (def channels-data (pull/pull-public-channels!)))


(def
 v5_l25
 (def
  streams-by-name
  (->>
   (client/get-streams)
   :streams
   (map (juxt :name identity))
   (into {}))))


(def v7_l35 (def excluded-channels #{"slack-archive"}))


(def
 v8_l37
 (def
  epoch->date
  (fn
   [ts]
   (str
    (java.time.LocalDate/ofInstant
     (java.time.Instant/ofEpochSecond ts)
     java.time.ZoneOffset/UTC)))))


(def
 v9_l43
 (def
  channel-summary
  (->
   (for
    [[ch r]
     (dissoc channels-data :not-found)
     :when
     (not (excluded-channels ch))
     :let
     [ms (pull/all-messages r) ts (seq (map :timestamp ms))]]
    {:channel ch,
     :web-public? (boolean (:is_web_public (streams-by-name ch))),
     :messages (count ms),
     :earliest (when ts (epoch->date (apply min ts))),
     :latest (when ts (epoch->date (apply max ts)))})
   tc/dataset
   (tc/order-by [:messages] [:desc]))))


(def v10_l56 (tc/row-count channel-summary))


(deftest
 t11_l58
 (is
  (=
   v10_l56
   (- (count (pull/public-channel-names)) (count excluded-channels)))))


(def v13_l64 (def total-channels (tc/row-count channel-summary)))


(def
 v14_l65
 (def total-messages (reduce + (:messages channel-summary))))


(def
 v15_l66
 (def
  web-public-cnt
  (->
   channel-summary
   (tc/select-rows (comp boolean :web-public?))
   tc/row-count)))


(def
 v16_l69
 (def
  empty-channels
  (->
   channel-summary
   (tc/select-rows (fn* [p1__67021#] (zero? (:messages p1__67021#))))
   tc/row-count)))


(def
 v17_l73
 (tc/dataset
  [{:metric "total channels", :value total-channels}
   {:metric "  of which web-public", :value web-public-cnt}
   {:metric "  with zero messages", :value empty-channels}
   {:metric "total messages", :value total-messages}]))


(def v19_l81 (def top-20 (-> channel-summary (tc/head 20))))


(def v20_l85 top-20)


(def
 v21_l87
 (->
  top-20
  (tc/order-by [:messages] [:asc])
  (pj/lay-value-bar :channel :messages)
  (pj/coord :flip)
  (pj/options {:width 720, :height 520})
  pj/plot))


(def
 v23_l100
 (def
  ranked
  (let
   [non-empty
    (->
     channel-summary
     (tc/select-rows
      (fn* [p1__67022#] (pos? (:messages p1__67022#)))))]
   (->
    non-empty
    (tc/add-column :rank (range 1 (inc (tc/row-count non-empty))))))))


(def
 v24_l106
 (->
  ranked
  (pj/lay-line :rank :messages)
  (pj/scale :y :log)
  (pj/options {:width 720, :height 360})
  pj/plot))


(def
 v26_l117
 (def today (java.time.LocalDate/now java.time.ZoneOffset/UTC)))


(def
 v27_l119
 (def
  bucket-of
  (fn
   [days]
   (cond
    (nil? days)
    "no messages"
    (<= days 30)
    "≤ 30 days"
    (<= days 90)
    "≤ 90 days"
    (<= days 365)
    "≤ 365 days"
    :else
    "> 365 days"))))


(def
 v28_l128
 (def
  bucket-order
  ["≤ 30 days" "≤ 90 days" "≤ 365 days" "> 365 days" "no messages"]))


(def
 v29_l131
 (def
  recency
  (->
   channel-summary
   (tc/add-column
    :days-since
    (fn
     [ds]
     (mapv
      (fn
       [latest]
       (when
        latest
        (.between
         java.time.temporal.ChronoUnit/DAYS
         (java.time.LocalDate/parse latest)
         today)))
      (:latest ds))))
   (tc/add-column :bucket (fn [ds] (mapv bucket-of (:days-since ds))))
   (tc/group-by [:bucket])
   (tc/aggregate {:channels tc/row-count})
   (tc/order-by
    [:bucket]
    [(fn
      [a b]
      (compare
       (.indexOf bucket-order a)
       (.indexOf bucket-order b)))]))))


(def v30_l149 recency)


(def
 v31_l151
 (->
  recency
  (pj/lay-value-bar :bucket :channels)
  (pj/options {:width 720, :height 360})
  pj/plot))


(def
 v33_l160
 (def
  web-public-summary
  (->
   channel-summary
   (tc/select-rows (comp boolean :web-public?))
   (tc/select-columns [:channel :messages :earliest :latest]))))


(def v34_l165 web-public-summary)
