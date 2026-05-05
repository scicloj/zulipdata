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
 v3_l53
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def v4_l56 (def sample-pull (pull/pull-channels! sample-channels)))


(def
 v5_l59
 (def
  sample-messages
  (->>
   sample-pull
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v6_l64
 (def sample-timeline (views/messages-timeline sample-messages)))


(def
 v7_l67
 (def sample-anon (anon/anonymized-timeline sample-messages)))


(def v8_l70 (def sample-with-time (nar/with-time-columns sample-anon)))


(def v10_l75 (kind/doc #'client/base-url))


(def v11_l77 client/base-url)


(deftest
 t12_l79
 (is (= v11_l77 "https://clojurians.zulipchat.com/api/v1")))


(def v13_l81 (kind/doc #'client/api-get))


(def v14_l83 (-> (client/api-get "/server_settings") :realm_name))


(deftest t15_l86 (is (= v14_l83 "Clojurians")))


(def
 v17_l90
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


(deftest t18_l98 (is (= v17_l90 1)))


(def v19_l100 (kind/doc #'client/whoami))


(def v20_l102 (client/whoami))


(deftest
 t21_l104
 (is
  ((fn
    [m]
    (every?
     (set (keys m))
     [:email :full-name :user-id :is-bot :is-admin :role]))
   v20_l102)))


(def v22_l108 (kind/doc #'client/get-me))


(def v23_l110 (-> (client/get-me) :user_id integer?))


(deftest t24_l112 (is (true? v23_l110)))


(def v25_l114 (kind/doc #'client/get-streams))


(def v26_l116 (-> (client/get-streams) :streams count pos?))


(deftest t27_l118 (is (true? v26_l116)))


(def v28_l120 (kind/doc #'client/get-messages))


(def
 v29_l122
 (->
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 3,
    :num-after 0})
  :messages
  count))


(deftest t30_l129 (is (= v29_l122 3)))


(def v32_l133 (kind/doc #'pull/default-batch-size))


(def v33_l135 pull/default-batch-size)


(deftest t34_l137 (is (= v33_l135 5000)))


(def v35_l139 (kind/doc #'pull/fetch-window))


(def
 v36_l141
 (-> (pull/fetch-window "clojurecivitas" 0 100) :messages count))


(deftest t37_l144 (is (= v36_l141 100)))


(def v38_l146 (kind/doc #'pull/pull-channel!))


(def
 v40_l150
 (->
  (pull/pull-channel! "clojurecivitas" 0)
  (select-keys [:pages :message-count])
  keys
  set))


(deftest t41_l155 (is (= v40_l150 #{:pages :message-count})))


(def v42_l157 (kind/doc #'pull/all-messages))


(def
 v43_l159
 (let
  [walk
   (pull/pull-channel! "clojurecivitas" 0)
   messages
   (pull/all-messages walk)]
  (= (count messages) (:message-count walk))))


(deftest t44_l163 (is (true? v43_l159)))


(def v45_l165 (kind/doc #'pull/pull-channels!))


(def
 v47_l170
 (->
  (pull/pull-channels! ["clojurecivitas" "no-such-channel"])
  :not-found))


(deftest t48_l173 (is (= v47_l170 ["no-such-channel"])))


(def v49_l175 (kind/doc #'pull/public-channel-names))


(def v50_l177 (-> (pull/public-channel-names) count pos?))


(deftest t51_l179 (is (true? v50_l177)))


(def v52_l181 (kind/doc #'pull/pull-public-channels!))


(def v54_l189 (kind/doc #'views/messages-timeline))


(def
 v55_l191
 (-> (views/messages-timeline sample-messages) tc/row-count))


(deftest t56_l194 (is (= v55_l191 (count sample-messages))))


(def v58_l198 (-> sample-timeline tc/column-names sort))


(deftest
 t59_l200
 (is
  (=
   v58_l198
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


(def v60_l204 (kind/doc #'views/reactions-long))


(def
 v61_l206
 (-> (views/reactions-long sample-messages) tc/column-names sort))


(deftest
 t62_l209
 (is
  (=
   v61_l206
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :stream-id
     :subject
     :user-id))))


(def v63_l213 (kind/doc #'views/edits-long))


(def
 v64_l215
 (-> (views/edits-long sample-messages) tc/column-names sort))


(deftest
 t65_l218
 (is
  (=
   v64_l215
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v66_l222 (kind/doc #'views/topic-links-long))


(def
 v67_l224
 (-> (views/topic-links-long sample-messages) tc/column-names sort))


(deftest
 t68_l227
 (is
  (= v67_l224 '(:channel :link-text :link-url :message-id :stream-id))))


(def v70_l232 (kind/doc #'anon/user-key))


(def v71_l234 (anon/user-key 42))


(deftest
 t72_l236
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v71_l234)))


(def
 v74_l240
 [(= (anon/user-key 42) (anon/user-key 42)) (anon/user-key nil)])


(deftest t75_l243 (is (= v74_l240 [true nil])))


(def v76_l245 (kind/doc #'anon/subject-key))


(def v77_l247 (anon/subject-key "channel introductions"))


(deftest
 t78_l249
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v77_l247)))


(def v79_l251 (kind/doc #'anon/anonymized-timeline))


(def
 v80_l253
 (-> (anon/anonymized-timeline sample-messages) tc/column-names sort))


(deftest
 t81_l256
 (is
  (=
   v80_l253
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


(def v82_l260 (kind/doc #'anon/anonymized-reactions))


(def
 v83_l262
 (-> (anon/anonymized-reactions sample-messages) tc/column-names sort))


(deftest
 t84_l265
 (is
  (=
   v83_l262
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :reactor-user-key
     :stream-id
     :subject-key))))


(def v85_l269 (kind/doc #'anon/anonymized-edits))


(def
 v86_l271
 (-> (anon/anonymized-edits sample-messages) tc/column-names sort))


(deftest
 t87_l274
 (is
  (=
   v86_l271
   '(:channel
     :edit-ts
     :editor-user-key
     :message-id
     :prev-stream
     :prev-subject-key
     :stream-id))))


(def v89_l279 (kind/doc #'nar/ts->month-date))


(def v90_l281 (nar/ts->month-date 1725611765))


(deftest t91_l283 (is (= v90_l281 (java.time.LocalDate/of 2024 9 1))))


(def v92_l285 (kind/doc #'nar/ts->year-month))


(def v93_l287 (nar/ts->year-month 1725611765))


(deftest t94_l289 (is (= v93_l287 "2024-09")))


(def v95_l291 (kind/doc #'nar/ts->year))


(def v96_l293 (nar/ts->year 1725611765))


(deftest t97_l295 (is (= v96_l293 2024)))


(def v98_l297 (kind/doc #'nar/with-time-columns))


(def
 v99_l299
 (->
  (nar/with-time-columns sample-anon)
  tc/column-names
  set
  (clojure.set/intersection #{:month-date :year :year-month})))


(deftest t100_l304 (is (= v99_l299 #{:month-date :year :year-month})))


(def v101_l306 (kind/doc #'nar/channel-lifecycle))


(def
 v102_l308
 (-> (nar/channel-lifecycle sample-with-time) tc/column-names sort))


(deftest
 t103_l311
 (is
  (=
   v102_l308
   '(:active-months
     :channel
     :distinct-users
     :first-date
     :last-date
     :total))))


(def v104_l314 (kind/doc #'nar/channels-by-name-pattern))


(def
 v105_l316
 (nar/channels-by-name-pattern sample-with-time #"civitas|gratitude"))


(deftest t106_l318 (is (= v105_l316 ["clojurecivitas" "gratitude"])))


(def v107_l320 (kind/doc #'nar/channels-by-shared-users))


(def
 v109_l326
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


(deftest t110_l330 (is (contains? v109_l326 "clojurecivitas")))


(def v111_l332 (kind/doc #'nar/first-posters-of-channel))


(def
 v112_l334
 (->
  (nar/first-posters-of-channel sample-with-time "clojurecivitas" 5)
  tc/column-names
  sort))


(deftest t113_l337 (is (= v112_l334 '(:first-post-date :user-key))))


(def v114_l339 (kind/doc #'nar/prior-channels-of-newcomers))


(def
 v115_l341
 (->
  (nar/prior-channels-of-newcomers
   sample-with-time
   "clojurecivitas"
   "2025-10")
  tc/column-names
  sort))


(deftest
 t116_l344
 (is (= v115_l341 '(:newcomers-touched :prior-channel))))


(def v117_l346 (kind/doc #'nar/channel-monthly-activity))


(def
 v118_l348
 (->
  (nar/channel-monthly-activity sample-with-time #{"clojurecivitas"})
  tc/column-names
  sort))


(deftest t119_l351 (is (= v118_l348 '(:channel :month-date :msgs))))


(def v121_l355 (kind/doc #'graph/user-channel-sets))


(def
 v123_l359
 (let
  [u->c
   (graph/user-channel-sets sample-with-time)
   [_ chans]
   (first u->c)]
  (set? chans)))


(deftest t124_l363 (is (true? v123_l359)))


(def v125_l365 (kind/doc #'graph/channel-comembership-graph))


(def
 v126_l367
 (let
  [g (graph/channel-comembership-graph sample-with-time :min-shared 1)]
  (= (set sample-channels) (.vertexSet g))))


(deftest t127_l370 (is (true? v126_l367)))


(def v128_l372 (kind/doc #'graph/user-copresence-graph))


(def
 v129_l374
 (let
  [g
   (graph/user-copresence-graph
    sample-with-time
    :min-shared
    2
    :min-channels
    2)]
  (pos? (count (.vertexSet g)))))


(deftest t130_l378 (is (true? v129_l374)))


(def v131_l380 (kind/doc #'graph/migration-graph))


(def
 v133_l385
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


(deftest t134_l389 (is (true? v133_l385)))


(def v135_l391 (kind/doc #'graph/betweenness))


(def
 v136_l393
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   scores
   (graph/betweenness g)]
  (= (.vertexSet g) (set (keys scores)))))


(deftest t137_l397 (is (true? v136_l393)))


(def v138_l399 (kind/doc #'graph/girvan-newman))


(def
 v139_l401
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/girvan-newman g 2)]
  (count clusters)))


(deftest t140_l405 (is (= v139_l401 2)))


(def v141_l407 (kind/doc #'graph/label-propagation))


(def
 v142_l409
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/label-propagation g)]
  (every? set? clusters)))


(deftest t143_l413 (is (true? v142_l409)))


(def v144_l415 (kind/doc #'graph/->cytoscape-elements))


(def
 v145_l417
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   e
   (graph/->cytoscape-elements g)]
  (set (keys e))))


(deftest t146_l421 (is (= v145_l417 #{:nodes :edges})))


(def v147_l423 (kind/doc #'graph/->dot))


(def
 v148_l425
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   dot
   (graph/->dot g :directed false)]
  (and (string? dot) (clojure.string/starts-with? dot "graph "))))


(deftest t149_l430 (is (true? v148_l425)))
