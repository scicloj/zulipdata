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
 v3_l44
 (def
  sample-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def v4_l47 (def sample-pull (pull/pull-channels! sample-channels)))


(def
 v5_l50
 (def
  sample-messages
  (->>
   sample-pull
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v6_l55
 (def sample-timeline (views/messages-timeline sample-messages)))


(def
 v7_l58
 (def sample-anon (anon/anonymized-timeline sample-messages)))


(def v8_l61 (def sample-with-time (nar/with-time-columns sample-anon)))


(def v10_l66 (kind/doc #'client/base-url))


(def v11_l68 client/base-url)


(deftest
 t12_l70
 (is (= v11_l68 "https://clojurians.zulipchat.com/api/v1")))


(def v13_l72 (kind/doc #'client/api-get))


(def v14_l74 (-> (client/api-get "/server_settings") :realm_name))


(deftest t15_l77 (is (= v14_l74 "Clojurians")))


(def
 v17_l81
 (->
  (client/api-get
   "/messages"
   {"narrow"
    (charred.api/write-json-str
     [{:operator "channel", :operand "kindly-dev"}]),
    "anchor" "newest",
    "num_before" 1,
    "num_after" 0})
  :messages
  count))


(deftest t18_l89 (is (= v17_l81 1)))


(def v19_l91 (kind/doc #'client/whoami))


(def v20_l93 (client/whoami))


(deftest
 t21_l95
 (is
  ((fn
    [m]
    (every?
     (set (keys m))
     [:email :full-name :user-id :is-bot? :is-admin? :role]))
   v20_l93)))


(def v22_l99 (kind/doc #'client/get-me))


(def v23_l101 (-> (client/get-me) :user_id integer?))


(deftest t24_l103 (is (true? v23_l101)))


(def v25_l105 (kind/doc #'client/get-streams))


(def v26_l107 (-> (client/get-streams) :streams count pos?))


(deftest t27_l109 (is (true? v26_l107)))


(def v28_l111 (kind/doc #'client/get-messages))


(def
 v29_l113
 (->
  (client/get-messages
   {:narrow [{:operator "channel", :operand "kindly-dev"}],
    :anchor "newest",
    :num-before 3,
    :num-after 0})
  :messages
  count))


(deftest t30_l120 (is (= v29_l113 3)))


(def v32_l124 (kind/doc #'pull/default-batch-size))


(def v33_l126 pull/default-batch-size)


(deftest t34_l128 (is (= v33_l126 5000)))


(def v35_l130 (kind/doc #'pull/fetch-window))


(def
 v36_l132
 (-> (pull/fetch-window "kindly-dev" 0 100) :messages count))


(deftest t37_l135 (is (= v36_l132 100)))


(def v38_l137 (kind/doc #'pull/pull-channel!))


(def
 v40_l141
 (->
  (pull/pull-channel! "kindly-dev" 0)
  (select-keys [:pages :message-count])
  keys
  set))


(deftest t41_l146 (is (= v40_l141 #{:pages :message-count})))


(def v42_l148 (kind/doc #'pull/all-messages))


(def
 v43_l150
 (let
  [walk
   (pull/pull-channel! "kindly-dev" 0)
   messages
   (pull/all-messages walk)]
  (= (count messages) (:message-count walk))))


(deftest t44_l154 (is (true? v43_l150)))


(def v45_l156 (kind/doc #'pull/pull-channels!))


(def
 v47_l161
 (-> (pull/pull-channels! ["kindly-dev" "no-such-channel"]) :not-found))


(deftest t48_l164 (is (= v47_l161 ["no-such-channel"])))


(def v49_l166 (kind/doc #'pull/public-channel-names))


(def v50_l168 (-> (pull/public-channel-names) count pos?))


(deftest t51_l170 (is (true? v50_l168)))


(def v52_l172 (kind/doc #'pull/pull-public-channels!))


(def v54_l180 (kind/doc #'views/messages-timeline))


(def
 v55_l182
 (-> (views/messages-timeline sample-messages) tc/row-count))


(deftest t56_l185 (is (= v55_l182 (count sample-messages))))


(def v58_l189 (-> sample-timeline tc/column-names sort))


(deftest
 t59_l191
 (is
  (=
   v58_l189
   '(:channel
     :client
     :content
     :content-length
     :edited?
     :id
     :instant
     :last-edit-ts
     :sender
     :sender-id
     :stream-id
     :subject
     :timestamp))))


(def v60_l195 (kind/doc #'views/reactions-long))


(def
 v61_l197
 (-> (views/reactions-long sample-messages) tc/column-names sort))


(deftest
 t62_l200
 (is
  (=
   v61_l197
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :stream-id
     :subject
     :user-id))))


(def v63_l204 (kind/doc #'views/edits-long))


(def
 v64_l206
 (-> (views/edits-long sample-messages) tc/column-names sort))


(deftest
 t65_l209
 (is
  (=
   v64_l206
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v66_l213 (kind/doc #'views/topic-links-long))


(def
 v67_l215
 (-> (views/topic-links-long sample-messages) tc/column-names sort))


(deftest
 t68_l218
 (is
  (= v67_l215 '(:channel :link-text :link-url :message-id :stream-id))))


(def v70_l223 (kind/doc #'anon/user-key))


(def v71_l225 (anon/user-key 42))


(deftest
 t72_l227
 (is ((fn [s] (and (string? s) (= 12 (count s)))) v71_l225)))


(def
 v74_l231
 [(= (anon/user-key 42) (anon/user-key 42)) (anon/user-key nil)])


(deftest t75_l234 (is (= v74_l231 [true nil])))


(def v76_l236 (kind/doc #'anon/subject-key))


(def v77_l238 (anon/subject-key "channel introductions"))


(deftest
 t78_l240
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v77_l238)))


(def v79_l242 (kind/doc #'anon/anonymized-timeline))


(def
 v80_l244
 (-> (anon/anonymized-timeline sample-messages) tc/column-names sort))


(deftest
 t81_l247
 (is
  (=
   v80_l244
   '(:channel
     :client
     :content-length
     :edited?
     :id
     :last-edit-ts
     :reaction-count
     :stream-id
     :subject-key
     :timestamp
     :user-key))))


(def v82_l251 (kind/doc #'anon/anonymized-reactions))


(def
 v83_l253
 (-> (anon/anonymized-reactions sample-messages) tc/column-names sort))


(deftest
 t84_l256
 (is
  (=
   v83_l253
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :reactor-user-key
     :stream-id
     :subject-key))))


(def v85_l260 (kind/doc #'anon/anonymized-edits))


(def
 v86_l262
 (-> (anon/anonymized-edits sample-messages) tc/column-names sort))


(deftest
 t87_l265
 (is
  (=
   v86_l262
   '(:channel
     :edit-ts
     :editor-user-key
     :message-id
     :prev-stream
     :prev-subject-key
     :stream-id))))


(def v89_l270 (kind/doc #'nar/ts->month-date))


(def v90_l272 (nar/ts->month-date 1725611765))


(deftest t91_l274 (is (= v90_l272 (java.time.LocalDate/of 2024 9 1))))


(def v92_l276 (kind/doc #'nar/ts->year-month))


(def v93_l278 (nar/ts->year-month 1725611765))


(deftest t94_l280 (is (= v93_l278 "2024-09")))


(def v95_l282 (kind/doc #'nar/ts->year))


(def v96_l284 (nar/ts->year 1725611765))


(deftest t97_l286 (is (= v96_l284 2024)))


(def v98_l288 (kind/doc #'nar/with-time-columns))


(def
 v99_l290
 (->
  (nar/with-time-columns sample-anon)
  tc/column-names
  set
  (clojure.set/intersection #{:month-date :year :year-month})))


(deftest t100_l295 (is (= v99_l290 #{:month-date :year :year-month})))


(def v101_l297 (kind/doc #'nar/channel-lifecycle))


(def
 v102_l299
 (-> (nar/channel-lifecycle sample-with-time) tc/column-names sort))


(deftest
 t103_l302
 (is
  (=
   v102_l299
   '(:active-months
     :channel
     :distinct-users
     :first-date
     :last-date
     :total))))


(def v104_l305 (kind/doc #'nar/channels-by-name-pattern))


(def
 v105_l307
 (nar/channels-by-name-pattern sample-with-time #"clay|tableplot"))


(deftest t106_l309 (is (= v105_l307 ["clay-dev" "tableplot-dev"])))


(def v107_l311 (kind/doc #'nar/channels-by-shared-users))


(def
 v109_l317
 (set
  (nar/channels-by-shared-users
   sample-with-time
   "clay-dev"
   :share
   0.4
   :min-msgs
   30
   :top-n
   30)))


(deftest t110_l321 (is (contains? v109_l317 "clay-dev")))


(def v111_l323 (kind/doc #'nar/first-posters-of-channel))


(def
 v112_l325
 (->
  (nar/first-posters-of-channel sample-with-time "kindly-dev" 5)
  tc/column-names
  sort))


(deftest t113_l328 (is (= v112_l325 '(:first-post-date :user-key))))


(def v114_l330 (kind/doc #'nar/prior-channels-of-newcomers))


(def
 v115_l332
 (->
  (nar/prior-channels-of-newcomers
   sample-with-time
   "kindly-dev"
   "2024-09")
  tc/column-names
  sort))


(deftest
 t116_l335
 (is (= v115_l332 '(:newcomers-touched :prior-channel))))


(def v117_l337 (kind/doc #'nar/channel-monthly-activity))


(def
 v118_l339
 (->
  (nar/channel-monthly-activity sample-with-time #{"kindly-dev"})
  tc/column-names
  sort))


(deftest t119_l342 (is (= v118_l339 '(:channel :month-date :msgs))))


(def v121_l346 (kind/doc #'graph/user-channel-sets))


(def
 v123_l350
 (let
  [u->c
   (graph/user-channel-sets sample-with-time)
   [_ chans]
   (first u->c)]
  (set? chans)))


(deftest t124_l354 (is (true? v123_l350)))


(def v125_l356 (kind/doc #'graph/channel-comembership-graph))


(def
 v126_l358
 (let
  [g (graph/channel-comembership-graph sample-with-time :min-shared 1)]
  (= (set sample-channels) (.vertexSet g))))


(deftest t127_l361 (is (true? v126_l358)))


(def v128_l363 (kind/doc #'graph/user-copresence-graph))


(def
 v129_l365
 (let
  [g
   (graph/user-copresence-graph
    sample-with-time
    :min-shared
    2
    :min-channels
    2)]
  (pos? (count (.vertexSet g)))))


(deftest t130_l369 (is (true? v129_l365)))


(def v131_l371 (kind/doc #'graph/migration-graph))


(def
 v133_l376
 (let
  [g
   (graph/migration-graph sample-with-time #{"clay-dev"} :min-users 1)]
  (every?
   (fn [e] (not= (.getEdgeSource g e) (.getEdgeTarget g e)))
   (.edgeSet g))))


(deftest t134_l380 (is (true? v133_l376)))


(def v135_l382 (kind/doc #'graph/betweenness))


(def
 v136_l384
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   scores
   (graph/betweenness g)]
  (= (.vertexSet g) (set (keys scores)))))


(deftest t137_l388 (is (true? v136_l384)))


(def v138_l390 (kind/doc #'graph/girvan-newman))


(def
 v139_l392
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/girvan-newman g 2)]
  (count clusters)))


(deftest t140_l396 (is (= v139_l392 2)))


(def v141_l398 (kind/doc #'graph/label-propagation))


(def
 v142_l400
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/label-propagation g)]
  (every? set? clusters)))


(deftest t143_l404 (is (true? v142_l400)))


(def v144_l406 (kind/doc #'graph/->cytoscape-elements))


(def
 v145_l408
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   e
   (graph/->cytoscape-elements g)]
  (set (keys e))))


(deftest t146_l412 (is (= v145_l408 #{:nodes :edges})))


(def v147_l414 (kind/doc #'graph/->dot))


(def
 v148_l416
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   dot
   (graph/->dot g :directed? false)]
  (and (string? dot) (clojure.string/starts-with? dot "graph "))))


(deftest t149_l421 (is (true? v148_l416)))
