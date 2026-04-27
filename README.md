## zulipdata

A Clojure analysis of the **Clojurians Zulip** chat history, with a
particular focus on the data-science / scicloj sub-community. The
notebooks pull every public + web-public channel, anonymize the
identities and content, and tell stories about how the community has
evolved over its first ~7.5 years.

### General info
|||
|-|-|
|Status |🛠alpha — actively rewriting🛠|
|License |[MIT](LICENSE)|

## Status

Pre-release. The library is usable end-to-end but the chapters and
helpers are still moving. The rendered chapters live under [`gfm/`](gfm)
and are regenerated from the notebooks under
[`notebooks/zulipdata_book/`](notebooks/zulipdata_book/).

## Quick start

### 1. Get a Clojurians member account

Sign up at [clojurians.zulipchat.com](https://clojurians.zulipchat.com)
if you don't already have one. A regular member account is enough — no
admin or bot privileges are required.

### 2. Get your API key

In the Clojurians web UI:
**Settings → Account & privacy → API key → Show / Generate**.

### 3. Configure credentials

**Either** export environment variables:

```bash
export ZULIP_EMAIL="you@example.com"
export ZULIP_API_KEY="..."
```

**Or** drop your existing `~/.zuliprc` (the standard Zulip CLI config)
in place. The project will read the `[api]` section automatically as
long as `site = https://clojurians.zulipchat.com`:

```ini
[api]
email = you@example.com
key = ...
site = https://clojurians.zulipchat.com
```

Env vars take priority if both are set.

### 4. Verify

```bash
clojure -M:dev -m nrepl.cmdline   # start REPL
```

```clojure
(require '[scicloj.zulipdata.client :as client])
(client/whoami)
;; => {:email "you@example.com" :full-name "Your Name"
;;     :user-id 12345 :is-bot? false :is-admin? false :role 400}
```

If you get credentials back, you're ready.

### 5. Pull the corpus

```clojure
(require '[scicloj.zulipdata.pull :as pull])
(pull/pull-public-channels!)   ;; ~14 minutes the first time
```

The first pull walks every public + web-public channel and caches each
5000-message window to disk under `~/.cache/zulipdata-clojurians/`
(override with `ZULIP_CACHE_DIR`). Subsequent runs are served entirely
from cache.

### 6. Read or render the chapters

The rendered chapters are in [`gfm/`](gfm). To regenerate them:

```clojure
(require '[dev :as dev])
(dev/make-gfm! "zulipdata_book/scicloj_story.clj")
;; or all chapters:
(dev/make-gfm!)
```

## What's in the box

- **`src/scicloj/zulipdata/`** — the library
  - `client` — Zulip REST + auth
  - `pull` — paginated, resumable, cached channel pulls
  - `views` — projections of raw messages into purpose-built tablecloth datasets
  - `anonymize` — anonymized projections (HMAC-hashed user keys, dropped content)
  - `narrative` — analysis helpers (channel lifecycles, cluster selection, migrations)
  - `graph` — JGraphT-backed graph operations (community detection, centrality)
- **`notebooks/zulipdata_book/`** — the analysis chapters
  - `channels_overview` — corpus-wide overview of all 178 channels
  - `channels_activity` — five-channel deep dive
  - `community_shape` — tenure mix, engagement, the contributor ring
  - `scicloj_story` — the five-era narrative of data-science in Clojure on this Zulip
  - `patient_community` — interpretive: solidarity, joy, resilience
  - `network_shape` — community detection + bridges + migration flow
- **`dev-notes/`** — long-form references (history, channel catalog, methodology)

## Anonymization

The published chapters never display real names, message bodies, or
topic strings. We hash sender ids and topic strings with HMAC-SHA256
using a salt committed in the source. This is *pseudonymous in
published artifacts* — anyone with access to the original Zulip data
and the salt can re-identify, but the rendered markdown / charts /
hashes alone do not leak names. See
[`dev-notes/research-methods.md`](dev-notes/research-methods.md) for
the full rationale.

## What you'll see

A single `(client/whoami)` against your member account is enough to
confirm everything works. From there, every notebook is reproducible
end-to-end on any Clojurians member account — your local pull will
yield the same channel data the project was built from.

## Development

```bash
clojure -M:dev -m nrepl.cmdline   # REPL
./run_tests.sh                     # tests (notebook-generated + hand-written)
```

## License

MIT. See [LICENSE](LICENSE).
