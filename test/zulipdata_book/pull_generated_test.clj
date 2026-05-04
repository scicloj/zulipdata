(ns
 zulipdata-book.pull-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l41 (def public-channels (pull/public-channel-names)))


(def v4_l43 (count public-channels))


(def v5_l45 (take 5 (sort public-channels)))


(def
 v7_l57
 (def first-window (pull/fetch-window "clojurecivitas" 0 100)))


(def v8_l60 (-> first-window :messages count))


(def v9_l62 (:found_anchor first-window))


(deftest t10_l64 (is (= v9_l62 false)))


(def v12_l71 (:found_newest first-window))


(def v14_l81 pull/default-batch-size)


(deftest t15_l83 (is (= v14_l81 5000)))


(def
 v17_l96
 (def clojurecivitas-pull (pull/pull-channel! "clojurecivitas" 0)))


(def v18_l99 (:message-count clojurecivitas-pull))


(def v19_l101 (count (:pages clojurecivitas-pull)))


(def
 v21_l110
 (def clojurecivitas-messages (pull/all-messages clojurecivitas-pull)))


(def v22_l112 (count clojurecivitas-messages))


(deftest
 t23_l114
 (is (= v22_l112 (:message-count clojurecivitas-pull))))


(def v25_l119 (first clojurecivitas-messages))


(def
 v27_l128
 (def
  pulled
  (pull/pull-channels!
   ["clojurecivitas" "definitely-not-a-real-channel"])))


(def v28_l131 (get-in pulled ["clojurecivitas" :message-count]))


(deftest t29_l133 (is (> v28_l131 0)))


(def
 v31_l144
 (<=
  (get-in pulled ["clojurecivitas" :message-count])
  (:message-count clojurecivitas-pull)))


(deftest t32_l147 (is (= v31_l144 true)))


(def v34_l152 (:not-found pulled))


(deftest t35_l154 (is (= v34_l152 ["definitely-not-a-real-channel"])))


(def
 v37_l161
 (->
  (get pulled "clojurecivitas")
  (select-keys [:stream-id :first-message-id :message-count])))


(def
 v39_l184
 (def
  clojurecivitas-pull-fresh
  (pull/pull-channel! "clojurecivitas" 0 :refresh true)))


(def v40_l187 (:message-count clojurecivitas-pull-fresh))


(def
 v42_l192
 (>=
  (:message-count clojurecivitas-pull-fresh)
  (:message-count clojurecivitas-pull)))


(deftest t43_l194 (is (= v42_l192 true)))


(def
 v45_l216
 (def
  two-channel-pull
  (pull/pull-channels!
   ["clojurecivitas" "scicloj-webpublic"]
   :parallelism
   2)))


(def
 v46_l219
 (map
  (fn [[k v]] [k (:message-count v)])
  (dissoc two-channel-pull :not-found)))
