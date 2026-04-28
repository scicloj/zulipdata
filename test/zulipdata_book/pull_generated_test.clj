(ns
 zulipdata-book.pull-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l41 (def public-channels (pull/public-channel-names)))


(def v4_l43 (count public-channels))


(def v5_l45 (take 5 (sort public-channels)))


(def v7_l57 (def first-window (pull/fetch-window "kindly-dev" 0 100)))


(def v8_l60 (-> first-window :messages count))


(def v9_l62 (:found_anchor first-window))


(deftest t10_l64 (is (= v9_l62 false)))


(def v12_l70 (:found_newest first-window))


(def v14_l80 pull/default-batch-size)


(deftest t15_l82 (is (= v14_l80 5000)))


(def v17_l95 (def kindly-dev-pull (pull/pull-channel! "kindly-dev" 0)))


(def v18_l98 (:message-count kindly-dev-pull))


(def v19_l100 (count (:pages kindly-dev-pull)))


(def
 v21_l108
 (def kindly-dev-messages (pull/all-messages kindly-dev-pull)))


(def v22_l110 (count kindly-dev-messages))


(deftest t23_l112 (is (= v22_l110 (:message-count kindly-dev-pull))))


(def
 v25_l117
 (->
  kindly-dev-messages
  first
  (select-keys [:id :sender_full_name :timestamp])))


(def
 v27_l126
 (def
  pulled
  (pull/pull-channels! ["kindly-dev" "definitely-not-a-real-channel"])))


(def v28_l129 (get-in pulled ["kindly-dev" :message-count]))


(deftest t29_l131 (is (= v28_l129 (:message-count kindly-dev-pull))))


(def v31_l136 (:not-found pulled))


(deftest t32_l138 (is (= v31_l136 ["definitely-not-a-real-channel"])))


(def
 v34_l145
 (->
  (get pulled "kindly-dev")
  (select-keys [:stream-id :first-message-id :message-count])))


(def
 v36_l168
 (def
  kindly-dev-pull-fresh
  (pull/pull-channel! "kindly-dev" 0 :refresh-tip true)))


(def v37_l171 (:message-count kindly-dev-pull-fresh))


(def
 v39_l176
 (>=
  (:message-count kindly-dev-pull-fresh)
  (:message-count kindly-dev-pull)))


(deftest t40_l178 (is (= v39_l176 true)))


(def
 v42_l200
 (def
  kindly-and-noj
  (pull/pull-channels! ["kindly-dev" "noj-dev"] :parallelism 2)))


(def
 v43_l203
 (map
  (fn [[k v]] [k (:message-count v)])
  (dissoc kindly-and-noj :not-found)))
