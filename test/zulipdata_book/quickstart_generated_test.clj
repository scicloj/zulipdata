(ns
 zulipdata-book.quickstart-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def v3_l25 (def me (client/whoami)))


(def v4_l27 me)


(def v6_l35 (def public-channels (pull/public-channel-names)))


(def v7_l37 (count public-channels))


(def v9_l41 (take 5 (sort public-channels)))


(def v11_l47 (def web-public (pull/web-public-channel-names)))


(def v12_l49 web-public)


(def v14_l58 (def pulled (pull/pull-channels! ["clojurecivitas"])))


(def
 v15_l61
 (def message-count (get-in pulled ["clojurecivitas" :message-count])))


(def v16_l64 message-count)


(def
 v18_l68
 (def raw-messages (pull/all-messages (get pulled "clojurecivitas"))))


(def v19_l71 (count raw-messages))


(deftest t20_l73 (is (= v19_l71 message-count)))


(def v22_l79 (first raw-messages))


(def v24_l86 (def timeline (views/messages-timeline raw-messages)))


(def v25_l88 (-> timeline (tc/order-by :instant :desc)))


(def v26_l91 (tc/row-count timeline))


(deftest t27_l93 (is (= v26_l91 message-count)))


(def v29_l98 (tc/column-names timeline))
