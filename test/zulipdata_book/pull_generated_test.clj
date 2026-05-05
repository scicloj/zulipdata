(ns
 zulipdata-book.pull-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l43 (def public-channels (pull/public-channel-names)))


(def v4_l45 (count public-channels))


(def v5_l47 (take 5 (sort public-channels)))


(def
 v7_l59
 (def first-window (pull/fetch-window "clojurecivitas" 0 100)))


(def v8_l62 (-> first-window :messages count))


(def v9_l64 (:found_anchor first-window))


(deftest t10_l66 (is (= v9_l64 false)))


(def v12_l73 (:found_newest first-window))


(def v14_l83 pull/default-batch-size)


(deftest t15_l85 (is (= v14_l83 5000)))


(def
 v17_l98
 (def clojurecivitas-pull (pull/pull-channel! "clojurecivitas" 0)))


(def v18_l101 (:message-count clojurecivitas-pull))


(def v19_l103 (count (:pages clojurecivitas-pull)))


(def
 v21_l112
 (def clojurecivitas-messages (pull/all-messages clojurecivitas-pull)))


(def v22_l114 (count clojurecivitas-messages))


(deftest
 t23_l116
 (is (= v22_l114 (:message-count clojurecivitas-pull))))


(def v25_l121 (first clojurecivitas-messages))


(def
 v27_l130
 (def
  pulled
  (pull/pull-channels!
   ["clojurecivitas" "definitely-not-a-real-channel"])))


(def v28_l133 (get-in pulled ["clojurecivitas" :message-count]))


(deftest t29_l135 (is (> v28_l133 0)))


(def
 v31_l146
 (<=
  (get-in pulled ["clojurecivitas" :message-count])
  (:message-count clojurecivitas-pull)))


(deftest t32_l149 (is (= v31_l146 true)))


(def v34_l154 (:not-found pulled))


(deftest t35_l156 (is (= v34_l154 ["definitely-not-a-real-channel"])))


(def
 v37_l163
 (->
  (get pulled "clojurecivitas")
  (select-keys [:stream-id :first-message-id :message-count])))


(def
 v39_l186
 (def
  clojurecivitas-pull-fresh
  (pull/pull-channel! "clojurecivitas" 0 :refresh true)))


(def v40_l189 (:message-count clojurecivitas-pull-fresh))


(def
 v42_l194
 (>=
  (:message-count clojurecivitas-pull-fresh)
  (:message-count clojurecivitas-pull)))


(deftest t43_l196 (is (= v42_l194 true)))


(def
 v45_l218
 (def
  two-channel-pull
  (pull/pull-channels!
   ["clojurecivitas" "scicloj-webpublic"]
   :parallelism
   2)))


(def
 v46_l221
 (map
  (fn [[k v]] [k (:message-count v)])
  (dissoc two-channel-pull :not-found)))
