(ns
 zulipdata-book.scicloj-story-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l26 (def excluded-channels #{"slack-archive"}))


(def
 v4_l28
 (def
  all-messages
  (->>
   (pull/pull-public-channels!)
   (remove (fn [[ch _]] (or (= :not-found ch) (excluded-channels ch))))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v5_l34
 (def
  timeline
  (-> (anon/anonymized-timeline all-messages) nar/with-time-columns)))


(def v6_l38 (tc/row-count timeline))


(deftest t7_l40 (is (= v6_l38 (count all-messages))))


(def
 v9_l57
 (def
  scicloj-channels
  (let
   [by-name
    (set
     (nar/channels-by-name-pattern
      timeline
      #"(?i)sci|^ml|^ds|noj|kindly|clay|tablecloth|tableplot|dataset|libpython|clojisr|^r-|fastmath|neanderthal|plot|^scinoj|tech\.ml|notespace|numeric|stats|visual|gen-|metaprob|^ds-"))
    by-users
    (set
     (nar/channels-by-shared-users
      timeline
      "data-science"
      :share
      0.4
      :min-msgs
      30
      :top-n
      30))]
   (clojure.set/union by-name by-users))))


(def
 v10_l66
 (def
  scicloj-timeline
  (->
   timeline
   (tc/select-rows
    (fn* [p1__69841#] (scicloj-channels (:channel p1__69841#)))))))


(def
 v11_l70
 (def scicloj-lifecycles (nar/channel-lifecycle scicloj-timeline)))


(def v12_l73 (tc/row-count scicloj-lifecycles))


(def
 v14_l82
 (def
  lifecycle-display
  (->
   scicloj-lifecycles
   (tc/add-column :rank (fn [ds] (range (tc/row-count ds))))
   (tc/add-column
    :log10-total
    (fn
     [ds]
     (mapv (fn* [p1__69842#] (Math/log10 p1__69842#)) (:total ds)))))))


(def
 v15_l90
 (->
  lifecycle-display
  (pj/lay-point :first-date :rank {:size :log10-total})
  (pj/options
   {:width 900,
    :height 720,
    :y-label "channels (ordered by birth)",
    :size-label "log10(messages)"})
  pj/plot))


(def
 v17_l99
 (->
  scicloj-lifecycles
  (tc/select-columns
   [:channel
    :first-date
    :last-date
    :total
    :active-months
    :distinct-users])))


(def
 v19_l113
 (->
  scicloj-lifecycles
  (tc/select-rows
   (fn* [p1__69843#] (<= (.getYear (:first-date p1__69843#)) 2019)))
  (tc/select-columns [:channel :first-date :total :distinct-users])
  (tc/order-by [:first-date])))


(def
 v21_l129
 (->
  scicloj-lifecycles
  (tc/select-rows
   (fn* [p1__69844#] (= 2020 (.getYear (:first-date p1__69844#)))))
  (tc/select-columns [:channel :first-date :total :distinct-users])
  (tc/order-by [:first-date])))


(def
 v23_l143
 (->
  scicloj-lifecycles
  (tc/select-rows
   (fn* [p1__69845#] (= 2021 (.getYear (:first-date p1__69845#)))))
  (tc/select-columns [:channel :first-date :total :distinct-users])
  (tc/order-by [:first-date])))


(def
 v25_l157
 (->
  scicloj-lifecycles
  (tc/select-rows
   (fn*
    [p1__69846#]
    (let
     [y (.getYear (:first-date p1__69846#))]
     (or
      (= y 2022)
      (= y 2023)
      (and
       (= y 2024)
       (<= (.getMonthValue (:first-date p1__69846#)) 6))))))
  (tc/select-columns [:channel :first-date :total :distinct-users])
  (tc/order-by [:first-date])))


(def
 v27_l172
 (->
  scicloj-lifecycles
  (tc/select-rows
   (fn*
    [p1__69847#]
    (let
     [d (:first-date p1__69847#)]
     (or
      (and (= 2024 (.getYear d)) (>= (.getMonthValue d) 7))
      (>= (.getYear d) 2025)))))
  (tc/select-columns [:channel :first-date :total :distinct-users])
  (tc/order-by [:first-date])))


(def
 v29_l186
 (def
  heatmap-data
  (let
   [order
    (-> scicloj-lifecycles (tc/order-by [:first-date]) :channel vec)
    rank-of
    (zipmap order (range))]
   (->
    scicloj-timeline
    (tc/group-by [:channel :year])
    (tc/aggregate {:msgs tc/row-count})
    (tc/add-column
     :channel-rank
     (fn [ds] (mapv rank-of (:channel ds))))
    (tc/add-column
     :log10-msgs
     (fn
      [ds]
      (mapv (fn* [p1__69848#] (Math/log10 p1__69848#)) (:msgs ds))))))))


(def
 v30_l198
 (->
  heatmap-data
  (pj/lay-tile :year :channel-rank {:fill :log10-msgs})
  (pj/options
   {:width 720,
    :height 720,
    :y-label "channels (ordered by birth date, 0 = earliest)",
    :color-label "log10(messages)"})
  pj/plot))


(def
 v32_l212
 (def
  first-scicloj-month-by-user
  (->>
   (tc/rows scicloj-timeline :as-maps)
   (group-by :user-key)
   (into
    {}
    (map
     (fn
      [[u msgs]]
      [u (nar/ts->month-date (apply min (map :timestamp msgs)))]))))))


(def
 v33_l219
 (def
  newcomer-cohorts
  (->
   (->>
    first-scicloj-month-by-user
    vals
    frequencies
    (map (fn [[d n]] {:month-date d, :new-users n}))
    tc/dataset)
   (tc/order-by [:month-date]))))


(def
 v34_l225
 (->
  newcomer-cohorts
  (pj/lay-line :month-date :new-users)
  (pj/lay-point :month-date :new-users)
  (pj/options {:width 900, :height 320})
  pj/plot))


(def
 v36_l233
 (-> newcomer-cohorts (tc/order-by [:new-users] [:desc]) (tc/head 5)))


(def
 v38_l247
 (nar/prior-channels-of-newcomers scicloj-timeline "noj-dev" "2022-04"))


(def
 v40_l252
 (nar/prior-channels-of-newcomers
  scicloj-timeline
  "clay-dev"
  "2024-01"))


(def
 v42_l257
 (nar/prior-channels-of-newcomers
  scicloj-timeline
  "kindly-dev"
  "2024-09"))


(def
 v44_l267
 (def
  cluster-summary
  (let
   [ds
    scicloj-timeline
    rows
    (tc/row-count ds)
    usrs
    (count (distinct (:user-key ds)))]
   (tc/dataset
    [{:metric "scicloj channels", :value (count scicloj-channels)}
     {:metric "messages", :value rows}
     {:metric "distinct contributors", :value usrs}]))))


(def v45_l276 cluster-summary)
