## zulipdata

A Clojure library for pulling, anonymizing, and analyzing chat
history on the
[Clojurians Zulip](https://clojurians.zulipchat.com).

## General info
|||
|-|-|
|Website | [https://scicloj.github.io/zulipdata/](https://scicloj.github.io/zulipdata/) |
|Source  | [https://github.com/scicloj/zulipdata](https://github.com/scicloj/zulipdata) |
|License | [MIT](LICENSE) |
|Status  | 🛠alpha — actively rewriting🛠 |

## What it does

- **Resumable cached pulls** of channel history.
- **Tablecloth projections** of raw messages into purpose-built datasets.
- **Anonymized views** suitable for sharing.
- **Narrative helpers** for channel lifecycles, cohorts, migrations.
- **Graph operations** for co-membership, co-presence, and community detection.

## Built on

- [tablecloth](https://github.com/scicloj/tablecloth) — dataset manipulation
- [hato](https://github.com/gnarroway/hato) — HTTP client
- [charred](https://github.com/cnuernber/charred) — JSON
- [pocket](https://github.com/scicloj/pocket) — disk cache
- [JGraphT](https://jgrapht.org/) — graph algorithms

## License

MIT. See [LICENSE](LICENSE).
