;; # Anonymized views
;;
;; The plain views in
;; [`scicloj.zulipdata.views`](./zulipdata_book.views.html)
;; carry real names, topic strings, and message text. That is fine
;; for analyses that stay on your machine — but the
;; moment a chart, a markdown table, or an exported dataset leaves
;; your laptop, real identities and quoted content go with it.
;;
;; `scicloj.zulipdata.anonymize` produces parallel views with:
;;
;; - sender ids replaced by stable 16-hex-character `:user-key`s,
;; - topic strings replaced by stable 16-hex-character `:subject-key`s,
;; - message content dropped (only `:content-length` survives).
;;
;; Same shape, same join keys, no real names or message bodies.

(ns zulipdata-book.anonymize
  (:require
   ;; Zulipdata pull -- paginated, cached channel history
   [scicloj.zulipdata.pull :as pull]
   ;; Zulipdata anonymize -- HMAC-keyed anonymized projections
   [scicloj.zulipdata.anonymize :as anon]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]))

;; ## How the keys are derived
;;
;; Both keys come from [HMAC-SHA256](https://en.wikipedia.org/wiki/HMAC)
;; (a one-way cryptographic hash) with a single committed salt — see
;; `src/scicloj/zulipdata/anonymize.clj`. The salt is in source on
;; purpose: re-running the analysis must produce the same keys, so
;; that follow-up work links back to prior artifacts.
;;
;; This means the published artifacts are **pseudonymous, not
;; anonymous**. Anyone with the salt and access to the original Zulip
;; data can re-identify by re-hashing. The goal is to keep real names
;; and message text from appearing in checked-in markdown, slides, or
;; dashboards — not to be unbreakable.
;;
;; Both keys are 16 hex characters (64 bits) — wide enough that
;; collisions are not a practical concern at this corpus's scale
;; (low-thousands of users, low-thousands of subjects).

;; ## Hashing one value
;;
;; The two key functions, `user-key` and `subject-key`, are exposed
;; for ad-hoc use. They are pure functions, accept `nil`, and return
;; deterministic hex strings.

(anon/user-key 42)

;; The output is stable: hashing the same input always returns the
;; same key.

(= (anon/user-key 42) (anon/user-key 42))

(kind/test-last
 (= true))

;; Different inputs almost certainly hash to different keys:

(not= (anon/user-key 42) (anon/user-key 43))

(kind/test-last
 (= true))

;; A `nil` sender id (which can happen for system messages) maps to
;; `nil` rather than to a hash:

(anon/user-key nil)

(kind/test-last
 (= nil))

(anon/subject-key "channel introductions")

;; ## A small sample
;;
;; A single channel, `kindly-dev`, is enough to illustrate the
;; anonymization layer. The cross-channel patterns come back in the
;; narrative and graph chapters.

(def messages
  (-> (pull/pull-channels! ["kindly-dev"])
      (get "kindly-dev")
      pull/all-messages))

;; ## One row per message — anonymized
;;
;; `anonymized-timeline` mirrors
;; [`views/messages-timeline`](./zulipdata_book.views.html#one-row-per-message)
;; but with sender ids, sender names, subject strings, and message
;; content replaced or removed.

(def anon-timeline (anon/anonymized-timeline messages))

;; Note that `:user-key` and `:subject-key` are hex strings, and
;; there is no `:content` column.

anon-timeline

(tc/row-count anon-timeline)

(kind/test-last
 (= (count messages)))

;; The distinct user-keys in this channel:

(-> anon-timeline :user-key distinct sort)

;; ## One row per reaction — anonymized
;;
;; `anonymized-reactions` mirrors
;; [`views/reactions-long`](./zulipdata_book.views.html#one-row-per-reaction).
;; The emoji name (a community-sentiment signal, not message
;; content) is preserved; the reactor's identity and the message's
;; subject are both anonymized.

(def anon-reactions (anon/anonymized-reactions messages))

anon-reactions

;; ## One row per edit — anonymized
;;
;; `anonymized-edits` mirrors
;; [`views/edits-long`](./zulipdata_book.views.html#one-row-per-edit)
;; with the editor and prior subject anonymized and prior content
;; dropped. `:prev-stream` (a numeric stream id, not personal data) is
;; left as-is.

(def anon-edits (anon/anonymized-edits messages))

anon-edits

;; ## What the anonymized data can — and cannot — answer
;;
;; The anonymized views are designed for questions about who, when,
;; and where — not about what was said.
;;
;; **Can be answered:**
;;
;; - Activity patterns over time, per channel, per user.
;; - Cohort tenure, retention, cross-channel migration.
;; - Reaction culture (emoji names are preserved).
;; - Edit rates and topic moves.
;; - Subject *recurrence*: same subject-key in many messages means
;;   the same topic thread, even though the topic text is hidden.
;;
;; **Cannot be answered without un-anonymizing:**
;;
;; - What was discussed (no content, no subject text).
;; - Who specifically did what (no names).
;; - Sentiment beyond what reactions carry.

;; ## Where to go next
;;
;; - [**Narrative**](./zulipdata_book.narrative.html) —
;;   `scicloj.zulipdata.narrative` adds time columns,
;;   channel-lifecycle summaries, and newcomer-tracking helpers
;;   that operate on the anonymized timeline.
;; - [**Graph views**](./zulipdata_book.graph.html) —
;;   `scicloj.zulipdata.graph` builds co-membership and
;;   co-presence graphs from the same anonymized timeline.
;; - [**API Reference**](./zulipdata_book.api_reference.html) —
;;   every public function in one chapter, with docstrings and a
;;   worked example each.
