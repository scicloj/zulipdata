(ns
 zulipdata-book.quickstart-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l26 (def me (client/whoami)))


(def v4_l28 me)


(def v6_l36 (def public-channels (pull/public-channel-names)))


(def v7_l38 (count public-channels))


(def v9_l42 (take 5 (sort public-channels)))


(def v11_l48 (def web-public (pull/web-public-channel-names)))


(def v12_l50 web-public)


(def v14_l59 (def pulled (pull/pull-channels! ["clojurecivitas"])))


(def
 v15_l62
 (def message-count (get-in pulled ["clojurecivitas" :message-count])))


(def v16_l65 message-count)


(def
 v18_l69
 (def raw-messages (pull/all-messages (get pulled "clojurecivitas"))))


(def v19_l72 (count raw-messages))


(deftest t20_l74 (is (= v19_l72 message-count)))


(def v22_l80 (first raw-messages))


(def v24_l87 (def timeline (views/messages-timeline raw-messages)))


(def v25_l89 (-> timeline (tc/order-by :instant :desc)))


(def v26_l92 (tc/row-count timeline))


(deftest t27_l94 (is (= v26_l92 message-count)))


(def v29_l99 (tc/column-names timeline))
