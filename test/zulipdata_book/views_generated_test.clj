(ns
 zulipdata-book.views-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l37
 (def
  fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def
 v4_l40
 (def
  messages
  (->>
   (pull/pull-channels! fixture-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l45 (count messages))


(def v7_l53 (def timeline (views/messages-timeline messages)))


(def v8_l55 (tc/row-count timeline))


(deftest t9_l57 (is (= v8_l55 (count messages))))


(def v11_l62 (tc/column-names timeline))


(def
 v13_l66
 (->
  timeline
  (tc/select-columns
   [:id :instant :channel :sender :content-length :edited?])
  (tc/head 3)))


(def v15_l74 (-> timeline :instant first type))


(def v17_l79 (-> timeline (tc/select-rows :edited?) tc/row-count))


(def v19_l87 (def reactions (views/reactions-long messages)))


(def v20_l89 (tc/row-count reactions))


(def v21_l91 (tc/column-names reactions))


(def
 v23_l95
 (->
  reactions
  (tc/group-by [:emoji-name])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))


(def v25_l109 (def edits (views/edits-long messages)))


(def v26_l111 (tc/row-count edits))


(def v27_l113 (tc/column-names edits))


(def
 v29_l118
 (->
  edits
  (tc/select-columns [:message-id :edit-ts :edit-user-id])
  (tc/head 3)))


(def v31_l128 (def links (views/topic-links-long messages)))


(def v32_l130 (tc/row-count links))


(def v33_l132 (tc/column-names links))


(def
 v35_l136
 (->
  links
  (tc/add-column
   :host
   (fn
    [ds]
    (mapv
     (fn* [p1__108500#] (some-> p1__108500# (java.net.URI.) .getHost))
     (:link-url ds))))
  (tc/group-by [:host])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))
