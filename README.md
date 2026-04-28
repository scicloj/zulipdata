## zulipdata

A Clojure library for pulling, anonymizing, and analyzing chat
history on the
[Clojurians Zulip](https://clojurians.zulipchat.com). Resumable
cached pulls, tablecloth projections of the raw messages, anonymized
views suitable for sharing, and helpers for channel lifecycles,
co-membership graphs, and community detection.

## General info
|||
|-|-|
|Website | [https://scicloj.github.io/zulipdata/](https://scicloj.github.io/zulipdata/) |
|Source  | [https://github.com/scicloj/zulipdata](https://github.com/scicloj/zulipdata) |
|License | [MIT](LICENSE) |
|Status  | 🛠alpha — actively rewriting🛠 |

## What it does

- **Resumable cached pulls** — `pull/pull-channels!` walks a
  channel's full history in 5000-message windows, caching each
  window to disk. Crashed pulls and re-runs are free.
- **Tablecloth projections** — `views/messages-timeline`,
  `views/reactions-long`, `views/edits-long`,
  `views/topic-links-long` flatten raw messages into purpose-built
  datasets.
- **Anonymized views** — `anonymize/anonymized-timeline` mirrors
  the plain views with HMAC-hashed user keys and dropped content,
  suitable for sharing.
- **Narrative helpers** — channel lifecycles, cross-channel
  migration tracing, newcomer cohorts, monthly activity tables.
- **Graph operations** — co-membership and co-presence graphs,
  community detection (Girvan-Newman, label propagation),
  betweenness centrality, and rendering to `kind/cytoscape` and
  `kind/graphviz`.

## Documentation

The book at
[scicloj.github.io/zulipdata](https://scicloj.github.io/zulipdata/)
is the source of truth. It includes:

- **Getting Started** — `quickstart` walks an end-to-end run,
  including credential setup against a Clojurians member account.
- **Tutorial** — one chapter per public namespace
  (`client`, `pull`, `views`, `anonymize`, `narrative`, `graph`).
- **Reference** — `api_reference` lists every public function with
  its docstring and a worked example.
- **Repository** — for contributors: rendering the book, GFM as a
  test-generation artifact, running tests, releases.

## API

```clojure
(require '[scicloj.zulipdata.client    :as client])  ; REST + auth
(require '[scicloj.zulipdata.pull      :as pull])    ; resumable cached pulls
(require '[scicloj.zulipdata.views     :as views])   ; tablecloth projections
(require '[scicloj.zulipdata.anonymize :as anon])    ; anonymized projections
(require '[scicloj.zulipdata.narrative :as nar])     ; lifecycles, newcomers
(require '[scicloj.zulipdata.graph     :as graph])   ; graphs, communities
```

## Built on

- [tablecloth](https://github.com/scicloj/tablecloth) — dataset manipulation
- [hato](https://github.com/gnarroway/hato) — HTTP client
- [charred](https://github.com/cnuernber/charred) — JSON
- [pocket](https://github.com/scicloj/pocket) — disk cache
- [JGraphT](https://jgrapht.org/) — graph algorithms

## License

MIT. See [LICENSE](LICENSE).
