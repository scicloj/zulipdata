(ns
 zulipdata-book.views-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l43
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def
 v4_l46
 (def
  messages
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def v5_l51 (count messages))


(def v7_l62 (def timeline (views/messages-timeline messages)))


(def v8_l64 (tc/row-count timeline))


(deftest t9_l66 (is (= v8_l64 (count messages))))


(def v11_l71 (-> timeline tc/column-names sort))


(deftest
 t12_l73
 (is
  (=
   v11_l71
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


(def v14_l81 (-> timeline (tc/order-by :instant :desc)))


(def v16_l88 (-> timeline :instant first type))


(deftest t17_l90 (is (= v16_l88 java.time.Instant)))


(def v19_l96 (-> timeline (tc/select-rows :edited) tc/row-count))


(def v21_l104 (def reactions (views/reactions-long messages)))


(def v22_l106 (tc/row-count reactions))


(def v23_l108 (-> reactions tc/column-names sort))


(deftest
 t24_l110
 (is
  (=
   v23_l108
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
 v26_l116
 (->
  reactions
  (tc/group-by [:emoji-name])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))


(def v28_l131 (def edits (views/edits-long messages)))


(def v29_l133 (tc/row-count edits))


(def v30_l135 (-> edits tc/column-names sort))


(deftest
 t31_l137
 (is
  (=
   v30_l135
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def
 v33_l143
 (->
  edits
  (tc/select-columns
   [:message-id :edit-ts :edit-user-id :prev-subject :prev-stream])
  (tc/order-by :edit-ts :desc)
  (tc/head 5)))


(def v35_l155 (def links (views/topic-links-long messages)))


(def v36_l157 (tc/row-count links))


(def v37_l159 (tc/column-names links))


(deftest
 t38_l161
 (is
  (= v37_l159 [:message-id :stream-id :channel :link-text :link-url])))


(def
 v40_l166
 (->
  links
  (tc/add-column
   :host
   (fn
    [ds]
    (mapv
     (fn* [p1__53419#] (some-> p1__53419# (java.net.URI.) .getHost))
     (:link-url ds))))
  (tc/group-by [:host])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))
