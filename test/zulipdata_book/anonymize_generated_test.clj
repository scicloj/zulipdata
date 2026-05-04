(ns
 zulipdata-book.anonymize-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l49 (anon/user-key 42))


(def v5_l54 (= (anon/user-key 42) (anon/user-key 42)))


(deftest t6_l56 (is (= v5_l54 true)))


(def v8_l61 (not= (anon/user-key 42) (anon/user-key 43)))


(deftest t9_l63 (is (= v8_l61 true)))


(def v11_l69 (anon/user-key nil))


(deftest t12_l71 (is (= v11_l69 nil)))


(def v13_l74 (anon/subject-key "channel introductions"))


(def
 v15_l82
 (def
  messages
  (->
   (pull/pull-channels! ["kindly-dev"])
   (get "kindly-dev")
   pull/all-messages)))


(def v17_l94 (def anon-timeline (anon/anonymized-timeline messages)))


(def v18_l96 (tc/row-count anon-timeline))


(deftest t19_l98 (is (= v18_l96 (count messages))))


(def v20_l101 (tc/column-names anon-timeline))


(def
 v22_l106
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


(def v24_l113 (-> anon-timeline :user-key distinct count))


(def v26_l123 (def anon-reactions (anon/anonymized-reactions messages)))


(def v27_l125 (tc/row-count anon-reactions))


(def v28_l127 (tc/column-names anon-reactions))


(def v30_l137 (def anon-edits (anon/anonymized-edits messages)))


(def v31_l139 (tc/row-count anon-edits))


(def v32_l141 (tc/column-names anon-edits))
