(ns
 zulipdata-book.views-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l38
 (def
  fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def
 v4_l41
 (def
  messages
  (->>
   (pull/pull-channels! fixture-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l46 (count messages))


(def v7_l57 (def timeline (views/messages-timeline messages)))


(def v8_l59 (tc/row-count timeline))


(deftest t9_l61 (is (= v8_l59 (count messages))))


(def v11_l66 (tc/column-names timeline))


(def
 v13_l70
 (->
  timeline
  (tc/select-columns
   [:id :instant :channel :sender :content-length :edited?])
  (tc/head 3)))


(def v15_l78 (-> timeline :instant first type))


(def v17_l83 (-> timeline (tc/select-rows :edited?) tc/row-count))


(def v19_l91 (def reactions (views/reactions-long messages)))


(def v20_l93 (tc/row-count reactions))


(def v21_l95 (tc/column-names reactions))


(def
 v23_l99
 (->
  reactions
  (tc/group-by [:emoji-name])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))


(def v25_l113 (def edits (views/edits-long messages)))


(def v26_l115 (tc/row-count edits))


(def v27_l117 (tc/column-names edits))


(def
 v29_l122
 (->
  edits
  (tc/select-columns [:message-id :edit-ts :edit-user-id])
  (tc/head 3)))


(def v31_l132 (def links (views/topic-links-long messages)))


(def v32_l134 (tc/row-count links))


(def v33_l136 (tc/column-names links))


(def
 v35_l140
 (->
  links
  (tc/add-column
   :host
   (fn
    [ds]
    (mapv
     (fn* [p1__51540#] (some-> p1__51540# (java.net.URI.) .getHost))
     (:link-url ds))))
  (tc/group-by [:host])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))


(def
 v37_l174
 (def
  gratitude-messages
  (->>
   (pull/pull-channels! ["gratitude"])
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v38_l179
 (def gratitude-timeline (views/messages-timeline gratitude-messages)))


(def
 v40_l185
 (->
  gratitude-timeline
  (tc/select-columns [:sender :subject :content])
  (tc/map-columns
   :content
   [:content]
   (fn [c] (subs c 0 (min 160 (count c)))))
  (tc/head 4)))
