(ns
 zulipdata-book.quickstart-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l31 (def me (client/whoami)))


(def v4_l33 me)


(def v6_l41 (def public-channels (pull/public-channel-names)))


(def v7_l43 (count public-channels))


(def v9_l47 (take 5 (sort public-channels)))


(def v11_l53 (def web-public (pull/web-public-channel-names)))


(def v12_l55 web-public)


(def v14_l64 (def pulled (pull/pull-channels! ["clojurecivitas"])))


(def
 v15_l67
 (def message-count (get-in pulled ["clojurecivitas" :message-count])))


(def v16_l70 message-count)


(def
 v18_l74
 (def raw-messages (pull/all-messages (get pulled "clojurecivitas"))))


(def v19_l77 (count raw-messages))


(deftest t20_l79 (is (= v19_l77 message-count)))


(def v22_l85 (first raw-messages))


(def v24_l92 (def timeline (views/messages-timeline raw-messages)))


(def v25_l94 (-> timeline (tc/order-by :instant :desc)))


(def v26_l97 (tc/row-count timeline))


(deftest t27_l99 (is (= v26_l97 message-count)))


(def v29_l104 (tc/column-names timeline))
