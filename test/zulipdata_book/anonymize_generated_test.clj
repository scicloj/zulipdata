(ns
 zulipdata-book.anonymize-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l47 (anon/user-key 42))


(def v5_l52 (= (anon/user-key 42) (anon/user-key 42)))


(deftest t6_l54 (is (= v5_l52 true)))


(def v8_l59 (not= (anon/user-key 42) (anon/user-key 43)))


(deftest t9_l61 (is (= v8_l59 true)))


(def v11_l67 (anon/user-key nil))


(deftest t12_l69 (is (= v11_l67 nil)))


(def v13_l72 (anon/subject-key "channel introductions"))


(def
 v15_l80
 (def
  messages
  (->
   (pull/pull-channels! ["kindly-dev"])
   (get "kindly-dev")
   pull/all-messages)))


(def v17_l91 (def anon-timeline (anon/anonymized-timeline messages)))


(def v18_l93 (tc/row-count anon-timeline))


(deftest t19_l95 (is (= v18_l93 (count messages))))


(def v20_l98 (tc/column-names anon-timeline))


(def
 v22_l103
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


(def v24_l110 (-> anon-timeline :user-key distinct count))


(def v26_l118 (def anon-reactions (anon/anonymized-reactions messages)))


(def v27_l120 (tc/row-count anon-reactions))


(def v28_l122 (tc/column-names anon-reactions))


(def v30_l130 (def anon-edits (anon/anonymized-edits messages)))


(def v31_l132 (tc/row-count anon-edits))


(def v32_l134 (tc/column-names anon-edits))
