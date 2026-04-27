(ns
 zulipdata-book.quickstart-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l24 (def me (client/whoami)))


(def v4_l26 (:email me))


(def v5_l28 (:user-id me))


(def v7_l35 (def public-channels (pull/public-channel-names)))


(def v8_l37 (count public-channels))


(def v10_l41 (take 5 (sort public-channels)))


(def v12_l49 (def pulled (pull/pull-channels! ["kindly-dev"])))


(def
 v13_l52
 (def message-count (get-in pulled ["kindly-dev" :message-count])))


(def v14_l55 message-count)


(def
 v16_l59
 (def raw-messages (pull/all-messages (get pulled "kindly-dev"))))


(def v17_l62 (count raw-messages))


(deftest t18_l64 (is (= v17_l62 message-count)))


(def v20_l72 (def timeline (views/messages-timeline raw-messages)))


(def v21_l74 (tc/row-count timeline))


(deftest t22_l76 (is (= v21_l74 message-count)))


(def v24_l81 (tc/column-names timeline))
