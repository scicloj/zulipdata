(ns
 zulipdata-book.client-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v3_l50 (def me (client/whoami)))


(def v4_l52 me)


(def v6_l56 (keys me))


(deftest
 t7_l58
 (is (= v6_l56 [:email :full-name :user-id :is-bot :is-admin :role])))


(def v9_l65 (-> (client/get-me) keys count))


(def v11_l73 (def streams-response (client/get-streams)))


(def v12_l75 (-> streams-response :streams count))


(def v14_l79 (-> streams-response :streams first keys sort))


(def
 v16_l88
 (def
  web-public-channels
  (->>
   streams-response
   :streams
   (filter :is_web_public)
   (mapv :name)
   sort)))


(def v17_l95 (count web-public-channels))


(def v19_l99 web-public-channels)


(def
 v21_l126
 (def
  one-message-response
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 1,
    :num-after 0})))


(def v22_l133 (-> one-message-response :messages count))


(deftest t23_l135 (is (= v22_l133 1)))


(def v25_l144 (-> one-message-response :messages first))


(def
 v27_l150
 (select-keys
  one-message-response
  [:found_anchor :found_oldest :found_newest]))


(def v29_l162 client/base-url)


(deftest
 t30_l164
 (is (= v29_l162 "https://clojurians.zulipchat.com/api/v1")))


(def
 v32_l171
 (->
  (client/api-get "/server_settings")
  (select-keys [:realm_name :realm_uri :zulip_version])))
