;; # Pulling and caching channels
;;
;; `scicloj.zulipdata.pull` is the workhorse layer above the REST
;; client. It walks a channel's full message history in *windows*,
;; caches each window on disk, and stitches the windows back together.
;; This chapter explains the cache model, then tours every public
;; function.
;;
;; The big idea: each window is identified by
;; `(channel-name, anchor-id, batch-size)`. Once a window has been
;; fetched, it is cached, and any future call with the same triple is
;; served from disk. That makes pulls **resumable** (a crashed pull
;; picks up where it left off), **idempotent** (re-running is free),
;; and **incremental** (only new tail windows need to be re-fetched).

(ns zulipdata-book.pull
  (:require
   [scicloj.zulipdata.pull :as pull]
   [scicloj.kindly.v4.kind :as kind]))

;; ## Where the cache lives
;;
;; The cache is backed by [pocket](https://github.com/scicloj/pocket),
;; which serialises values with [nippy](https://github.com/taoensso/nippy)
;; under a directory we control. The directory is read from
;; `ZULIP_CACHE_DIR`, falling back to
;; `~/.cache/zulipdata-clojurians/`. The directory is created on first
;; use.
;;
;; Each cached window lives in a sharded subdirectory keyed by a hash
;; of the call. You should not need to inspect the cache directly —
;; but if you ever need to start over for a particular window, the
;; right move is `(pocket/invalidate! ...)`, not `rm -rf`.

;; ## Listing channels you can pull
;;
;; `public-channel-names` filters `/streams` down to channels that are
;; either fully public or web-public — i.e. the ones a research bot
;; can read.

(def public-channels (pull/public-channel-names))

(count public-channels)

(take 5 (sort public-channels))

;; ## Pulling one window
;;
;; `fetch-window` is the cached unit. It returns the raw page map
;; from Zulip — the same shape `client/get-messages` returns,
;; including the `:found_newest` flag the walker uses.

;; The first call hits the network; the second is served from the
;; cache. Both are identical from the caller's point of view. We
;; reach for the small `kindly-dev` channel as our example.

(def first-window
  (pull/fetch-window "kindly-dev" 0 100))

(-> first-window :messages count)

(:found_anchor first-window)

(kind/test-last
 (= false))

;; The walker stops once it sees `:found_newest` true, but a single
;; small window like this one usually does not reach the tip:

(:found_newest first-window)

;; ## The default batch size
;;
;; `default-batch-size` is the number of messages requested per window
;; when callers do not specify one. Zulip caps it around 5000, and
;; that is also our default — large enough that a busy channel takes
;; only a few windows, small enough to stay well within request
;; limits.

pull/default-batch-size

(kind/test-last
 (= 5000))

;; ## Walking a whole channel
;;
;; `pull-channel!` walks forward from a starting anchor in cached
;; windows. The result is a map with the page list and the total
;; message count — `:pages` keeps every window so you can flatten or
;; reconsume them, and `:message-count` summarises.

;; To walk from the very beginning, start at id zero — the first real
;; message will satisfy the anchor and the walk runs from there.

(def kindly-walk
  (pull/pull-channel! "kindly-dev" 0))

(:message-count kindly-walk)

(count (:pages kindly-walk))

;; ## Flattening pages into messages
;;
;; A walk's `:pages` is a vector of raw page maps. `all-messages`
;; concatenates their `:messages` and de-duplicates by `:id` (windows
;; are non-overlapping by construction; the dedup is belt-and-braces).

(def kindly-messages (pull/all-messages kindly-walk))

(count kindly-messages)

(kind/test-last
 (= (:message-count kindly-walk)))

;; A single message:

(-> kindly-messages first (select-keys [:id :sender_full_name :timestamp]))

;; ## Pulling several channels at once
;;
;; `pull-channels!` is the convenient batch form. It looks up each
;; channel's first-message id from `/streams`, walks each channel,
;; and returns a map keyed by name. Channels that do not exist on
;; the server are collected under `:not-found`.

(def pulled
  (pull/pull-channels! ["kindly-dev" "definitely-not-a-real-channel"]))

(get-in pulled ["kindly-dev" :message-count])

(kind/test-last
 (= (:message-count kindly-walk)))

;; The unknown channel ends up in `:not-found`:

(:not-found pulled)

(kind/test-last
 (= ["definitely-not-a-real-channel"]))

;; The successful entries also carry the stream id and the channel's
;; first-message id — those are looked up by the function and
;; included for downstream use:

(-> (get pulled "kindly-dev")
    (select-keys [:stream-id :first-message-id :message-count]))

;; ## Pulling everything public
;;
;; `pull-public-channels!` is the convenience wrapper used by the
;; full-corpus analyses: it pulls every channel in
;; `public-channel-names` with whatever options you pass. We do **not**
;; run it here — a full pull on a fresh cache can take minutes — but
;; this is the call to reach for when building a corpus-wide dataset.

;; ## Catching up: the `:refresh-tip?` option
;;
;; By default, repeated calls are entirely cache-served. That is
;; usually what you want — but it means a re-run misses any messages
;; posted since the last pull, because the final cached window will
;; have been stored with `:found_newest true`.
;;
;; Pass `:refresh-tip? true` to invalidate any cached page with
;; `:found_newest true`, re-fetch it, and continue walking if new full
;; windows now appear. This is the right option for keeping a corpus
;; up to date without rebuilding it from scratch.

(def kindly-fresh
  (pull/pull-channel! "kindly-dev" 0 :refresh-tip? true))

(:message-count kindly-fresh)

;; The fresh count is at least the cached count — typically equal, or
;; slightly larger if new messages arrived since:

(>= (:message-count kindly-fresh) (:message-count kindly-walk))

(kind/test-last
 (= true))

;; ## Where to go next
;;
;; You now have raw messages — vectors of maps with Zulip's snake_case
;; field names. The next step is shaping them into datasets:
;;
;; - **Tablecloth views** — `scicloj.zulipdata.views` projects raw
;;   messages into one-row-per-thing datasets (timeline, reactions,
;;   edits, links).
;; - **Anonymized views** — `scicloj.zulipdata.anonymize` produces
;;   parallel datasets with sender identities and topic strings
;;   replaced by stable hash keys.
;; - **API Reference** — every public function in one chapter, with
;;   docstrings and a worked example each.
