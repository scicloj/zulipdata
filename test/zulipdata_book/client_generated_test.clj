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


(def v11_l71 (def streams-response (client/get-streams)))


(def v12_l73 (-> streams-response :streams count))


(def v14_l77 (-> streams-response :streams first keys sort))


(def
 v16_l85
 (def
  web-public-channels
  (->>
   streams-response
   :streams
   (filter :is_web_public)
   (mapv :name)
   sort)))


(def v17_l92 (count web-public-channels))


(def v19_l96 web-public-channels)


(def
 v21_l121
 (def
  one-message-response
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 1,
    :num-after 0})))


(def v22_l128 (-> one-message-response :messages count))


(deftest t23_l130 (is (= v22_l128 1)))


(def v25_l139 (-> one-message-response :messages first))


(def
 v27_l145
 (select-keys
  one-message-response
  [:found_anchor :found_oldest :found_newest]))


(def v29_l157 client/base-url)


(deftest
 t30_l159
 (is (= v29_l157 "https://clojurians.zulipchat.com/api/v1")))


(def
 v32_l165
 (->
  (client/api-get "/server_settings")
  (select-keys [:realm_name :realm_uri :zulip_version])))
