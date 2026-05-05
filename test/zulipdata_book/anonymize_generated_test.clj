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


(def v19_l99 anon-timeline)


(def v20_l101 (tc/row-count anon-timeline))


(deftest t21_l103 (is (= v20_l101 (count messages))))


(def v23_l108 (-> anon-timeline :user-key distinct sort))


(def v25_l118 (def anon-reactions (anon/anonymized-reactions messages)))


(def v26_l120 anon-reactions)


(def v28_l130 (def anon-edits (anon/anonymized-edits messages)))


(def v29_l132 anon-edits)
