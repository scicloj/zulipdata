;; # The REST client
;;
;; `scicloj.zulipdata.client` is the library's lowest level: a thin
;; wrapper over [Zulip's HTTP REST API](https://zulip.com/api/) for
;; the [Clojurians](https://clojurians.zulipchat.com) instance. Most
;; analyses can use the higher-level `pull` namespace. The client is
;; useful to understand, and is what you call directly for any
;; endpoint the rest of the library does not cover.
;;
;; This chapter walks through:
;;
;; - How credentials are resolved.
;; - The four endpoints the library exposes (`whoami`, `get-me`,
;;   `get-streams`, `get-messages`).
;; - The general-purpose `api-get` for any other endpoint.

(ns zulipdata-book.client
  (:require
   ;; Zulipdata client -- Zulip REST API wrapper
   [scicloj.zulipdata.client :as client]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]))

;; ## Credentials
;;
;; The client looks for credentials in two places, in order:
;;
;; 1. The environment variables `ZULIP_EMAIL` and `ZULIP_API_KEY`.
;; 2. The `[api]` section of `~/.zuliprc` (the standard Zulip CLI
;;    config file), with `email`, `key`, and `site = https://clojurians.zulipchat.com`.
;;
;; If both are present, the environment variables take precedence.
;; If `~/.zuliprc` points at a different `site` value, the client
;; raises an error rather than calling the wrong host. If neither path yields a complete pair, the first
;; request raises an exception with instructions for fixing it.
;;
;; You can generate an API key from
;; **Settings → Account & privacy → API key** in any Clojurians Zulip
;; account — admin rights are not required.
;;
;; Credentials are resolved lazily and memoized — the file is read at
;; most once per JVM.

;; ## A first request
;;
;; `whoami` is the simplest possible request: it calls
;; [`/users/me`](https://zulip.com/api/get-own-user) and returns a
;; small summary of the authenticated identity. It is the right first
;; call after configuring credentials.

(def me (client/whoami))

me

;; The shape:

(keys me)

(kind/test-last
 (= [:email :full-name :user-id :is-bot :is-admin :role]))

;; If you need the full `/users/me` response — including fields
;; `whoami` does not include, like timezone or avatar URL — use
;; `get-me`. We print the count of fields here:

(-> (client/get-me) keys count)

;; ## Listing channels
;;
;; `get-streams` calls [`/streams`](https://zulip.com/api/get-streams)
;; and returns the full Zulip API response, including every channel
;; the authenticated user can see.

(def streams-response (client/get-streams))

(-> streams-response :streams count)

;; A single stream entry, with its raw Zulip field names:

(-> streams-response :streams first keys sort)

;; ### Web-public channels
;;
;; Among those streams, some are marked
;; [**web-public**](https://zulip.com/help/public-access-option): their
;; messages are readable without a Zulip account. The flag lives on
;; the stream entry as `:is_web_public`.

(def web-public-channels
  (->> streams-response
       :streams
       (filter :is_web_public)
       (mapv :name)
       sort))

(count web-public-channels)

;; The full list, alphabetised:

web-public-channels

;; The distinction matters when sharing data — content
;; from a non-web-public channel should not appear in artifacts that
;; leave your machine, while content from web-public channels is
;; suitable for sharing. The book's tutorial chapters
;; ([**Tablecloth views**](./zulipdata_book.views.html),
;; [**Narrative**](./zulipdata_book.narrative.html),
;; [**Graph views**](./zulipdata_book.graph.html))
;; deliberately use a small **web-public** sample so the
;; rendered output can show real names, topic strings, and message
;; content without leaking anything login-gated. For analyses on
;; non-web-public channels, see
;; [**Anonymized views**](./zulipdata_book.anonymize.html).

;; ## Fetching messages
;;
;; [`get-messages`](https://zulip.com/api/get-messages) is the
;; message-history endpoint. It takes a
;; [*narrow*](https://zulip.com/api/construct-narrow) (a vector of
;; operator/operand maps), an *anchor* (an id, or one of the keywords
;; `"newest"`, `"oldest"`, `"first_unread"`), and counts of messages
;; to fetch before and after the anchor.
;;
;; A single message from `clojurecivitas`, one of the web-public
;; channels we reuse as a sample throughout this book:

(def one-message-response
  (client/get-messages
   {:narrow     [{:operator "channel" :operand "clojurecivitas"}]
    :anchor     "newest"
    :num-before 1
    :num-after  0}))

(-> one-message-response :messages count)

(kind/test-last
 (= 1))

;; The whole message map — sender, topic, content, timestamps,
;; reactions, and edit history. The exact set of fields varies
;; slightly across messages; the rest of the library normalises
;; this shape into the views described in
;; [**Tablecloth views**](./zulipdata_book.views.html).

(-> one-message-response :messages first)

;; The response also indicates whether the window touches the start
;; or end of the channel's history — this is what `pull/pull-channel!`
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

;; A direct call.
;; [`/server_settings`](https://zulip.com/api/get-server-settings) is
;; unauthenticated metadata — a harmless example.

(-> (client/api-get "/server_settings")
    (select-keys [:realm_name :realm_uri :zulip_version]))

;; ## Reliability: timeouts and retries
;;
;; The client wraps every request in a small retry loop with longer
;; waits between retries (up to four retries) for network errors and
;; timeouts. A single request times out after ninety seconds. This is
;; invisible from the outside — calls succeed or, after exhausting
;; retries, throw — but it is worth knowing when explaining occasional
;; slow responses.

;; ## Where to go next
;;
;; - [**Pulling and caching channels**](./zulipdata_book.pull.html) —
;;   `scicloj.zulipdata.pull` builds on this client to walk a
;;   channel's full history in resumable, cached windows.
;; - [**Tablecloth views**](./zulipdata_book.views.html) —
;;   `scicloj.zulipdata.views` projects raw messages into
;;   purpose-built datasets.
;; - [**API Reference**](./zulipdata_book.api_reference.html) —
;;   every public function in one chapter, with docstrings and a
;;   worked example each.
