;; # Quickstart
;;
;; A walkthrough of the `zulipdata` library's core API: authenticating,
;; listing public channels, pulling channel messages, and shaping them
;; into a [tablecloth](https://github.com/scicloj/tablecloth) dataset.
;; Run this notebook end-to-end to confirm your setup works.
;;
;; Credentials are read from `ZULIP_EMAIL` / `ZULIP_API_KEY` env vars
;; or `~/.zuliprc` — see Zulip's [API keys
;; documentation](https://zulip.com/api/api-keys) for how to obtain them.

(ns zulipdata-book.quickstart
  (:require
   ;; Zulipdata client -- Zulip REST API wrapper
   [scicloj.zulipdata.client :as client]
   ;; Zulipdata pull -- paginated, cached channel history
   [scicloj.zulipdata.pull :as pull]
   ;; Zulipdata views -- tablecloth projections of raw messages
   [scicloj.zulipdata.views :as views]
   ;; Zulipdata emoji -- decode reaction emoji-codes for display
   [scicloj.zulipdata.emoji :as emoji]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]
   ;; Tablecloth -- dataset manipulation
   [tablecloth.api :as tc]))

;; ## Authenticating
;;
;; `client/whoami` calls
;; [`/users/me`](https://zulip.com/api/get-own-user) and returns a
;; summary of the authenticated identity:

(def me (client/whoami))

me

;; ## Listing channels
;;
;; `pull/public-channel-names` returns every channel the bot can read —
;; both fully public (login-gated) and the smaller subset that is
;; web-public (readable without logging in).

(def public-channels (pull/public-channel-names))

(count public-channels)

;; A prefix:

(take 5 (sort public-channels))

;; `pull/web-public-channel-names` returns just the web-public subset.
;; Throughout this book we draw demo data from these channels so that
;; message content can be shown without leaking anything login-gated.

(def web-public (pull/web-public-channel-names))

web-public

;; ## Pulling messages from one channel
;;
;; `pull/pull-channels!` walks forward through a list of channels in
;; cached windows. The first run populates the disk cache; subsequent
;; runs are served from cache. We pull `clojurecivitas`, a web-public
;; channel.

(def pulled
  (pull/pull-channels! ["clojurecivitas"]))

(def message-count
  (get-in pulled ["clojurecivitas" :message-count]))

message-count

;; Flatten the cached windows into a single sequence of raw messages:

(def raw-messages
  (pull/all-messages (get pulled "clojurecivitas")))

(count raw-messages)

(kind/test-last
 (= message-count))

;; A single raw message — one map per Zulip message, with sender,
;; topic, content, timestamps, reactions, and edit history:

(first raw-messages)

;; ## Building a timeline view
;;
;; `views/messages-timeline` projects raw messages into a tablecloth
;; dataset with one row per message and simple-valued columns only:

(def timeline (views/messages-timeline raw-messages))

(-> timeline
    (tc/order-by :instant :desc))

(tc/row-count timeline)

(kind/test-last
 (= message-count))

;; The dataset's columns:

(tc/column-names timeline)

;; ## Top reactions
;;
;; `views/reactions-long` is another view, with one row per reaction.
;; Reactions come in two kinds. Unicode emoji carry a hyphen-separated
;; codepoint sequence in `:emoji-code` — `"1f64f"` for 🙏,
;; `"1f3f4-200d-2620-fe0f"` for 🏴‍☠️. Custom realm emoji are workspace-uploaded
;; images; their `:emoji-code` is the realm emoji's id, and the URL
;; lives in [`/realm/emoji`](https://zulip.com/api/get-custom-emoji).
;;
;; The `scicloj.zulipdata.emoji` namespace handles both kinds:
;; `emoji/realm-emoji-map` fetches the workspace's custom emoji,
;; and `emoji/display` returns the unicode glyph or a `<img>` hiccup
;; element ready to drop into a `kind/table` cell.

(def realm-emoji (emoji/realm-emoji-map))

;; A self-contained pipeline: pull a few crowded web-public channels,
;; project, decode, aggregate, sort, render. Unicode glyphs become
;; characters; realm emoji become inline `<img>` tags nested inside
;; the table cells:

(-> (pull/pull-channels! ["clojurecivitas" "scicloj-webpublic"
                          "gratitude" "events"])
    pull/all-channel-messages
    views/reactions-long
    (tc/map-columns :emoji [:reaction-type :emoji-code :emoji-name]
                    (partial emoji/display realm-emoji))
    (tc/group-by [:emoji-name :emoji])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by [:n] [:desc])
    (tc/head 5)
    kind/table)

;; ## Next steps
;;
;; The rest of this book is one chapter per namespace:
;;
;; - [**The REST client**](./zulipdata_book.client.html) — what
;;   `client/whoami` does internally, plus the four
;;   endpoints the library wraps.
;; - [**Pulling and caching channels**](./zulipdata_book.pull.html) —
;;   the cache model behind `pull/pull-channels!`, plus options
;;   like `:refresh` for keeping a corpus up to date.
;; - [**Tablecloth views**](./zulipdata_book.views.html) —
;;   `views/messages-timeline`, `views/reactions-long`,
;;   `views/edits-long`, and `views/topic-links-long`.
;; - [**Anonymized views**](./zulipdata_book.anonymize.html) —
;;   parallel views with sender names and topic strings replaced
;;   by stable hash keys, message content dropped — for sharing
;;   datasets without leaking identities or text.
;; - [**Narrative**](./zulipdata_book.narrative.html) —
;;   date columns, channel lifecycles, and newcomer tracking.
;; - [**Graph views**](./zulipdata_book.graph.html) — co-membership
;;   and co-presence graphs, community detection, and rendering.
;; - [**API Reference**](./zulipdata_book.api_reference.html) —
;;   every public function in one chapter.
