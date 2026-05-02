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


(def v12_l70 (:found_newest first-window))


(def v14_l80 pull/default-batch-size)


(deftest t15_l82 (is (= v14_l80 5000)))


(def
 v17_l95
 (def clojurecivitas-pull (pull/pull-channel! "clojurecivitas" 0)))


(def v18_l98 (:message-count clojurecivitas-pull))


(def v19_l100 (count (:pages clojurecivitas-pull)))


(def
 v21_l109
 (def clojurecivitas-messages (pull/all-messages clojurecivitas-pull)))


(def v22_l111 (count clojurecivitas-messages))


(deftest
 t23_l113
 (is (= v22_l111 (:message-count clojurecivitas-pull))))


(def v25_l118 (first clojurecivitas-messages))


(def
 v27_l127
 (def
  pulled
  (pull/pull-channels!
   ["clojurecivitas" "definitely-not-a-real-channel"])))


(def v28_l130 (get-in pulled ["clojurecivitas" :message-count]))


(deftest t29_l132 (is (> v28_l130 0)))


(def
 v31_l143
 (<=
  (get-in pulled ["clojurecivitas" :message-count])
  (:message-count clojurecivitas-pull)))


(deftest t32_l146 (is (= v31_l143 true)))


(def v34_l151 (:not-found pulled))


(deftest t35_l153 (is (= v34_l151 ["definitely-not-a-real-channel"])))


(def
 v37_l160
 (->
  (get pulled "clojurecivitas")
  (select-keys [:stream-id :first-message-id :message-count])))


(def
 v39_l183
 (def
  clojurecivitas-pull-fresh
  (pull/pull-channel! "clojurecivitas" 0 :refresh-tip true)))


(def v40_l186 (:message-count clojurecivitas-pull-fresh))


(def
 v42_l191
 (>=
  (:message-count clojurecivitas-pull-fresh)
  (:message-count clojurecivitas-pull)))


(deftest t43_l193 (is (= v42_l191 true)))


(def
 v45_l215
 (def
  two-channel-pull
  (pull/pull-channels!
   ["clojurecivitas" "scicloj-webpublic"]
   :parallelism
   2)))


(def
 v46_l218
 (map
  (fn [[k v]] [k (:message-count v)])
  (dissoc two-channel-pull :not-found)))
