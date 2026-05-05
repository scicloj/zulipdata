(ns
 zulipdata-book.client-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l51 (def me (client/whoami)))


(def v4_l53 me)


(def v6_l57 (keys me))


(deftest
 t7_l59
 (is (= v6_l57 [:email :full-name :user-id :is-bot :is-admin :role])))


(def v9_l66 (-> (client/get-me) keys count))


(def v11_l74 (def streams-response (client/get-streams)))


(def v12_l76 (-> streams-response :streams count))


(def v14_l80 (-> streams-response :streams first keys sort))


(def
 v16_l89
 (def
  web-public-channels
  (->>
   streams-response
   :streams
   (filter :is_web_public)
   (mapv :name)
   sort)))


(def v17_l96 (count web-public-channels))


(def v19_l100 web-public-channels)


(def
 v21_l127
 (def
  one-message-response
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 1,
    :num-after 0})))


(def v22_l134 (-> one-message-response :messages count))


(deftest t23_l136 (is (= v22_l134 1)))


(def v25_l145 (-> one-message-response :messages first))


(def
 v27_l151
 (select-keys
  one-message-response
  [:found_anchor :found_oldest :found_newest]))


(def v29_l163 client/base-url)


(deftest
 t30_l165
 (is (= v29_l163 "https://clojurians.zulipchat.com/api/v1")))


(def
 v32_l172
 (->
  (client/api-get "/server_settings")
  (select-keys [:realm_name :realm_uri :zulip_version])))
