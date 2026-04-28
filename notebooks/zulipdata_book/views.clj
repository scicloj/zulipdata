;; # Tablecloth views
;;
;; Raw Zulip messages are nested maps with snake_case keys. Useful for
;; some things, awkward for analysis. `scicloj.zulipdata.views`
;; offers four projections that turn a sequence of raw messages into
;; tablecloth datasets — flat, scalar-only, ready for grouping and
;; plotting.
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

;; ## Setting up a small fixture
;;
;; We use the same four-channel sample as the later chapters: small
;; enough to render quickly, varied enough that every view has
;; non-empty rows. Subsequent runs are cache-served.
;;
;; `pull/pull-channels!` returns a map keyed by channel name plus a
;; `:not-found` entry for any unknown names. The `(filter (string? k))`
;; step keeps only the channel-keyed entries before flattening their
;; pages into a single seq of raw messages. This same idiom recurs in
;; the later chapters.

(def fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"])

(def messages
  (->> (pull/pull-channels! fixture-channels)
       (filter (fn [[k _]] (string? k)))
       (mapcat (fn [[_ r]] (pull/all-messages r)))))

(count messages)

;; ## One row per message
;;
;; `messages-timeline` is the primary view. One row per message,
;; only scalar columns — no reactions list, no edit history, no
;; nested topic-link records. Those live in their own views.

(def timeline (views/messages-timeline messages))

(tc/row-count timeline)

(kind/test-last
 (= (count messages)))

;; The columns:

(tc/column-names timeline)

;; A peek at the first three rows, with a few interesting columns:

(-> timeline
    (tc/select-columns [:id :instant :channel :sender :content-length :edited?])
    (tc/head 3))

;; The `:instant` column is a Java `Instant` derived from
;; `:timestamp` (epoch seconds). Both are kept — `:timestamp` is good
;; for arithmetic, `:instant` for display and time-zone work.

(-> timeline :instant first type)

;; The `:edited?` column is a structural derivation from the raw
;; message — true iff `:last_edit_timestamp` is present.

(-> timeline (tc/select-rows :edited?) tc/row-count)

;; ## One row per reaction
;;
;; A message can have any number of reactions. `reactions-long`
;; flattens them into a one-row-per-reaction dataset, with the
;; message-level identifiers carried alongside.

(def reactions (views/reactions-long messages))

(tc/row-count reactions)

(tc/column-names reactions)

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

(tc/column-names edits)

;; A few rows. We project the columns to keep the table narrow and
;; avoid showing prior message text.

(-> edits
    (tc/select-columns [:message-id :edit-ts :edit-user-id])
    (tc/head 3))

;; ## One row per linked URL
;;
;; Zulip auto-detects URLs in messages and records them in
;; `:topic_links`. `topic-links-long` flattens those into a
;; one-row-per-link dataset.

(def links (views/topic-links-long messages))

(tc/row-count links)

(tc/column-names links)

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
;; - **Anonymized views** — `scicloj.zulipdata.anonymize` mirrors
;;   `messages-timeline`, `reactions-long`, and `edits-long` with
;;   sender names and topic strings replaced by stable hash keys, and
;;   message content dropped. Use those for any artifact that leaves
;;   your machine.
;; - **API Reference** — every public function in one chapter, with
;;   docstrings and a worked example each.
