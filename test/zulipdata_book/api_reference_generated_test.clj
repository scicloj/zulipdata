(ns
 zulipdata-book.api-reference-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.zulipdata.graph :as graph]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l45
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def v4_l48 (def sample-pull (pull/pull-channels! sample-channels)))


(def
 v5_l51
 (def
  sample-messages
  (->>
   sample-pull
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v6_l56
 (def sample-timeline (views/messages-timeline sample-messages)))


(def
 v7_l59
 (def sample-anon (anon/anonymized-timeline sample-messages)))


(def v8_l62 (def sample-with-time (nar/with-time-columns sample-anon)))


(def v10_l67 (kind/doc #'client/base-url))


(def v11_l69 client/base-url)


(deftest
 t12_l71
 (is (= v11_l69 "https://clojurians.zulipchat.com/api/v1")))


(def v13_l73 (kind/doc #'client/api-get))


(def v14_l75 (-> (client/api-get "/server_settings") :realm_name))


(deftest t15_l78 (is (= v14_l75 "Clojurians")))


(def
 v17_l82
 (->
  (client/api-get
   "/messages"
   {"narrow"
    (charred.api/write-json-str
     [{:operator "channel", :operand "clojurecivitas"}]),
    "anchor" "newest",
    "num_before" 1,
    "num_after" 0})
  :messages
  count))


(deftest t18_l90 (is (= v17_l82 1)))


(def v19_l92 (kind/doc #'client/whoami))


(def v20_l94 (client/whoami))


(deftest
 t21_l96
 (is
  ((fn
    [m]
    (every?
     (set (keys m))
     [:email :full-name :user-id :is-bot :is-admin :role]))
   v20_l94)))


(def v22_l100 (kind/doc #'client/get-me))


(def v23_l102 (-> (client/get-me) :user_id integer?))


(deftest t24_l104 (is (true? v23_l102)))


(def v25_l106 (kind/doc #'client/get-streams))


(def v26_l108 (-> (client/get-streams) :streams count pos?))


(deftest t27_l110 (is (true? v26_l108)))


(def v28_l112 (kind/doc #'client/get-messages))


(def
 v29_l114
 (->
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 3,
    :num-after 0})
  :messages
  count))


(deftest t30_l121 (is (= v29_l114 3)))


(def v32_l125 (kind/doc #'pull/default-batch-size))


(def v33_l127 pull/default-batch-size)


(deftest t34_l129 (is (= v33_l127 5000)))


(def v35_l131 (kind/doc #'pull/fetch-window))


(def
 v36_l133
 (-> (pull/fetch-window "clojurecivitas" 0 100) :messages count))


(deftest t37_l136 (is (= v36_l133 100)))


(def v38_l138 (kind/doc #'pull/pull-channel!))


(def
 v40_l142
 (->
  (pull/pull-channel! "clojurecivitas" 0)
  (select-keys [:pages :message-count])
  keys
  set))


(deftest t41_l147 (is (= v40_l142 #{:pages :message-count})))


(def v42_l149 (kind/doc #'pull/all-messages))


(def
 v43_l151
 (let
  [walk
   (pull/pull-channel! "clojurecivitas" 0)
   messages
   (pull/all-messages walk)]
  (= (count messages) (:message-count walk))))


(deftest t44_l155 (is (true? v43_l151)))


(def v45_l157 (kind/doc #'pull/pull-channels!))


(def
 v47_l162
 (->
  (pull/pull-channels! ["clojurecivitas" "no-such-channel"])
  :not-found))


(deftest t48_l165 (is (= v47_l162 ["no-such-channel"])))


(def v49_l167 (kind/doc #'pull/public-channel-names))


(def v50_l169 (-> (pull/public-channel-names) count pos?))


(deftest t51_l171 (is (true? v50_l169)))


(def v52_l173 (kind/doc #'pull/pull-public-channels!))


(def v54_l181 (kind/doc #'views/messages-timeline))


(def
 v55_l183
 (-> (views/messages-timeline sample-messages) tc/row-count))


(deftest t56_l186 (is (= v55_l183 (count sample-messages))))


(def v58_l190 (-> sample-timeline tc/column-names sort))


(deftest
 t59_l192
 (is
  (=
   v58_l190
   '(:channel
     :client
     :content
     :content-length
     :edited
     :id
     :instant
     :last-edit-ts
     :sender
     :sender-id
     :stream-id
     :subject
     :timestamp))))


(def v60_l196 (kind/doc #'views/reactions-long))


(def
 v61_l198
 (-> (views/reactions-long sample-messages) tc/column-names sort))


(deftest
 t62_l201
 (is
  (=
   v61_l198
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :stream-id
     :subject
     :user-id))))


(def v63_l205 (kind/doc #'views/edits-long))


(def
 v64_l207
 (-> (views/edits-long sample-messages) tc/column-names sort))


(deftest
 t65_l210
 (is
  (=
   v64_l207
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v66_l214 (kind/doc #'views/topic-links-long))


(def
 v67_l216
 (-> (views/topic-links-long sample-messages) tc/column-names sort))


(deftest
 t68_l219
 (is
  (= v67_l216 '(:channel :link-text :link-url :message-id :stream-id))))


(def v70_l224 (kind/doc #'anon/user-key))


(def v71_l226 (anon/user-key 42))


(deftest
 t72_l228
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v71_l226)))


(def
 v74_l232
 [(= (anon/user-key 42) (anon/user-key 42)) (anon/user-key nil)])


(deftest t75_l235 (is (= v74_l232 [true nil])))


(def v76_l237 (kind/doc #'anon/subject-key))


(def v77_l239 (anon/subject-key "channel introductions"))


(deftest
 t78_l241
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v77_l239)))


(def v79_l243 (kind/doc #'anon/anonymized-timeline))


(def
 v80_l245
 (-> (anon/anonymized-timeline sample-messages) tc/column-names sort))


(deftest
 t81_l248
 (is
  (=
   v80_l245
   '(:channel
     :client
     :content-length
     :edited
     :id
     :last-edit-ts
     :reaction-count
     :stream-id
     :subject-key
     :timestamp
     :user-key))))


(def v82_l252 (kind/doc #'anon/anonymized-reactions))


(def
 v83_l254
 (-> (anon/anonymized-reactions sample-messages) tc/column-names sort))


(deftest
 t84_l257
 (is
  (=
   v83_l254
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :reactor-user-key
     :stream-id
     :subject-key))))


(def v85_l261 (kind/doc #'anon/anonymized-edits))


(def
 v86_l263
 (-> (anon/anonymized-edits sample-messages) tc/column-names sort))


(deftest
 t87_l266
 (is
  (=
   v86_l263
   '(:channel
     :edit-ts
     :editor-user-key
     :message-id
     :prev-stream
     :prev-subject-key
     :stream-id))))


(def v89_l271 (kind/doc #'nar/ts->month-date))


(def v90_l273 (nar/ts->month-date 1725611765))


(deftest t91_l275 (is (= v90_l273 (java.time.LocalDate/of 2024 9 1))))


(def v92_l277 (kind/doc #'nar/ts->year-month))


(def v93_l279 (nar/ts->year-month 1725611765))


(deftest t94_l281 (is (= v93_l279 "2024-09")))


(def v95_l283 (kind/doc #'nar/ts->year))


(def v96_l285 (nar/ts->year 1725611765))


(deftest t97_l287 (is (= v96_l285 2024)))


(def v98_l289 (kind/doc #'nar/with-time-columns))


(def
 v99_l291
 (->
  (nar/with-time-columns sample-anon)
  tc/column-names
  set
  (clojure.set/intersection #{:month-date :year :year-month})))


(deftest t100_l296 (is (= v99_l291 #{:month-date :year :year-month})))


(def v101_l298 (kind/doc #'nar/channel-lifecycle))


(def
 v102_l300
 (-> (nar/channel-lifecycle sample-with-time) tc/column-names sort))


(deftest
 t103_l303
 (is
  (=
   v102_l300
   '(:active-months
     :channel
     :distinct-users
     :first-date
     :last-date
     :total))))


(def v104_l306 (kind/doc #'nar/channels-by-name-pattern))


(def
 v105_l308
 (nar/channels-by-name-pattern sample-with-time #"civitas|gratitude"))


(deftest t106_l310 (is (= v105_l308 ["clojurecivitas" "gratitude"])))


(def v107_l312 (kind/doc #'nar/channels-by-shared-users))


(def
 v109_l318
 (set
  (nar/channels-by-shared-users
   sample-with-time
   "clojurecivitas"
   :share
   0.5
   :min-msgs
   5
   :top-n
   5)))


(deftest t110_l322 (is (contains? v109_l318 "clojurecivitas")))


(def v111_l324 (kind/doc #'nar/first-posters-of-channel))


(def
 v112_l326
 (->
  (nar/first-posters-of-channel sample-with-time "clojurecivitas" 5)
  tc/column-names
  sort))


(deftest t113_l329 (is (= v112_l326 '(:first-post-date :user-key))))


(def v114_l331 (kind/doc #'nar/prior-channels-of-newcomers))


(def
 v115_l333
 (->
  (nar/prior-channels-of-newcomers
   sample-with-time
   "clojurecivitas"
   "2025-10")
  tc/column-names
  sort))


(deftest
 t116_l336
 (is (= v115_l333 '(:newcomers-touched :prior-channel))))


(def v117_l338 (kind/doc #'nar/channel-monthly-activity))


(def
 v118_l340
 (->
  (nar/channel-monthly-activity sample-with-time #{"clojurecivitas"})
  tc/column-names
  sort))


(deftest t119_l343 (is (= v118_l340 '(:channel :month-date :msgs))))


(def v121_l347 (kind/doc #'graph/user-channel-sets))


(def
 v123_l351
 (let
  [u->c
   (graph/user-channel-sets sample-with-time)
   [_ chans]
   (first u->c)]
  (set? chans)))


(deftest t124_l355 (is (true? v123_l351)))


(def v125_l357 (kind/doc #'graph/channel-comembership-graph))


(def
 v126_l359
 (let
  [g (graph/channel-comembership-graph sample-with-time :min-shared 1)]
  (= (set sample-channels) (.vertexSet g))))


(deftest t127_l362 (is (true? v126_l359)))


(def v128_l364 (kind/doc #'graph/user-copresence-graph))


(def
 v129_l366
 (let
  [g
   (graph/user-copresence-graph
    sample-with-time
    :min-shared
    2
    :min-channels
    2)]
  (pos? (count (.vertexSet g)))))


(deftest t130_l370 (is (true? v129_l366)))


(def v131_l372 (kind/doc #'graph/migration-graph))


(def
 v133_l377
 (let
  [g
   (graph/migration-graph
    sample-with-time
    #{"clojurecivitas"}
    :min-users
    1)]
  (every?
   (fn [e] (not= (.getEdgeSource g e) (.getEdgeTarget g e)))
   (.edgeSet g))))


(deftest t134_l381 (is (true? v133_l377)))


(def v135_l383 (kind/doc #'graph/betweenness))


(def
 v136_l385
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   scores
   (graph/betweenness g)]
  (= (.vertexSet g) (set (keys scores)))))


(deftest t137_l389 (is (true? v136_l385)))


(def v138_l391 (kind/doc #'graph/girvan-newman))


(def
 v139_l393
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/girvan-newman g 2)]
  (count clusters)))


(deftest t140_l397 (is (= v139_l393 2)))


(def v141_l399 (kind/doc #'graph/label-propagation))


(def
 v142_l401
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/label-propagation g)]
  (every? set? clusters)))


(deftest t143_l405 (is (true? v142_l401)))


(def v144_l407 (kind/doc #'graph/->cytoscape-elements))


(def
 v145_l409
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   e
   (graph/->cytoscape-elements g)]
  (set (keys e))))


(deftest t146_l413 (is (= v145_l409 #{:nodes :edges})))


(def v147_l415 (kind/doc #'graph/->dot))


(def
 v148_l417
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   dot
   (graph/->dot g :directed false)]
  (and (string? dot) (clojure.string/starts-with? dot "graph "))))


(deftest t149_l422 (is (true? v148_l417)))
