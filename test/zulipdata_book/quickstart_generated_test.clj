(ns
 zulipdata-book.quickstart-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def v3_l33 (def me (client/whoami)))


(def v4_l35 me)


(def v6_l43 (def public-channels (pull/public-channel-names)))


(def v7_l45 (count public-channels))


(def v9_l49 (take 5 (sort public-channels)))


(def v11_l55 (def web-public (pull/web-public-channel-names)))


(def v12_l57 web-public)


(def v14_l66 (def pulled (pull/pull-channels! ["clojurecivitas"])))


(def
 v15_l69
 (def message-count (get-in pulled ["clojurecivitas" :message-count])))


(def v16_l72 message-count)


(def
 v18_l76
 (def raw-messages (pull/all-messages (get pulled "clojurecivitas"))))


(def v19_l79 (count raw-messages))


(deftest t20_l81 (is (= v19_l79 message-count)))


(def v22_l87 (first raw-messages))


(def v24_l94 (def timeline (views/messages-timeline raw-messages)))


(def v25_l96 (-> timeline (tc/order-by :instant :desc)))


(def v26_l99 (tc/row-count timeline))


(deftest t27_l101 (is (= v26_l99 message-count)))


(def v29_l106 (tc/column-names timeline))


(def
 v31_l117
 (def realm-emoji (-> (client/api-get "/realm/emoji") :emoji)))


(def
 v32_l120
 (defn
  emoji-display
  [reaction-type emoji-code emoji-name]
  (case
   reaction-type
   "unicode_emoji"
   (->>
    (str/split emoji-code #"-")
    (mapcat
     (fn*
      [p1__50185#]
      (Character/toChars (Integer/parseInt p1__50185# 16))))
    char-array
    String.)
   ("realm_emoji" "zulip_extra_emoji")
   (when-let
    [src (get-in realm-emoji [(keyword emoji-code) :source_url])]
    (kind/hiccup
     [:img {:src src, :width 24, :height 24, :alt emoji-name}]))
   nil)))


(def
 v34_l137
 (->
  (->>
   (pull/pull-channels!
    ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"])
   (mapcat (fn [[_ r]] (pull/all-messages r))))
  views/reactions-long
  (tc/map-columns
   :emoji
   [:reaction-type :emoji-code :emoji-name]
   emoji-display)
  (tc/group-by [:emoji-name :emoji])
  (tc/aggregate {:n tc/row-count})
  (tc/order-by [:n] [:desc])
  (tc/head 5)
  kind/table))
