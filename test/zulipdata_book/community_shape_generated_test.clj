(ns
 zulipdata-book.community-shape-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]])
 (:import [java.time Instant LocalDate ZoneOffset YearMonth]))


(def v3_l25 (def excluded-channels #{"slack-archive"}))


(def
 v4_l27
 (def
  all-messages
  (->>
   (pull/pull-public-channels!)
   (remove (fn [[ch _]] (or (= :not-found ch) (excluded-channels ch))))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v5_l33
 (def
  ts->year-month
  (fn
   [ts]
   (let
    [d (LocalDate/ofInstant (Instant/ofEpochSecond ts) ZoneOffset/UTC)]
    (str (YearMonth/of (.getYear d) (.getMonthValue d)))))))


(def
 v6_l38
 (def
  ts->year
  (fn
   [ts]
   (.getYear
    (LocalDate/ofInstant (Instant/ofEpochSecond ts) ZoneOffset/UTC)))))


(def
 v7_l42
 (def
  year-month->idx
  (fn
   [ym]
   (let
    [[y m] (clojure.string/split ym #"-")]
    (+ (* 12 (Integer/parseInt y)) (Integer/parseInt m))))))


(def
 v8_l47
 (def
  year-month->date
  (fn
   [ym]
   (let
    [[y m] (clojure.string/split ym #"-")]
    (LocalDate/of (Integer/parseInt y) (Integer/parseInt m) 1)))))


(def
 v9_l52
 (def current-year-month (str (YearMonth/now ZoneOffset/UTC))))


(def
 v10_l55
 (def
  timeline
  (->
   (anon/anonymized-timeline all-messages)
   (tc/add-column
    :year-month
    (fn [ds] (mapv ts->year-month (:timestamp ds))))
   (tc/add-column :year (fn [ds] (mapv ts->year (:timestamp ds)))))))


(def v11_l62 (tc/row-count timeline))


(deftest t12_l64 (is (= v11_l62 (count all-messages))))


(def
 v14_l70
 (def
  closed-months-timeline
  (->
   timeline
   (tc/select-rows
    (fn*
     [p1__69652#]
     (not= current-year-month (:year-month p1__69652#)))))))


(def
 v16_l84
 (def
  first-month-by-user
  (->>
   (tc/rows timeline :as-maps)
   (group-by :user-key)
   (into
    {}
    (map
     (fn
      [[u msgs]]
      [u (apply min-key year-month->idx (map :year-month msgs))]))))))


(def
 v17_l91
 (def
  bucket-of
  (fn
   [tenure-months]
   (cond
    (zero? tenure-months)
    "01: new (this month)"
    (<= tenure-months 3)
    "02: 1-3 months"
    (<= tenure-months 12)
    "03: 4-12 months"
    (<= tenure-months 36)
    "04: 1-3 years"
    :else
    "05: 3+ years"))))


(def
 v18_l100
 (def
  tenure-mix
  (->
   closed-months-timeline
   (tc/add-column
    :tenure-bucket
    (fn
     [ds]
     (mapv
      (fn
       [now u]
       (bucket-of
        (-
         (year-month->idx now)
         (year-month->idx (first-month-by-user u)))))
      (:year-month ds)
      (:user-key ds))))
   (tc/group-by [:year-month :tenure-bucket])
   (tc/aggregate {:msgs tc/row-count})
   (tc/add-column
    :month-date
    (fn [ds] (mapv year-month->date (:year-month ds))))
   (tc/order-by [:month-date :tenure-bucket]))))


(def
 v19_l113
 (->
  tenure-mix
  (pj/lay-area
   :month-date
   :msgs
   {:color :tenure-bucket, :position :stack})
  (pj/options {:width 900, :height 420})
  pj/plot))


(def
 v21_l129
 (def
  monthly-engagement
  (->
   closed-months-timeline
   (tc/group-by [:year-month])
   (tc/aggregate
    {:msgs tc/row-count,
     :reactions-sum (fn [ds] (apply + (:reaction-count ds))),
     :length-sum (fn [ds] (apply + (:content-length ds)))})
   (tc/add-column
    :reactions-per-msg
    (fn
     [ds]
     (mapv
      (fn* [p1__69653# p2__69654#] (double (/ p1__69653# p2__69654#)))
      (:reactions-sum ds)
      (:msgs ds))))
   (tc/add-column
    :avg-length
    (fn
     [ds]
     (mapv
      (fn* [p1__69655# p2__69656#] (double (/ p1__69655# p2__69656#)))
      (:length-sum ds)
      (:msgs ds))))
   (tc/add-column
    :month-date
    (fn [ds] (mapv year-month->date (:year-month ds))))
   (tc/order-by [:month-date]))))


(def
 v23_l147
 (->
  monthly-engagement
  (pj/lay-line :month-date :reactions-per-msg)
  (pj/lay-smooth :month-date :reactions-per-msg)
  (pj/options {:width 900, :height 320})
  pj/plot))


(def
 v25_l156
 (->
  monthly-engagement
  (pj/lay-line :month-date :avg-length)
  (pj/lay-smooth :month-date :avg-length)
  (pj/options {:width 900, :height 320})
  pj/plot))


(def
 v27_l165
 (->
  monthly-engagement
  (tc/select-rows
   (fn*
    [p1__69657#]
    (re-matches #"\d{4}-06" (:year-month p1__69657#))))
  (tc/select-columns
   [:year-month :msgs :reactions-per-msg :avg-length])
  (tc/order-by [:year-month])))


(def
 v29_l177
 (def
  per-user-summary
  (->>
   (tc/rows timeline :as-maps)
   (group-by :user-key)
   (into
    []
    (map
     (fn
      [[u msgs]]
      (let
       [years (set (map :year msgs))]
       {:user-key u,
        :messages (count msgs),
        :channels (count (set (map :channel msgs))),
        :first-yr (apply min years),
        :last-yr (apply max years),
        :span-yrs (- (apply max years) (apply min years))})))))))


(def
 v30_l190
 (def
  ring-users
  (->>
   per-user-summary
   (sort-by (fn [u] (* (:channels u) (:span-yrs u))) >)
   (take 15)
   (map :user-key)
   set)))


(def
 v31_l197
 (def
  ring-by-year
  (->
   timeline
   (tc/select-rows
    (fn* [p1__69658#] (ring-users (:user-key p1__69658#))))
   (tc/group-by [:user-key :year])
   (tc/aggregate {:msgs tc/row-count}))))


(def
 v33_l206
 (def
  ring-user-order
  (->>
   per-user-summary
   (filter (fn* [p1__69659#] (ring-users (:user-key p1__69659#))))
   (sort-by :messages)
   (mapv :user-key))))


(def
 v34_l212
 (def
  ring-display
  (->
   ring-by-year
   (tc/add-column
    :user-rank
    (fn
     [ds]
     (mapv
      (fn* [p1__69660#] (.indexOf ring-user-order p1__69660#))
      (:user-key ds)))))))


(def
 v35_l217
 (->
  ring-display
  (pj/lay-point :year :user-rank {:size :msgs})
  (pj/options {:width 900, :height 480})
  pj/plot))


(def
 v37_l230
 (->
  (tc/dataset
   (mapv
    (fn
     [i k]
     (let
      [u
       (first
        (filter
         (fn* [p1__69661#] (= k (:user-key p1__69661#)))
         per-user-summary))]
      {:rank i,
       :user-key k,
       :messages (:messages u),
       :channels (:channels u),
       :first-yr (:first-yr u),
       :last-yr (:last-yr u)}))
    (range)
    ring-user-order))
  (tc/order-by [:rank] [:desc])))
