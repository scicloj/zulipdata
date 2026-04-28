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
 (is (= v6_l55 [:email :full-name :user-id :is-bot? :is-admin? :role])))


(def v9_l64 (-> (client/get-me) keys count))


(def v11_l71 (def streams-response (client/get-streams)))


(def v12_l73 (-> streams-response :streams count))


(def v14_l77 (-> streams-response :streams first keys sort))


(def
 v16_l89
 (def
  one-message-response
  (client/get-messages
   {:narrow [{:operator "channel", :operand "kindly-dev"}],
    :anchor "newest",
    :num-before 1,
    :num-after 0})))


(def v17_l96 (-> one-message-response :messages count))


(deftest t18_l98 (is (= v17_l96 1)))


(def v20_l103 (-> one-message-response :messages first keys sort))


(def
 v22_l109
 (select-keys
  one-message-response
  [:found_anchor :found_oldest :found_newest]))


(def v24_l121 client/base-url)


(deftest
 t25_l123
 (is (= v24_l121 "https://clojurians.zulipchat.com/api/v1")))


(def
 v27_l129
 (->
  (client/api-get "/server_settings")
  (select-keys [:realm_name :realm_uri :zulip_version])))
