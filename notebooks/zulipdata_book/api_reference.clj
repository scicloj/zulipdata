;; # API Reference
;;
;; Complete reference for every public function and constant in
;; the zulipdata library:
;;
;; - [`scicloj.zulipdata.client`](./zulipdata_book.client.html) —
;;   REST client for the Clojurians Zulip instance.
;; - [`scicloj.zulipdata.pull`](./zulipdata_book.pull.html) —
;;   paginated, cached pulls of channel history.
;; - [`scicloj.zulipdata.views`](./zulipdata_book.views.html) —
;;   tablecloth projections of raw messages.
;; - [`scicloj.zulipdata.anonymize`](./zulipdata_book.anonymize.html) —
;;   anonymized projections suitable for sharing.
;; - [`scicloj.zulipdata.narrative`](./zulipdata_book.narrative.html) —
;;   date helpers, channel lifecycles, newcomer tracking.
;; - [`scicloj.zulipdata.graph`](./zulipdata_book.graph.html) —
;;   co-membership and co-presence graphs, community detection,
;;   rendering.
;;
;; Each entry shows the docstring, a live example, and a test. The
;; namespace links above lead to the conceptual walkthrough for
;; each — read those for context; this chapter is the API reference.

^{:kindly/hide-code true
  :kindly/options  {:kinds-that-hide-code #{:kind/doc}}}
(ns zulipdata-book.api-reference
  (:require
   ;; Zulipdata client -- Zulip REST API wrapper
   [scicloj.zulipdata.client :as client]
   ;; Zulipdata pull -- paginated, cached channel history
   [scicloj.zulipdata.pull :as pull]
   ;; Zulipdata views -- tablecloth projections of raw messages
   [scicloj.zulipdata.views :as views]
   ;; Zulipdata anonymize -- HMAC-keyed anonymized projections
   [scicloj.zulipdata.anonymize :as anon]
   ;; Zulipdata narrative -- date columns, lifecycles, newcomer tracking
   [scicloj.zulipdata.narrative :as nar]
   ;; Zulipdata graph -- co-membership / co-presence graphs
   [scicloj.zulipdata.graph :as graph]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]))

;; ## Sample data
;;
;; A small pull, reused across every example below. Each
;; layer of the pipeline is bound for direct reuse: `sample-pull`
;; (raw pull result), `sample-messages` (flat seq of raw messages),
;; `sample-timeline` (plain tablecloth view), `sample-anon`
;; (anonymized), `sample-with-time` (anonymized + date columns).

(def sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"])

(def sample-pull
  (pull/pull-channels! sample-channels))

(def sample-messages
  (->> sample-pull
       (filter (fn [[k _]] (string? k)))
       (mapcat (fn [[_ r]] (pull/all-messages r)))))

(def sample-timeline
  (views/messages-timeline sample-messages))

(def sample-anon
  (anon/anonymized-timeline sample-messages))

(def sample-with-time
  (nar/with-time-columns sample-anon))

;; ## `scicloj.zulipdata.client`

(kind/doc #'client/base-url)

client/base-url

(kind/test-last [= "https://clojurians.zulipchat.com/api/v1"])

(kind/doc #'client/api-get)

(-> (client/api-get "/server_settings")
    :realm_name)

(kind/test-last [= "Clojurians"])

;; With query parameters:

(-> (client/api-get "/messages"
                    {"narrow"     (charred.api/write-json-str
                                   [{:operator "channel" :operand "clojurecivitas"}])
                     "anchor"     "newest"
                     "num_before" 1
                     "num_after"  0})
    :messages count)

(kind/test-last [= 1])

(kind/doc #'client/whoami)

(client/whoami)

(kind/test-last [(fn [m] (every? (set (keys m))
                                 [:email :full-name :user-id
                                  :is-bot :is-admin :role]))])

(kind/doc #'client/get-me)

(-> (client/get-me) :user_id integer?)

(kind/test-last [true?])

(kind/doc #'client/get-streams)

(-> (client/get-streams) :streams count pos?)

(kind/test-last [true?])

(kind/doc #'client/get-messages)

(-> (client/get-messages
     {:narrow     [{:operator "channel" :operand "clojurecivitas"}]
      :anchor     "newest"
      :num-before 3
      :num-after  0})
    :messages count)

(kind/test-last [= 3])

;; ## `scicloj.zulipdata.pull`

(kind/doc #'pull/default-batch-size)

pull/default-batch-size

(kind/test-last [= 5000])

(kind/doc #'pull/fetch-window)

(-> (pull/fetch-window "clojurecivitas" 0 100)
    :messages count)

(kind/test-last [= 100])

(kind/doc #'pull/pull-channel!)

;; A complete walk from id zero to the channel's tip. Result keys:

(-> (pull/pull-channel! "clojurecivitas" 0)
    (select-keys [:pages :message-count])
    keys
    set)

(kind/test-last [= #{:pages :message-count}])

(kind/doc #'pull/all-messages)

(let [walk     (pull/pull-channel! "clojurecivitas" 0)
      messages (pull/all-messages walk)]
  (= (count messages) (:message-count walk)))

(kind/test-last [true?])

(kind/doc #'pull/pull-channels!)

;; Successful entries are keyed by name; unknown names land in
;; `:not-found`.

(-> (pull/pull-channels! ["clojurecivitas" "no-such-channel"])
    :not-found)

(kind/test-last [= ["no-such-channel"]])

(kind/doc #'pull/public-channel-names)

(-> (pull/public-channel-names) count pos?)

(kind/test-last [true?])

(kind/doc #'pull/pull-public-channels!)

;; Not run here — a fresh full-corpus pull can take minutes. Pulls
;; every name returned by `pull/public-channel-names` and accepts the
;; same options as `pull-channels!`.

;; ## `scicloj.zulipdata.views`

(kind/doc #'views/messages-timeline)

(-> (views/messages-timeline sample-messages)
    tc/row-count)

(kind/test-last [= (count sample-messages)])

;; The columns:

(-> sample-timeline tc/column-names sort)

(kind/test-last [= '(:channel :client :content :content-length :edited
                              :id :instant :last-edit-ts :sender
                              :sender-id :stream-id :subject :timestamp)])

(kind/doc #'views/reactions-long)

(-> (views/reactions-long sample-messages)
    tc/column-names sort)

(kind/test-last [= '(:channel :emoji-code :emoji-name :message-id
                              :message-ts :reaction-type :stream-id
                              :subject :user-id)])

(kind/doc #'views/edits-long)

(-> (views/edits-long sample-messages)
    tc/column-names sort)

(kind/test-last [= '(:channel :edit-ts :edit-user-id :message-id
                              :prev-content :prev-stream :prev-subject
                              :stream-id)])

(kind/doc #'views/topic-links-long)

(-> (views/topic-links-long sample-messages)
    tc/column-names sort)

(kind/test-last [= '(:channel :link-text :link-url :message-id
                              :stream-id)])

;; ## `scicloj.zulipdata.anonymize`

(kind/doc #'anon/user-key)

(anon/user-key 42)

(kind/test-last [(fn [s] (and (string? s) (= 16 (count s))))])

;; Stable across calls; `nil` passes through:

[(= (anon/user-key 42) (anon/user-key 42))
 (anon/user-key nil)]

(kind/test-last [= [true nil]])

(kind/doc #'anon/subject-key)

(anon/subject-key "channel introductions")

(kind/test-last [(fn [s] (and (string? s) (= 16 (count s))))])

(kind/doc #'anon/anonymized-timeline)

(-> (anon/anonymized-timeline sample-messages)
    tc/column-names sort)

(kind/test-last [= '(:channel :client :content-length :edited :id
                              :last-edit-ts :reaction-count :stream-id
                              :subject-key :timestamp :user-key)])

(kind/doc #'anon/anonymized-reactions)

(-> (anon/anonymized-reactions sample-messages)
    tc/column-names sort)

(kind/test-last [= '(:channel :emoji-code :emoji-name :message-id
                              :message-ts :reaction-type :reactor-user-key
                              :stream-id :subject-key)])

(kind/doc #'anon/anonymized-edits)

(-> (anon/anonymized-edits sample-messages)
    tc/column-names sort)

(kind/test-last [= '(:channel :edit-ts :editor-user-key :message-id
                              :prev-stream :prev-subject-key :stream-id)])

;; ## `scicloj.zulipdata.narrative`

(kind/doc #'nar/ts->month-date)

(nar/ts->month-date 1725611765)

(kind/test-last [= (java.time.LocalDate/of 2024 9 1)])

(kind/doc #'nar/ts->year-month)

(nar/ts->year-month 1725611765)

(kind/test-last [= "2024-09"])

(kind/doc #'nar/ts->year)

(nar/ts->year 1725611765)

(kind/test-last [= 2024])

(kind/doc #'nar/with-time-columns)

(-> (nar/with-time-columns sample-anon)
    tc/column-names
    set
    (clojure.set/intersection #{:month-date :year-month :year}))

(kind/test-last [= #{:month-date :year-month :year}])

(kind/doc #'nar/channel-lifecycle)

(-> (nar/channel-lifecycle sample-with-time)
    tc/column-names sort)

(kind/test-last [= '(:active-months :channel :distinct-users
                                    :first-date :last-date :total)])

(kind/doc #'nar/channels-by-name-pattern)

(nar/channels-by-name-pattern sample-with-time #"civitas|gratitude")

(kind/test-last [= ["clojurecivitas" "gratitude"]])

(kind/doc #'nar/channels-by-shared-users)

;; The seed channel itself appears in the result if it meets the
;; threshold, since by definition its top posters account for 100% of
;; its activity.

(set
 (nar/channels-by-shared-users sample-with-time "clojurecivitas"
                               :share 0.5 :min-msgs 5 :top-n 5))

(kind/test-last [contains? "clojurecivitas"])

(kind/doc #'nar/first-posters-of-channel)

(-> (nar/first-posters-of-channel sample-with-time "clojurecivitas" 5)
    tc/column-names sort)

(kind/test-last [= '(:first-post-date :user-key)])

(kind/doc #'nar/prior-channels-of-newcomers)

(-> (nar/prior-channels-of-newcomers sample-with-time "clojurecivitas" "2025-10")
    tc/column-names sort)

(kind/test-last [= '(:newcomers-touched :prior-channel)])

(kind/doc #'nar/channel-monthly-activity)

(-> (nar/channel-monthly-activity sample-with-time #{"clojurecivitas"})
    tc/column-names sort)

(kind/test-last [= '(:channel :month-date :msgs)])

;; ## `scicloj.zulipdata.graph`

(kind/doc #'graph/user-channel-sets)

;; Map of user-key to the set of channels they posted in.

(let [u->c (graph/user-channel-sets sample-with-time)
      [_ chans] (first u->c)]
  (set? chans))

(kind/test-last [true?])

(kind/doc #'graph/channel-comembership-graph)

(let [g (graph/channel-comembership-graph sample-with-time :min-shared 1)]
  (= (set sample-channels) (.vertexSet g)))

(kind/test-last [true?])

(kind/doc #'graph/user-copresence-graph)

(let [g (graph/user-copresence-graph sample-with-time
                                     :min-shared 2 :min-channels 2)]
  (pos? (count (.vertexSet g))))

(kind/test-last [true?])

(kind/doc #'graph/migration-graph)

;; Edges from each `from-set` source to channels users moved to next.
;; With `clojurecivitas` as the seed, no self-loops:

(let [g (graph/migration-graph sample-with-time #{"clojurecivitas"} :min-users 1)]
  (every? (fn [e] (not= (.getEdgeSource g e) (.getEdgeTarget g e)))
          (.edgeSet g)))

(kind/test-last [true?])

(kind/doc #'graph/betweenness)

(let [g      (graph/channel-comembership-graph sample-with-time)
      scores (graph/betweenness g)]
  (= (.vertexSet g) (set (keys scores))))

(kind/test-last [true?])

(kind/doc #'graph/girvan-newman)

(let [g        (graph/channel-comembership-graph sample-with-time)
      clusters (graph/girvan-newman g 2)]
  (count clusters))

(kind/test-last [= 2])

(kind/doc #'graph/label-propagation)

(let [g        (graph/channel-comembership-graph sample-with-time)
      clusters (graph/label-propagation g)]
  (every? set? clusters))

(kind/test-last [true?])

(kind/doc #'graph/->cytoscape-elements)

(let [g (graph/channel-comembership-graph sample-with-time)
      e (graph/->cytoscape-elements g)]
  (set (keys e)))

(kind/test-last [= #{:nodes :edges}])

(kind/doc #'graph/->dot)

(let [g   (graph/channel-comembership-graph sample-with-time)
      dot (graph/->dot g :directed false)]
  (and (string? dot)
       (clojure.string/starts-with? dot "graph ")))

(kind/test-last [true?])
