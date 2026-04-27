(ns
 zulipdata-book.patient-community-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l27 (def excluded-channels #{"slack-archive"}))


(def
 v4_l29
 (def
  all-messages
  (->>
   (pull/pull-public-channels!)
   (remove (fn [[ch _]] (or (= :not-found ch) (excluded-channels ch))))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v5_l35
 (def
  timeline
  (-> (anon/anonymized-timeline all-messages) nar/with-time-columns)))


(def
 v7_l42
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
 v8_l51
 (def
  cluster
  (->
   timeline
   (tc/select-rows
    (fn* [p1__70051#] (scicloj-channels (:channel p1__70051#)))))))


(def v9_l55 (tc/row-count cluster))


(deftest t10_l57 (is (>= v9_l55 50000)))


(def
 v12_l70
 (def
  backbone-users
  (let
   [per-user
    (->>
     (tc/rows cluster :as-maps)
     (group-by :user-key)
     (map
      (fn
       [[u msgs]]
       (let
        [yrs (set (map :year msgs))]
        {:user-key u,
         :messages (count msgs),
         :channels (count (set (map :channel msgs))),
         :years (count yrs),
         :span (- (apply max yrs) (apply min yrs))}))))]
   (->>
    per-user
    (filter
     (fn*
      [p1__70052#]
      (and (>= (:span p1__70052#) 5) (>= (:channels p1__70052#) 5))))
    (sort-by (fn [u] (* (:channels u) (:span u))) >)
    (take 15)))))


(def v13_l86 (def backbone-set (set (map :user-key backbone-users))))


(def
 v14_l89
 (def
  backbone-monthly
  (->
   cluster
   (tc/select-rows
    (fn* [p1__70053#] (backbone-set (:user-key p1__70053#))))
   (tc/group-by [:user-key :month-date])
   (tc/aggregate {:msgs tc/row-count}))))


(def
 v16_l100
 (def
  presence-by-user
  (->>
   (tc/rows backbone-monthly :as-maps)
   (group-by :user-key)
   (into {} (map (fn [[u rs]] [u (count rs)]))))))


(def
 v17_l105
 (def
  backbone-rank
  (let
   [sorted-users
    (->>
     backbone-set
     (sort-by (fn* [p1__70054#] (- (presence-by-user p1__70054# 0)))))]
   (zipmap sorted-users (range)))))


(def
 v18_l110
 (def
  backbone-display
  (->
   backbone-monthly
   (tc/add-column
    :user-rank
    (fn [ds] (mapv backbone-rank (:user-key ds))))
   (tc/add-column
    :log-msgs
    (fn
     [ds]
     (mapv (fn* [p1__70055#] (Math/log10 p1__70055#)) (:msgs ds)))))))


(def
 v19_l117
 (->
  backbone-display
  (pj/lay-tile :month-date :user-rank {:fill :log-msgs})
  (pj/options
   {:width 900,
    :height 380,
    :y-label "backbone rank (0 = most consistent)",
    :color-label "log10(messages)"})
  pj/plot))


(def
 v21_l126
 (->
  backbone-users
  (->>
   (sort-by (comp - presence-by-user :user-key))
   (take 5)
   (map
    (fn
     [u]
     {:user-key (:user-key u),
      :active-months (presence-by-user (:user-key u)),
      :channels (:channels u),
      :messages (:messages u)})))
  tc/dataset))


(def
 v23_l149
 (def
  covid-monthly
  (->
   cluster
   (tc/select-rows
    (fn* [p1__70056#] (= "covid-19" (:channel p1__70056#))))
   (tc/group-by [:month-date])
   (tc/aggregate
    {:msgs tc/row-count,
     :users (fn [ds] (count (distinct (:user-key ds))))})
   (tc/order-by [:month-date]))))


(def v24_l157 covid-monthly)


(def
 v26_l163
 (def
  ds-around-covid
  (->
   cluster
   (tc/select-rows
    (fn* [p1__70057#] (= "data-science" (:channel p1__70057#))))
   (tc/group-by [:month-date])
   (tc/aggregate {:msgs tc/row-count})
   (tc/select-rows
    (fn
     [r]
     (let
      [d (:month-date r) y (.getYear d) m (.getMonthValue d)]
      (and (= y 2020) (<= m 8)))))
   (tc/order-by [:month-date]))))


(def
 v27_l176
 (->
  ds-around-covid
  (pj/lay-line :month-date :msgs)
  (pj/lay-point :month-date :msgs)
  (pj/options
   {:width 720,
    :height 280,
    :y-label "messages in data-science",
    :title "data-science, first half of 2020"})
  pj/plot))


(def
 v29_l192
 (def
  first-cluster-month-by-user
  (->>
   (tc/rows cluster :as-maps)
   (group-by :user-key)
   (into
    {}
    (map
     (fn
      [[u msgs]]
      [u (nar/ts->month-date (apply min (map :timestamp msgs)))]))))))


(def
 v30_l199
 (def
  gathering-moments
  (->
   (->>
    first-cluster-month-by-user
    vals
    frequencies
    (map (fn [[d n]] {:month-date d, :new-users n, :label (str d)}))
    tc/dataset)
   (tc/order-by [:new-users] [:desc])
   (tc/head 10))))


(def
 v31_l206
 (->
  gathering-moments
  (tc/order-by [:new-users] [:asc])
  (pj/lay-lollipop :label :new-users {:x-type :categorical})
  (pj/coord :flip)
  (pj/options
   {:width 720,
    :height 380,
    :x-label "first-time scicloj posters that month"})
  pj/plot))


(def
 v33_l217
 (tc/dataset
  [{:month "2021-11",
    :new-users 63,
    :note "nov21-workshops launched (97 total in 4 months)"}
   {:month "2020-03", :new-users 30, :note "covid-19 channel opened"}
   {:month "2025-05",
    :new-users 22,
    :note "scinoj-light-1 conference (channel peak month)"}
   {:month "2021-02",
    :new-users 20,
    :note "tech.ml.dataset peak; sicmutils peak"}
   {:month "2021-08",
    :new-users 17,
    :note "study-groups era (sci-fu, ml-study, sicmutils)"}
   {:month "2025-10",
    :new-users 17,
    :note "macroexpand-2025 conference"}
   {:month "2024-09", :new-users 16, :note "kindly-dev opened"}
   {:month "2020-11",
    :new-users 16,
    :note "sci-fu, ml-study born; events channel born"}
   {:month "2020-04", :new-users 14, :note "tech.ml.dataset.dev born"}
   {:month "2021-04", :new-users 13, :note "scicloj.ml-dev pickup"}]))


(def
 v35_l240
 (def
  dying-channels
  #{"saite-dev"
    "sicmutils"
    "r-interop"
    "sci-fu"
    "notespace-dev"
    "cljplot-dev"
    "ml-study"}))


(def
 v36_l244
 (def
  migration
  (->>
   (tc/rows cluster :as-maps)
   (group-by :user-key)
   (mapcat
    (fn
     [[u msgs]]
     (let
      [in-dying
       (filter
        (fn* [p1__70058#] (dying-channels (:channel p1__70058#)))
        msgs)]
      (when
       (>= (count in-dying) 5)
       (let
        [last-ts (apply max (map :timestamp in-dying))]
        (->>
         msgs
         (filter
          (fn*
           [p1__70059#]
           (and
            (not (dying-channels (:channel p1__70059#)))
            (> (:timestamp p1__70059#) last-ts))))
         (group-by :channel)
         (map
          (fn
           [[c rs]]
           {:user u, :destination c, :msgs (count rs)})))))))))))


(def
 v37_l259
 (def
  n-tracked
  (->>
   (tc/rows cluster :as-maps)
   (group-by :user-key)
   (filter
    (fn
     [[_ msgs]]
     (>=
      (count
       (filter
        (fn* [p1__70060#] (dying-channels (:channel p1__70060#)))
        msgs))
      5)))
   count)))


(def
 v38_l266
 (def n-continued (->> migration (map :user) distinct count)))


(def
 v40_l274
 (tc/dataset
  [{:metric "users with ≥5 messages in a dying channel",
    :value n-tracked}
   {:metric "those who continued posting elsewhere",
    :value n-continued}
   {:metric "continuation rate (%)",
    :value (Math/round (* 100.0 (/ n-continued n-tracked)))}]))


(def
 v42_l282
 (def
  migration-destinations
  (->>
   migration
   (group-by :destination)
   (map
    (fn
     [[c rs]]
     {:destination c,
      :users (count (distinct (map :user rs))),
      :messages-after (apply + (map :msgs rs))}))
   (sort-by :users >)
   (take 10))))


(def
 v43_l292
 (->
  migration-destinations
  tc/dataset
  (tc/order-by [:users] [:asc])
  (pj/lay-lollipop :destination :users)
  (pj/coord :flip)
  (pj/options
   {:width 720, :height 360, :x-label "veterans who landed here next"})
  pj/plot))


(def
 v45_l314
 (def
  yearly-joy
  (->
   cluster
   (tc/group-by [:year])
   (tc/aggregate
    {:msgs tc/row-count,
     :rxn-sum (fn [ds] (apply + (:reaction-count ds)))})
   (tc/add-column
    :rxn-per-msg
    (fn
     [ds]
     (mapv
      (fn* [p1__70061# p2__70062#] (double (/ p1__70061# p2__70062#)))
      (:rxn-sum ds)
      (:msgs ds))))
   (tc/order-by [:year])
   (tc/select-rows
    (fn* [p1__70063#] (<= 2019 (:year p1__70063#) 2025))))))


(def
 v46_l327
 (->
  yearly-joy
  (pj/lay-line :year :rxn-per-msg)
  (pj/lay-point :year :rxn-per-msg {:size 80})
  (pj/options
   {:width 720,
    :height 320,
    :y-label "reactions per message",
    :title "Cluster-wide engagement, by year"})
  pj/plot))


(def
 v48_l344
 (def
  joyful-channels
  (->>
   (tc/rows cluster :as-maps)
   (group-by :channel)
   (map
    (fn
     [[c msgs]]
     {:channel c,
      :messages (count msgs),
      :rxn-per-msg
      (double (/ (apply + (map :reaction-count msgs)) (count msgs))),
      :first-date
      (nar/ts->month-date (apply min (map :timestamp msgs)))}))
   (filter (fn* [p1__70064#] (>= (:messages p1__70064#) 200)))
   (sort-by :rxn-per-msg >)
   (take 10)
   (sort-by :first-date)
   tc/dataset)))


(def v49_l360 joyful-channels)
