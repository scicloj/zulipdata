;; # Tablecloth views
;;
;; Raw Zulip messages — pulled via
;; [`scicloj.zulipdata.pull`](./zulipdata_book.pull.html) — are
;; nested maps with snake_case keys. Useful for some things,
;; awkward for analysis. `scicloj.zulipdata.views` offers four
;; projections that turn a sequence of raw messages into tablecloth
;; datasets — flat, scalar-only, ready for grouping and plotting.
;;
;; Two design choices worth knowing about:
;;
;; - **Raw messages stay the source of truth.** The views are computed
;;   on demand; nothing is cached at the dataset layer. Pulling once
;;   and re-projecting many times is cheap.
;; - **Each view is "long" by structure.** One row per message in the
;;   timeline, one row per (message, reaction) in `reactions-long`,
;;   and so on. That keeps later `tc/group-by` calls straightforward.

(ns zulipdata-book.views
  (:require
   [scicloj.zulipdata.pull :as pull]
   [scicloj.zulipdata.views :as views]
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]))

;; ## Setting up a fixture
;;
;; We use a four-channel **web-public** fixture throughout this chapter
;; so the view contents — sender names, topic strings, message bodies —
;; can be shown without leaking anything login-gated. Web-public means
;; the channel is readable on `clojurians.zulipchat.com` without a
;; Zulip account; see
;; [**Web-public channels**](./zulipdata_book.client.html#web-public-channels)
;; in the client chapter.
;;
;; The four channels are small enough to render quickly, varied enough
;; that every view has non-empty rows, and have overlapping
;; contributors so the cross-channel analyses in
;; [Narrative helpers](./zulipdata_book.narrative.html) and
;; [Graph views](./zulipdata_book.graph.html) are non-trivial.
;; Subsequent runs are cache-served.

(def fixture-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"])

(def messages
  (->> (pull/pull-channels! fixture-channels)
       (filter (fn [[k _]] (string? k)))
       (mapcat (fn [[_ r]] (pull/all-messages r)))))

(count messages)

;; ## One row per message
;;
;; `messages-timeline` is the primary view. One row per message,
;; only scalar columns — no reactions list, no edit history, no
;; nested topic-link records. Those live in their own views. For
;; an anonymized parallel that drops sender names and message
;; content, see
;; [`anonymized-timeline`](./zulipdata_book.anonymize.html#one-row-per-message-anonymized).

(def timeline (views/messages-timeline messages))

(tc/row-count timeline)

(kind/test-last
 (= (count messages)))

;; The columns (sorted alphabetically):

(-> timeline tc/column-names sort)

(kind/test-last
 (= '(:channel :client :content :content-length :edited :id :instant
               :last-edit-ts :sender :sender-id :stream-id :subject :timestamp)))

;; The whole dataset, freshest first. Inline emoji codes
;; (`:gratitude:`, `:pray:`) and `@**Real Name**` mentions are how
;; Zulip tags content; they pass through verbatim.

(-> timeline
    (tc/order-by :instant :desc))

;; The `:instant` column is a Java `Instant` derived from
;; `:timestamp` (epoch seconds). Both are kept — `:timestamp` is good
;; for arithmetic, `:instant` for display and time-zone work.

(-> timeline :instant first type)

(kind/test-last
 (= java.time.Instant))

;; The `:edited` column is a structural derivation from the raw
;; message — true iff `:last_edit_timestamp` is present:

(-> timeline (tc/select-rows :edited) tc/row-count)

;; ## One row per reaction
;;
;; A message can have any number of reactions. `reactions-long`
;; flattens them into a one-row-per-reaction dataset, with the
;; message-level identifiers carried alongside.

(def reactions (views/reactions-long messages))

(tc/row-count reactions)

(-> reactions tc/column-names sort)

(kind/test-last
 (= '(:channel :emoji-code :emoji-name :message-id :message-ts
               :reaction-type :stream-id :subject :user-id)))

;; Top emoji across the fixture, by reaction count:

(-> reactions
    (tc/group-by [:emoji-name])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by [:n] [:desc])
    (tc/head 5))

;; ## One row per edit
;;
;; Zulip stores an `edit_history` for every message that has been
;; edited or moved. `edits-long` flattens those events. Note: an
;; "edit" here may be a content edit, a topic move, or a stream move
;; — `:prev-content`, `:prev-subject`, and `:prev-stream` indicate
;; which by being non-nil.

(def edits (views/edits-long messages))

(tc/row-count edits)

(-> edits tc/column-names sort)

(kind/test-last
 (= '(:channel :edit-ts :edit-user-id :message-id
               :prev-content :prev-stream :prev-subject :stream-id)))

;; A few rows. We project the columns to keep the table narrow:

(-> edits
    (tc/select-columns [:message-id :edit-ts :edit-user-id
                        :prev-subject :prev-stream])
    (tc/order-by :edit-ts :desc)
    (tc/head 5))

;; ## One row per linked URL
;;
;; Zulip auto-detects URLs in messages and records them in
;; `:topic_links`. `topic-links-long` flattens those into a
;; one-row-per-link dataset.

(def links (views/topic-links-long messages))

(tc/row-count links)

(tc/column-names links)

(kind/test-last
 (= [:message-id :stream-id :channel :link-text :link-url]))

;; The most-linked URL hosts:

(-> links
    (tc/add-column :host
                   (fn [ds]
                     (mapv #(some-> %
                                    (java.net.URI.)
                                    .getHost)
                           (:link-url ds))))
    (tc/group-by [:host])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by [:n] [:desc])
    (tc/head 5))

;; ## A note on join keys
;;
;; All four views carry `:message-id` (or `:id` in the timeline) and
;; `:stream-id` so you can join them back together when you need both
;; a message's content and its reactions, or both its content and its
;; links. Each view is independently complete — joins are *additional*
;; structure, not required structure.

;; ## Where to go next
;;
;; - [**Anonymized views**](./zulipdata_book.anonymize.html) —
;;   `scicloj.zulipdata.anonymize` mirrors `messages-timeline`,
;;   `reactions-long`, and `edits-long` with sender names and topic
;;   strings replaced by stable hash keys, and message content
;;   dropped. Use those for any artifact derived from non-web-public
;;   channels.
;; - [**Narrative helpers**](./zulipdata_book.narrative.html) —
;;   adds time columns, channel-lifecycle summaries, and
;;   newcomer-tracking helpers built on top of the timeline.
;; - [**API Reference**](./zulipdata_book.api_reference.html) —
;;   every public function in one chapter, with docstrings and a
;;   worked example each.
