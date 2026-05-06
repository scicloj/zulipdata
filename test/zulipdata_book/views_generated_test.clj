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
  (-> (pull/pull-channels! sample-channels) pull/all-channel-messages)))


(def v5_l69 (count messages))


(def v7_l80 (def timeline (views/messages-timeline messages)))


(def v8_l82 (tc/row-count timeline))


(deftest t9_l84 (is (= v8_l82 (count messages))))


(def v11_l89 (-> timeline tc/column-names sort))


(deftest
 t12_l91
 (is
  (=
   v11_l89
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


(def v14_l99 (-> timeline (tc/order-by :instant :desc)))


(def v16_l106 (-> timeline :instant first type))


(deftest t17_l108 (is (= v16_l106 java.time.Instant)))


(def v19_l114 (-> timeline (tc/select-rows :edited) tc/row-count))


(def v21_l122 (def reactions (views/reactions-long messages)))


(def v22_l124 reactions)


(def v23_l126 (-> reactions tc/column-names sort))


(deftest
 t24_l128
 (is
  (=
   v23_l126
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
 v26_l134
 (->
  reactions
  (tc/group-by [:emoji-name])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))


(def v28_l149 (def edits (views/edits-long messages)))


(def v29_l151 edits)


(def v30_l153 (-> edits tc/column-names sort))


(deftest
 t31_l155
 (is
  (=
   v30_l153
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v33_l165 (def links (views/topic-links-long messages)))


(def v34_l167 links)


(def v35_l169 (tc/column-names links))


(deftest
 t36_l171
 (is
  (= v35_l169 [:message-id :stream-id :channel :link-text :link-url])))


(def
 v38_l176
 (->
  links
  (tc/add-column
   :host
   (fn
    [ds]
    (mapv
     (fn* [p1__48757#] (some-> p1__48757# (java.net.URI.) .getHost))
     (:link-url ds))))
  (tc/group-by [:host])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)))
