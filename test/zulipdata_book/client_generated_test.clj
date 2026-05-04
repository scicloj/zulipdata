(ns
 zulipdata-book.client-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l49 (def me (client/whoami)))


(def v4_l51 me)


(def v6_l55 (keys me))


(deftest
 t7_l57
 (is (= v6_l55 [:email :full-name :user-id :is-bot :is-admin :role])))


(def v9_l64 (-> (client/get-me) keys count))


(def v11_l72 (def streams-response (client/get-streams)))


(def v12_l74 (-> streams-response :streams count))


(def v14_l78 (-> streams-response :streams first keys sort))


(def
 v16_l87
 (def
  web-public-channels
  (->>
   streams-response
   :streams
   (filter :is_web_public)
   (mapv :name)
   sort)))


(def v17_l94 (count web-public-channels))


(def v19_l98 web-public-channels)


(def
 v21_l125
 (def
  one-message-response
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 1,
    :num-after 0})))


(def v22_l132 (-> one-message-response :messages count))


(deftest t23_l134 (is (= v22_l132 1)))


(def v25_l143 (-> one-message-response :messages first))


(def
 v27_l149
 (select-keys
  one-message-response
  [:found_anchor :found_oldest :found_newest]))


(def v29_l161 client/base-url)


(deftest
 t30_l163
 (is (= v29_l161 "https://clojurians.zulipchat.com/api/v1")))


(def
 v32_l170
 (->
  (client/api-get "/server_settings")
  (select-keys [:realm_name :realm_uri :zulip_version])))
