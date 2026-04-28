(ns
 zulipdata-book.anonymize-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l48 (anon/user-key 42))


(def v5_l53 (= (anon/user-key 42) (anon/user-key 42)))


(deftest t6_l55 (is (= v5_l53 true)))


(def v8_l60 (not= (anon/user-key 42) (anon/user-key 43)))


(deftest t9_l62 (is (= v8_l60 true)))


(def v11_l68 (anon/user-key nil))


(deftest t12_l70 (is (= v11_l68 nil)))


(def v13_l73 (anon/subject-key "channel introductions"))


(def
 v15_l81
 (def
  messages
  (->
   (pull/pull-channels! ["kindly-dev"])
   (get "kindly-dev")
   pull/all-messages)))


(def v17_l93 (def anon-timeline (anon/anonymized-timeline messages)))


(def v18_l95 (tc/row-count anon-timeline))


(deftest t19_l97 (is (= v18_l95 (count messages))))


(def v20_l100 (tc/column-names anon-timeline))


(def
 v22_l105
 (->
  anon-timeline
  (tc/select-columns
   [:id
    :timestamp
    :channel
    :user-key
    :subject-key
    :content-length
    :reaction-count])
  (tc/head 3)))


(def v24_l112 (-> anon-timeline :user-key distinct count))


(def v26_l122 (def anon-reactions (anon/anonymized-reactions messages)))


(def v27_l124 (tc/row-count anon-reactions))


(def v28_l126 (tc/column-names anon-reactions))


(def v30_l136 (def anon-edits (anon/anonymized-edits messages)))


(def v31_l138 (tc/row-count anon-edits))


(def v32_l140 (tc/column-names anon-edits))
