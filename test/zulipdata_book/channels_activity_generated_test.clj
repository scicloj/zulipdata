(ns
 zulipdata-book.channels-activity-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.plotje.api :as pj]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l18
 (def
  channels
  ["data-science"
   "noj-dev"
   "real-world-data"
   "tech.ml.dataset.dev"
   "kindly-dev"]))


(def v4_l22 (def channels-data (pull/pull-channels! channels)))


(def
 v6_l27
 (def
  all-raw-messages
  (mapcat
   (fn [[_ch r]] (pull/all-messages r))
   (dissoc channels-data :not-found))))


(def
 v8_l33
 (def
  overview
  (->
   (for
    [[ch r]
     (dissoc channels-data :not-found)
     :let
     [ms (pull/all-messages r) ts (map :timestamp ms)]]
    {:channel ch,
     :messages (count ms),
     :earliest
     (str
      (java.time.LocalDate/ofInstant
       (java.time.Instant/ofEpochSecond (apply min ts))
       java.time.ZoneOffset/UTC)),
     :latest
     (str
      (java.time.LocalDate/ofInstant
       (java.time.Instant/ofEpochSecond (apply max ts))
       java.time.ZoneOffset/UTC))})
   tc/dataset
   (tc/order-by [:messages] [:desc]))))


(def v9_l48 overview)


(def v11_l52 (tc/row-count overview))


(deftest t12_l54 (is (= v11_l52 (count channels))))


(def v14_l59 (def total-messages (reduce + (:messages overview))))


(def v15_l61 total-messages)


(deftest t16_l63 (is (= v15_l61 (count all-raw-messages))))


(def v18_l68 (def timeline (views/messages-timeline all-raw-messages)))


(def v19_l70 (tc/column-names timeline))


(def v21_l74 (tc/row-count timeline))


(deftest t22_l76 (is (= v21_l74 total-messages)))


(def
 v24_l83
 (def
  monthly-activity
  (->
   timeline
   (tc/add-column
    :month
    (fn
     [ds]
     (mapv
      (fn
       [ts]
       (let
        [d
         (java.time.LocalDate/ofInstant
          (java.time.Instant/ofEpochSecond ts)
          java.time.ZoneOffset/UTC)]
        (java.time.LocalDate/of (.getYear d) (.getMonthValue d) 1)))
      (:timestamp ds))))
   (tc/group-by [:channel :month])
   (tc/aggregate {:messages tc/row-count})
   (tc/order-by [:month :channel]))))


(def
 v25_l97
 (->
  monthly-activity
  (pj/lay-line :month :messages {:color :channel})
  (pj/options {:width 900, :height 420})
  pj/plot))


(def
 v27_l104
 (def
  top-senders
  (->
   timeline
   (tc/group-by [:sender])
   (tc/aggregate {:messages tc/row-count})
   (tc/order-by [:messages] [:desc])
   (tc/head 15))))


(def
 v28_l111
 (->
  top-senders
  (tc/order-by [:messages] [:asc])
  (pj/lay-value-bar :sender :messages)
  (pj/coord :flip)
  (pj/options {:width 720, :height 420})
  pj/plot))


(def v29_l118 top-senders)


(def
 v31_l122
 (def
  hourly-activity
  (->
   timeline
   (tc/add-column
    :hour
    (fn
     [ds]
     (mapv
      (fn
       [ts]
       (->
        (java.time.Instant/ofEpochSecond ts)
        (.atZone java.time.ZoneOffset/UTC)
        .getHour))
      (:timestamp ds))))
   (tc/group-by [:hour])
   (tc/aggregate {:messages tc/row-count})
   (tc/order-by [:hour]))))


(def
 v32_l135
 (->
  hourly-activity
  (pj/lay-value-bar :hour :messages {:x-type :categorical})
  (pj/options {:width 720, :height 360})
  pj/plot))


(def v34_l142 (def reactions (views/reactions-long all-raw-messages)))


(def v35_l144 (tc/row-count reactions))


(def
 v37_l148
 (def
  top-emojis
  (->
   reactions
   (tc/group-by [:emoji-name])
   (tc/aggregate {:count tc/row-count})
   (tc/order-by [:count] [:desc])
   (tc/head 15))))


(def
 v38_l155
 (->
  top-emojis
  (tc/order-by [:count] [:asc])
  (pj/lay-value-bar :emoji-name :count)
  (pj/coord :flip)
  (pj/options {:width 720, :height 420})
  pj/plot))


(def v39_l162 top-emojis)


(def
 v41_l166
 (def
  most-reacted
  (->
   reactions
   (tc/group-by [:message-id :channel :subject])
   (tc/aggregate {:reactions tc/row-count})
   (tc/order-by [:reactions] [:desc])
   (tc/head 15)
   (tc/left-join
    (-> timeline (tc/select-columns [:id :sender]))
    {:left :message-id, :right :id}))))


(def
 v42_l176
 (tc/select-columns
  most-reacted
  [:channel :subject :sender :reactions]))
