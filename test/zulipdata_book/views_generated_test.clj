(ns
 zulipdata-book.views-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l62
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def
 v4_l65
 (def
  messages
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l70 (count messages))


(def v7_l81 (def timeline (views/messages-timeline messages)))


(def v8_l83 (tc/row-count timeline))


(deftest t9_l85 (is (= v8_l83 (count messages))))


(def v11_l90 (-> timeline tc/column-names sort))


(deftest
 t12_l92
 (is
  (=
   v11_l90
   '(:channel
     :client
     :content
     :content-length
     :edited
     :id
     :instant
     :last-edit-ts
     :sender
     :sender-id
     :stream-id
     :subject
     :timestamp))))


(def v14_l100 (-> timeline (tc/order-by :instant :desc)))


(def v16_l107 (-> timeline :instant first type))


(deftest t17_l109 (is (= v16_l107 java.time.Instant)))


(def v19_l115 (-> timeline (tc/select-rows :edited) tc/row-count))


(def v21_l123 (def reactions (views/reactions-long messages)))


(def v22_l125 reactions)


(def v23_l127 (-> reactions tc/column-names sort))


(deftest
 t24_l129
 (is
  (=
   v23_l127
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :stream-id
     :subject
     :user-id))))


(def
 v26_l135
 (->
  reactions
  (tc/group-by [:emoji-name])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))


(def v28_l150 (def edits (views/edits-long messages)))


(def v29_l152 edits)


(def v30_l154 (-> edits tc/column-names sort))


(deftest
 t31_l156
 (is
  (=
   v30_l154
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v33_l166 (def links (views/topic-links-long messages)))


(def v34_l168 links)


(def v35_l170 (tc/column-names links))


(deftest
 t36_l172
 (is
  (= v35_l170 [:message-id :stream-id :channel :link-text :link-url])))


(def
 v38_l177
 (->
  links
  (tc/add-column
   :host
   (fn
    [ds]
    (mapv
     (fn* [p1__51162#] (some-> p1__51162# (java.net.URI.) .getHost))
     (:link-url ds))))
  (tc/group-by [:host])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))
