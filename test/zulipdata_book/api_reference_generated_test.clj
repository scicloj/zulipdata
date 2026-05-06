(ns
 zulipdata-book.api-reference-generated-test
 (:require
  [scicloj.zulipdata.client :as client]
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.views :as views]
  [scicloj.zulipdata.emoji :as emoji]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.zulipdata.graph :as graph]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l57
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def v4_l60 (def sample-pull (pull/pull-channels! sample-channels)))


(def
 v5_l63
 (def sample-messages (pull/all-channel-messages sample-pull)))


(def
 v6_l66
 (def sample-timeline (views/messages-timeline sample-messages)))


(def
 v7_l69
 (def sample-anon (anon/anonymized-timeline sample-messages)))


(def v8_l72 (def sample-with-time (nar/with-time-columns sample-anon)))


(def v10_l77 (kind/doc #'client/base-url))


(def v11_l79 client/base-url)


(deftest
 t12_l81
 (is (= v11_l79 "https://clojurians.zulipchat.com/api/v1")))


(def v13_l83 (kind/doc #'client/api-get))


(def v14_l85 (-> (client/api-get "/server_settings") :realm_name))


(deftest t15_l88 (is (= v14_l85 "Clojurians")))


(def
 v17_l92
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


(deftest t18_l100 (is (= v17_l92 1)))


(def v19_l102 (kind/doc #'client/whoami))


(def v20_l104 (client/whoami))


(deftest
 t21_l106
 (is
  ((fn
    [m]
    (every?
     (set (keys m))
     [:email :full-name :user-id :is-bot :is-admin :role]))
   v20_l104)))


(def v22_l110 (kind/doc #'client/get-me))


(def v23_l112 (-> (client/get-me) :user_id integer?))


(deftest t24_l114 (is (true? v23_l112)))


(def v25_l116 (kind/doc #'client/get-streams))


(def v26_l118 (-> (client/get-streams) :streams count pos?))


(deftest t27_l120 (is (true? v26_l118)))


(def v28_l122 (kind/doc #'client/get-messages))


(def
 v29_l124
 (->
  (client/get-messages
   {:narrow [{:operator "channel", :operand "clojurecivitas"}],
    :anchor "newest",
    :num-before 3,
    :num-after 0})
  :messages
  count))


(deftest t30_l131 (is (= v29_l124 3)))


(def v32_l135 (kind/doc #'pull/default-batch-size))


(def v33_l137 pull/default-batch-size)


(deftest t34_l139 (is (= v33_l137 5000)))


(def v35_l141 (kind/doc #'pull/fetch-window))


(def
 v36_l143
 (-> (pull/fetch-window "clojurecivitas" 0 100) :messages count))


(deftest t37_l146 (is (= v36_l143 100)))


(def v38_l148 (kind/doc #'pull/pull-channel!))


(def
 v40_l152
 (->
  (pull/pull-channel! "clojurecivitas" 0)
  (select-keys [:pages :message-count])
  keys
  set))


(deftest t41_l157 (is (= v40_l152 #{:pages :message-count})))


(def v42_l159 (kind/doc #'pull/all-messages))


(def
 v43_l161
 (let
  [walk
   (pull/pull-channel! "clojurecivitas" 0)
   messages
   (pull/all-messages walk)]
  (= (count messages) (:message-count walk))))


(deftest t44_l165 (is (true? v43_l161)))


(def v45_l167 (kind/doc #'pull/pull-channels!))


(def
 v47_l172
 (->
  (pull/pull-channels! ["clojurecivitas" "no-such-channel"])
  :not-found))


(deftest t48_l175 (is (= v47_l172 ["no-such-channel"])))


(def v49_l177 (kind/doc #'pull/all-channel-messages))


(def
 v50_l179
 (let
  [pulled (pull/pull-channels! ["clojurecivitas"])]
  (=
   (count (pull/all-channel-messages pulled))
   (count (pull/all-messages (get pulled "clojurecivitas"))))))


(deftest t51_l183 (is (true? v50_l179)))


(def v52_l185 (kind/doc #'pull/public-channel-names))


(def v53_l187 (-> (pull/public-channel-names) count pos?))


(deftest t54_l189 (is (true? v53_l187)))


(def v55_l191 (kind/doc #'pull/pull-public-channels!))


(def v57_l199 (kind/doc #'views/messages-timeline))


(def
 v58_l201
 (-> (views/messages-timeline sample-messages) tc/row-count))


(deftest t59_l204 (is (= v58_l201 (count sample-messages))))


(def v61_l208 (-> sample-timeline tc/column-names sort))


(deftest
 t62_l210
 (is
  (=
   v61_l208
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


(def v63_l214 (kind/doc #'views/reactions-long))


(def
 v64_l216
 (-> (views/reactions-long sample-messages) tc/column-names sort))


(deftest
 t65_l219
 (is
  (=
   v64_l216
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :stream-id
     :subject
     :user-id))))


(def v66_l223 (kind/doc #'views/edits-long))


(def
 v67_l225
 (-> (views/edits-long sample-messages) tc/column-names sort))


(deftest
 t68_l228
 (is
  (=
   v67_l225
   '(:channel
     :edit-ts
     :edit-user-id
     :message-id
     :prev-content
     :prev-stream
     :prev-subject
     :stream-id))))


(def v69_l232 (kind/doc #'views/topic-links-long))


(def
 v70_l234
 (-> (views/topic-links-long sample-messages) tc/column-names sort))


(deftest
 t71_l237
 (is
  (= v70_l234 '(:channel :link-text :link-url :message-id :stream-id))))


(def v73_l242 (kind/doc #'emoji/decode-unicode))


(def v74_l244 (emoji/decode-unicode "1f64f"))


(deftest t75_l246 (is (= v74_l244 "🙏")))


(def v77_l251 (emoji/decode-unicode "1f3f4-200d-2620-fe0f"))


(deftest t78_l253 (is (= v77_l251 "🏴‍☠️")))


(def v79_l255 (kind/doc #'emoji/realm-emoji-map))


(def v80_l257 (-> (emoji/realm-emoji-map) vals first keys set))


(deftest
 t81_l259
 (is ((fn [s] (every? s [:id :name :source_url])) v80_l257)))


(def v82_l261 (kind/doc #'emoji/display))


(def v84_l266 (emoji/display nil "unicode_emoji" "1f44d" "+1"))


(deftest t85_l268 (is (= v84_l266 "👍")))


(def
 v87_l273
 (let
  [rm
   (emoji/realm-emoji-map)
   one-id
   (-> rm keys first name)
   result
   (emoji/display rm "realm_emoji" one-id "x")]
  [(vector? result) (= :img (first result))]))


(deftest t88_l278 (is (= v87_l273 [true true])))


(def v90_l282 (kind/doc #'anon/user-key))


(def v91_l284 (anon/user-key 42))


(deftest
 t92_l286
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v91_l284)))


(def
 v94_l290
 [(= (anon/user-key 42) (anon/user-key 42)) (anon/user-key nil)])


(deftest t95_l293 (is (= v94_l290 [true nil])))


(def v96_l295 (kind/doc #'anon/subject-key))


(def v97_l297 (anon/subject-key "channel introductions"))


(deftest
 t98_l299
 (is ((fn [s] (and (string? s) (= 16 (count s)))) v97_l297)))


(def v99_l301 (kind/doc #'anon/anonymized-timeline))


(def
 v100_l303
 (-> (anon/anonymized-timeline sample-messages) tc/column-names sort))


(deftest
 t101_l306
 (is
  (=
   v100_l303
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


(def v102_l310 (kind/doc #'anon/anonymized-reactions))


(def
 v103_l312
 (-> (anon/anonymized-reactions sample-messages) tc/column-names sort))


(deftest
 t104_l315
 (is
  (=
   v103_l312
   '(:channel
     :emoji-code
     :emoji-name
     :message-id
     :message-ts
     :reaction-type
     :reactor-user-key
     :stream-id
     :subject-key))))


(def v105_l319 (kind/doc #'anon/anonymized-edits))


(def
 v106_l321
 (-> (anon/anonymized-edits sample-messages) tc/column-names sort))


(deftest
 t107_l324
 (is
  (=
   v106_l321
   '(:channel
     :edit-ts
     :editor-user-key
     :message-id
     :prev-stream
     :prev-subject-key
     :stream-id))))


(def v109_l329 (kind/doc #'nar/ts->month-date))


(def v110_l331 (nar/ts->month-date 1725611765))


(deftest t111_l333 (is (= v110_l331 (java.time.LocalDate/of 2024 9 1))))


(def v112_l335 (kind/doc #'nar/ts->year-month))


(def v113_l337 (nar/ts->year-month 1725611765))


(deftest t114_l339 (is (= v113_l337 "2024-09")))


(def v115_l341 (kind/doc #'nar/ts->year))


(def v116_l343 (nar/ts->year 1725611765))


(deftest t117_l345 (is (= v116_l343 2024)))


(def v118_l347 (kind/doc #'nar/with-time-columns))


(def
 v119_l349
 (->
  (nar/with-time-columns sample-anon)
  tc/column-names
  set
  (clojure.set/intersection #{:month-date :year :year-month})))


(deftest t120_l354 (is (= v119_l349 #{:month-date :year :year-month})))


(def v121_l356 (kind/doc #'nar/channel-lifecycle))


(def
 v122_l358
 (-> (nar/channel-lifecycle sample-with-time) tc/column-names sort))


(deftest
 t123_l361
 (is
  (=
   v122_l358
   '(:active-months
     :channel
     :distinct-users
     :first-date
     :last-date
     :total))))


(def v124_l364 (kind/doc #'nar/channels-by-name-pattern))


(def
 v125_l366
 (nar/channels-by-name-pattern sample-with-time #"civitas|gratitude"))


(deftest t126_l368 (is (= v125_l366 ["clojurecivitas" "gratitude"])))


(def v127_l370 (kind/doc #'nar/channels-by-shared-users))


(def
 v129_l376
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


(deftest t130_l380 (is (contains? v129_l376 "clojurecivitas")))


(def v131_l382 (kind/doc #'nar/first-posters-of-channel))


(def
 v132_l384
 (->
  (nar/first-posters-of-channel sample-with-time "clojurecivitas" 5)
  tc/column-names
  sort))


(deftest t133_l387 (is (= v132_l384 '(:first-post-date :user-key))))


(def v134_l389 (kind/doc #'nar/prior-channels-of-newcomers))


(def
 v135_l391
 (->
  (nar/prior-channels-of-newcomers
   sample-with-time
   "clojurecivitas"
   "2025-10")
  tc/column-names
  sort))


(deftest
 t136_l394
 (is (= v135_l391 '(:newcomers-touched :prior-channel))))


(def v137_l396 (kind/doc #'nar/channel-monthly-activity))


(def
 v138_l398
 (->
  (nar/channel-monthly-activity sample-with-time #{"clojurecivitas"})
  tc/column-names
  sort))


(deftest t139_l401 (is (= v138_l398 '(:channel :month-date :msgs))))


(def v141_l405 (kind/doc #'graph/user-channel-sets))


(def
 v143_l409
 (let
  [u->c
   (graph/user-channel-sets sample-with-time)
   [_ chans]
   (first u->c)]
  (set? chans)))


(deftest t144_l413 (is (true? v143_l409)))


(def v145_l415 (kind/doc #'graph/channel-comembership-graph))


(def
 v146_l417
 (let
  [g (graph/channel-comembership-graph sample-with-time :min-shared 1)]
  (= (set sample-channels) (.vertexSet g))))


(deftest t147_l420 (is (true? v146_l417)))


(def v148_l422 (kind/doc #'graph/user-copresence-graph))


(def
 v149_l424
 (let
  [g
   (graph/user-copresence-graph
    sample-with-time
    :min-shared
    2
    :min-channels
    2)]
  (pos? (count (.vertexSet g)))))


(deftest t150_l428 (is (true? v149_l424)))


(def v151_l430 (kind/doc #'graph/migration-graph))


(def
 v153_l435
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


(deftest t154_l439 (is (true? v153_l435)))


(def v155_l441 (kind/doc #'graph/betweenness))


(def
 v156_l443
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   scores
   (graph/betweenness g)]
  (= (.vertexSet g) (set (keys scores)))))


(deftest t157_l447 (is (true? v156_l443)))


(def v158_l449 (kind/doc #'graph/girvan-newman))


(def
 v159_l451
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/girvan-newman g 2)]
  (count clusters)))


(deftest t160_l455 (is (= v159_l451 2)))


(def v161_l457 (kind/doc #'graph/label-propagation))


(def
 v162_l459
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   clusters
   (graph/label-propagation g)]
  (every? set? clusters)))


(deftest t163_l463 (is (true? v162_l459)))


(def v164_l465 (kind/doc #'graph/->cytoscape-elements))


(def
 v165_l467
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   e
   (graph/->cytoscape-elements g)]
  (set (keys e))))


(deftest t166_l471 (is (= v165_l467 #{:nodes :edges})))


(def v167_l473 (kind/doc #'graph/->dot))


(def
 v168_l475
 (let
  [g
   (graph/channel-comembership-graph sample-with-time)
   dot
   (graph/->dot g :directed false)]
  (and (string? dot) (clojure.string/starts-with? dot "graph "))))


(deftest t169_l480 (is (true? v168_l475)))
