(ns
 zulipdata-book.anonymize-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l53 (anon/user-key 42))


(def v5_l58 (= (anon/user-key 42) (anon/user-key 42)))


(deftest t6_l60 (is (= v5_l58 true)))


(def v8_l65 (not= (anon/user-key 42) (anon/user-key 43)))


(deftest t9_l67 (is (= v8_l65 true)))


(def v11_l73 (anon/user-key nil))


(deftest t12_l75 (is (= v11_l73 nil)))


(def v13_l78 (anon/subject-key "channel introductions"))


(def
 v15_l86
 (def
  messages
  (->
   (pull/pull-channels! ["kindly-dev"])
   (get "kindly-dev")
   pull/all-messages)))


(def v17_l98 (def anon-timeline (anon/anonymized-timeline messages)))


(def v19_l103 anon-timeline)


(def v20_l105 (tc/row-count anon-timeline))


(deftest t21_l107 (is (= v20_l105 (count messages))))


(def v23_l112 (-> anon-timeline :user-key distinct sort))


(def v25_l122 (def anon-reactions (anon/anonymized-reactions messages)))


(def v26_l124 anon-reactions)


(def v28_l134 (def anon-edits (anon/anonymized-edits messages)))


(def v29_l136 anon-edits)
