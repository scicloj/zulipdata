;; # Quickstart
;;
;; A walkthrough of the `zulipdata` library's core API: authenticating,
;; listing public channels, pulling channel messages, and shaping them
;; into a tablecloth dataset. Run this notebook end-to-end to confirm
;; your setup works.
;;
;; Credentials are read from `ZULIP_EMAIL` / `ZULIP_API_KEY` env vars
;; or `~/.zuliprc` — see the project README.

(ns zulipdata-book.quickstart
  (:require
   [scicloj.zulipdata.client :as client]
   [scicloj.zulipdata.pull :as pull]
   [scicloj.zulipdata.views :as views]
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]))

;; ## Authenticating
;;
;; `client/whoami` calls `/users/me` and returns a small summary of the
;; authenticated identity.

(def me (client/whoami))

(:email me)

(:user-id me)

;; ## Listing public channels
;;
;; `pull/public-channel-names` returns every channel the bot can read —
;; both fully public and the smaller subset that is web-public.

(def public-channels (pull/public-channel-names))

(count public-channels)

;; A few names:

(take 5 (sort public-channels))

;; ## Pulling messages from one channel
;;
;; `pull/pull-channels!` walks forward through a list of channels in
;; cached windows. The first run populates the disk cache; subsequent
;; runs are served from cache. We pull a single small channel here.

(def pulled
  (pull/pull-channels! ["kindly-dev"]))

(def message-count
  (get-in pulled ["kindly-dev" :message-count]))

message-count

;; Flatten the cached windows into a single seq of raw messages:

(def raw-messages
  (pull/all-messages (get pulled "kindly-dev")))

(count raw-messages)

(kind/test-last
 (= message-count))

;; ## Building a timeline view
;;
;; `views/messages-timeline` projects raw messages into a tablecloth
;; dataset with one row per message and scalar columns only.

(def timeline (views/messages-timeline raw-messages))

(tc/row-count timeline)

(kind/test-last
 (= message-count))

;; The dataset's columns:

(tc/column-names timeline)

;; ## Next steps
;;
;; - `views/reactions-long`, `views/edits-long`, and
;;   `views/topic-links-long` — long-format projections of the
;;   reaction, edit-history, and link sub-records.
;; - `scicloj.zulipdata.anonymize` — anonymized projections
;;   (HMAC-hashed user keys, dropped content) suitable for sharing
;;   dataset views without leaking real names or message text.
;; - `scicloj.zulipdata.narrative` and `scicloj.zulipdata.graph` —
;;   higher-level analysis helpers (channel lifecycles, network
;;   shapes, community detection).
