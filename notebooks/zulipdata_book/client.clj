;; # The REST client
;;
;; `scicloj.zulipdata.client` is the lowest layer of the library: a thin
;; wrapper over Zulip's HTTP REST API for the
;; [Clojurians](https://clojurians.zulipchat.com) instance. Almost all
;; analyses can stay on the higher-level `pull` namespace, but it helps
;; to know what is happening underneath, and the client is the right
;; tool when you need to call an endpoint the rest of the library does
;; not cover.
;;
;; This chapter walks through:
;;
;; - How credentials are resolved.
;; - The four endpoints the library exposes (`whoami`, `get-me`,
;;   `get-streams`, `get-messages`).
;; - The general-purpose `api-get` for any other endpoint.

(ns zulipdata-book.client
  (:require
   [scicloj.zulipdata.client :as client]
   [scicloj.kindly.v4.kind :as kind]))

;; ## Credentials
;;
;; The client looks for credentials in two places, in order:
;;
;; 1. The environment variables `ZULIP_EMAIL` and `ZULIP_API_KEY`.
;; 2. The `[api]` section of `~/.zuliprc` (the standard Zulip CLI
;;    config file), with `email`, `key`, and `site = https://clojurians.zulipchat.com`.
;;
;; The environment wins if both are present. If `~/.zuliprc` points at
;; a different `site` value, the client refuses rather than hitting the
;; wrong host. If neither path yields a complete pair, the first
;; request raises an exception with instructions for fixing it.
;;
;; You can generate an API key from
;; **Settings → Account & privacy → API key** in any Clojurians Zulip
;; account — admin rights are not required.
;;
;; Credentials are resolved lazily and memoized — the file is read at
;; most once per JVM.

;; ## A smoke-test request
;;
;; `whoami` is the simplest possible round-trip: it hits `/users/me`
;; and returns a small summary of the authenticated identity. It is
;; the right first call after configuring credentials.

(def me (client/whoami))

me

;; The shape:

(keys me)

(kind/test-last
 (= [:email :full-name :user-id :is-bot? :is-admin? :role]))

;; If you need the full `/users/me` response — including fields
;; `whoami` does not surface, like timezone or avatar URL — use
;; `get-me`. We just print the count of fields here:

(-> (client/get-me) keys count)

;; ## Listing channels
;;
;; `get-streams` calls `/streams` and returns the full Zulip API
;; response, including every channel the authenticated user can see.

(def streams-response (client/get-streams))

(-> streams-response :streams count)

;; A single stream entry, with its raw Zulip field names:

(-> streams-response :streams first keys sort)

;; ## Fetching messages
;;
;; `get-messages` is the message-history endpoint. It takes a *narrow*
;; (a vector of operator/operand maps), an *anchor* (an id, or one of
;; the keywords `"newest"`, `"oldest"`, `"first_unread"`), and counts
;; of messages to fetch before and after the anchor.
;;
;; A single message from `kindly-dev`, one of the small channels we
;; reuse as a fixture throughout this book:

(def one-message-response
  (client/get-messages
   {:narrow     [{:operator "channel" :operand "kindly-dev"}]
    :anchor     "newest"
    :num-before 1
    :num-after  0}))

(-> one-message-response :messages count)

(kind/test-last
 (= 1))

;; The fields a message carries (a sample — full set varies):

(-> one-message-response :messages first keys sort)

;; The response also tells you whether the window touches the start or
;; end of the channel's history — this is what `pull/pull-channel!`
;; uses to decide when to stop walking forward:

(select-keys one-message-response
             [:found_anchor :found_oldest :found_newest])

;; ## The general-purpose endpoint
;;
;; `api-get` is the thin function the others are built on. Use it for
;; any endpoint not covered above — pass a path (relative to the API
;; base) and an optional query-params map.
;;
;; The `base-url` def is the prefix that path arguments are appended
;; to. It points at the Clojurians instance:

client/base-url

(kind/test-last
 (= "https://clojurians.zulipchat.com/api/v1"))

;; A direct call. `/server_settings` is unauthenticated metadata — a
;; harmless example.

(-> (client/api-get "/server_settings")
    (select-keys [:realm_name :realm_uri :zulip_version]))

;; ## Reliability: timeouts and retries
;;
;; The client wraps every request in a small retry loop with
;; exponential backoff (up to four retries) for I/O errors and
;; timeouts. A single request times out after ninety seconds. This is
;; opaque from the outside — calls just succeed or, after exhausting
;; retries, throw — but it is worth knowing when interpreting unusual
;; latencies.

;; ## Where to go next
;;
;; - **Pulling and caching channels** — `scicloj.zulipdata.pull`
;;   builds on this client to walk a channel's full history in
;;   resumable, cached windows.
;; - **Tablecloth views** — `scicloj.zulipdata.views` projects raw
;;   messages into purpose-built datasets.
;; - **API Reference** — every public function in one chapter, with
;;   docstrings and a worked example each.
