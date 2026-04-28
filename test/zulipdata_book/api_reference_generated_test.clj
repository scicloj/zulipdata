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
 v3_l46
 (def
  sample-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def v4_l49 (def sample-pull (pull/pull-channels! sample-channels)))


(def
 v5_l52
 (def
  sample-messages
  (->>
   sample-pull
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r))))))


(def
 v6_l57
 (def sample-timeline (views/messages-timeline sample-messages)))


(def
 v7_l60
 (def sample-anon (anon/anonymized-timeline sample-messages)))


(def v8_l63 (def sample-with-time (nar/with-time-columns sample-anon)))


(def v10_l68 (kind/doc #'client/base-url))


(def v11_l70 client/base-url)


(deftest
 t12_l72
 (is (= v11_l70 "https://clojurians.zulipchat.com/api/v1")))


(def v13_l74 (kind/doc #'client/api-get))


(def v14_l76 (-> (client/api-get "/server_settings") :realm_name))


(deftest t15_l79 (is (= v14_l76 "Clojurians")))


(def
 v17_l83
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


(deftest t18_l91 (is (= v17_l83 1)))


(def v19_l93 (kind/doc #'client/whoami))


(def v20_l95 (client/whoami))


(deftest
 t21_l97
 (is
  ((fn
    [m]
    (every?
     (set (keys m))
     [:email :full-name :user-id :is-bot :is-admin :role]))
   v20_l95)))


(def v22_l101 (kind/doc #'client/get-me))


(def v23_l103 (-> (client/get-me) :user_id integer?))


(deftest t24_l105 (is (true? v23_l103)))


(def v25_l107 (kind/doc #'client/get-streams))


(def v26_l109 (-> (client/get-streams) :streams count pos?))


(deftest t27_l111 (is (true? v26_l109)))


(def v28_l113 (kind/doc #'client/get-messages))


(def
 v29_l115
 (->
  (client/get-messages
   {:narrow [{:operator "channel", :operand "kindly-dev"}],
    :anchor "newest",
    :num-before 3,
    :num-after 0})
  :messages
  count))


(deftest t30_l122 (is (= v29_l115 3)))


(def v32_l126 (kind/doc #'pull/default-batch-size))


(def v33_l128 pull/default-batch-size)


(deftest t34_l130 (is (= v33_l128 5000)))


(def v35_l132 (kind/doc #'pull/fetch-window))


(def
 v36_l134
 (-> (pull/fetch-window "kindly-dev" 0 100) :messages count))


(deftest t37_l137 (is (= v36_l134 100)))


(def v38_l139 (kind/doc #'pull/pull-channel!))


(def
 v40_l143
 (->
  (pull/pull-channel! "kindly-dev" 0)
  (select-keys [:pages :message-count])
  keys
  set))


(deftest t41_l148 (is (= v40_l143 #{:pages :message-count})))


(def v42_l150 (kind/doc #'pull/all-messages))


(def
 v43_l152
 (let
  [walk
   (pull/pull-channel! "kindly-dev" 0)
   messages
   (pull/all-messages walk)]
  (= (count messages) (:message-count walk))))


(deftest t44_l156 (is (true? v43_l152)))


(def v45_l158 (kind/doc #'pull/pull-channels!))


(def
 v47_l163
 (-> (pull/pull-channels! ["kindly-dev" "no-such-channel"]) :not-found))


(deftest t48_l166 (is (= v47_l163 ["no-such-channel"])))


(def v49_l168 (kind/doc #'pull/public-channel-names))


(def v50_l170 (-> (pull/public-channel-names) count pos?))


(deftest t51_l172 (is (true? v50_l170)))


(def v52_l174 (kind/doc #'pull/pull-public-channels!))


(def v54_l182 (kind/doc #'views/messages-timeline))


(def
 v55_l184
 (-> (views/messages-timeline sample-messages) tc/row-count))


(deftest t56_l187 (is (= v55_l184 (count sample-messages))))


(def v58_l191 (-> sample-timeline tc/column-names sort))


(deftest
 t59_l193
 (is
  (=
   v58_l191
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


(def v60_l197 (kind/doc #'views/reactions-long))


(def
 v61_l199
 (-> (views/reactions-long sample-messages) tc/column-names sort))


(deftest
 t62_l202
 (is
  (=
   v61_l199
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :stream-id
     :subject
     :user-id))))


(def v63_l206 (kind/doc #'views/edits-long))


(def
 v64_l208
 (-> (views/edits-long sample-messages) tc/column-names sort))


(deftest
 t65_l211
 (is
  (=
   v64_l208
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v66_l215 (kind/doc #'views/topic-links-long))


(def
 v67_l217
 (-> (views/topic-links-long sample-messages) tc/column-names sort))


(deftest
 t68_l220
 (is
  (= v67_l217 '(:channel :link-text :link-url :message-id :stream-id))))


(def v70_l225 (kind/doc #'anon/user-key))


(def v71_l227 (anon/user-key 42))


(deftest
 t72_l229
 (is ((fn [s] (and (string? s) (= 12 (count s)))) v71_l227)))


(def
 v74_l233
 [(= (anon/user-key 42) (anon/user-key 42)) (anon/user-key nil)])


(deftest t75_l236 (is (= v74_l233 [true nil])))


(def v76_l238 (kind/doc #'anon/subject-key))


(def v77_l240 (anon/subject-key "channel introductions"))


(deftest
 t78_l242
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v77_l240)))


(def v79_l244 (kind/doc #'anon/anonymized-timeline))


(def
 v80_l246
 (-> (anon/anonymized-timeline sample-messages) tc/column-names sort))


(deftest
 t81_l249
 (is
  (=
   v80_l246
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


(def v82_l253 (kind/doc #'anon/anonymized-reactions))


(def
 v83_l255
 (-> (anon/anonymized-reactions sample-messages) tc/column-names sort))


(deftest
 t84_l258
 (is
  (=
   v83_l255
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :reactor-user-key
     :stream-id
     :subject-key))))


(def v85_l262 (kind/doc #'anon/anonymized-edits))


(def
 v86_l264
 (-> (anon/anonymized-edits sample-messages) tc/column-names sort))


(deftest
 t87_l267
 (is
  (=
   v86_l264
   '(:channel
     :edit-ts
     :editor-user-key
     :message-id
     :prev-stream
     :prev-subject-key
     :stream-id))))


(def v89_l272 (kind/doc #'nar/ts->month-date))


(def v90_l274 (nar/ts->month-date 1725611765))


(deftest t91_l276 (is (= v90_l274 (java.time.LocalDate/of 2024 9 1))))


(def v92_l278 (kind/doc #'nar/ts->year-month))


(def v93_l280 (nar/ts->year-month 1725611765))


(deftest t94_l282 (is (= v93_l280 "2024-09")))


(def v95_l284 (kind/doc #'nar/ts->year))


(def v96_l286 (nar/ts->year 1725611765))


(deftest t97_l288 (is (= v96_l286 2024)))


(def v98_l290 (kind/doc #'nar/with-time-columns))


(def
 v99_l292
 (->
  (nar/with-time-columns sample-anon)
  tc/column-names
  set
  (clojure.set/intersection #{:month-date :year :year-month})))


(deftest t100_l297 (is (= v99_l292 #{:month-date :year :year-month})))


(def v101_l299 (kind/doc #'nar/channel-lifecycle))


(def
 v102_l301
 (-> (nar/channel-lifecycle sample-with-time) tc/column-names sort))


(deftest
 t103_l304
 (is
  (=
   v102_l301
   '(:active-months
     :channel
     :distinct-users
     :first-date
     :last-date
     :total))))


(def v104_l307 (kind/doc #'nar/channels-by-name-pattern))


(def
 v105_l309
 (nar/channels-by-name-pattern sample-with-time #"clay|tableplot"))


(deftest t106_l311 (is (= v105_l309 ["clay-dev" "tableplot-dev"])))


(def v107_l313 (kind/doc #'nar/channels-by-shared-users))


(def
 v109_l319
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


(deftest t110_l323 (is (contains? v109_l319 "clay-dev")))


(def v111_l325 (kind/doc #'nar/first-posters-of-channel))


(def
 v112_l327
 (->
  (nar/first-posters-of-channel sample-with-time "kindly-dev" 5)
  tc/column-names
  sort))


(deftest t113_l330 (is (= v112_l327 '(:first-post-date :user-key))))


(def v114_l332 (kind/doc #'nar/prior-channels-of-newcomers))


(def
 v115_l334
 (->
  (nar/prior-channels-of-newcomers
   sample-with-time
   "kindly-dev"
   "2024-09")
  tc/column-names
  sort))


(deftest
 t116_l337
 (is (= v115_l334 '(:newcomers-touched :prior-channel))))


(def v117_l339 (kind/doc #'nar/channel-monthly-activity))


(def
 v118_l341
 (->
  (nar/channel-monthly-activity sample-with-time #{"kindly-dev"})
  tc/column-names
  sort))


(deftest t119_l344 (is (= v118_l341 '(:channel :month-date :msgs))))


(def v121_l348 (kind/doc #'graph/user-channel-sets))


(def
 v123_l352
 (let
  [u->c
   (graph/user-channel-sets sample-with-time)
   [_ chans]
   (first u->c)]
  (set? chans)))


(deftest t124_l356 (is (true? v123_l352)))


(def v125_l358 (kind/doc #'graph/channel-comembership-graph))


(def
 v126_l360
 (let
  [g (graph/channel-comembership-graph sample-with-time :min-shared 1)]
  (= (set sample-channels) (.vertexSet g))))


(deftest t127_l363 (is (true? v126_l360)))


(def v128_l365 (kind/doc #'graph/user-copresence-graph))


(def
 v129_l367
 (let
  [g
   (graph/user-copresence-graph
    sample-with-time
    :min-shared
    2
    :min-channels
    2)]
  (pos? (count (.vertexSet g)))))


(deftest t130_l371 (is (true? v129_l367)))


(def v131_l373 (kind/doc #'graph/migration-graph))


(def
 v133_l378
 (let
  [g
   (graph/migration-graph sample-with-time #{"clay-dev"} :min-users 1)]
  (every?
   (fn [e] (not= (.getEdgeSource g e) (.getEdgeTarget g e)))
   (.edgeSet g))))


(deftest t134_l382 (is (true? v133_l378)))


(def v135_l384 (kind/doc #'graph/betweenness))


(def
 v136_l386
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   scores
   (graph/betweenness g)]
  (= (.vertexSet g) (set (keys scores)))))


(deftest t137_l390 (is (true? v136_l386)))


(def v138_l392 (kind/doc #'graph/girvan-newman))


(def
 v139_l394
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/girvan-newman g 2)]
  (count clusters)))


(deftest t140_l398 (is (= v139_l394 2)))


(def v141_l400 (kind/doc #'graph/label-propagation))


(def
 v142_l402
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/label-propagation g)]
  (every? set? clusters)))


(deftest t143_l406 (is (true? v142_l402)))


(def v144_l408 (kind/doc #'graph/->cytoscape-elements))


(def
 v145_l410
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   e
   (graph/->cytoscape-elements g)]
  (set (keys e))))


(deftest t146_l414 (is (= v145_l410 #{:nodes :edges})))


(def v147_l416 (kind/doc #'graph/->dot))


(def
 v148_l418
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   dot
   (graph/->dot g :directed false)]
  (and (string? dot) (clojure.string/starts-with? dot "graph "))))


(deftest t149_l423 (is (true? v148_l418)))
