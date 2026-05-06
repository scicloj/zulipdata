# Changelog

All notable changes to zulipdata will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-05-06

Added

- `scicloj.zulipdata.pull/all-channel-messages` — flatten the result
  of `pull-channels!` into a single distinct sequence of raw messages,
  replacing the explicit `(filter ... (mapcat ...))` pattern.
- `scicloj.zulipdata.emoji` — display helpers for reactions:
  `decode-unicode` (codepoint sequence → glyph), `realm-emoji-map`
  (workspace custom-emoji map from `/realm/emoji`), and `display`
  (returns a unicode glyph or a `kind/hiccup` `<img>` element suitable
  for a `kind/table` cell).

Dependencies

- `org.scicloj/kindly` is now a direct dependency (was already
  pulled in transitively via tablecloth).

## [0.1.0] - 2026-05-05

Initial release.

- `scicloj.zulipdata.client` — REST client for the Clojurians Zulip
  instance.
- `scicloj.zulipdata.pull` — paginated, cached, resumable pulls of
  channel history (disk cache via pocket / nippy).
- `scicloj.zulipdata.views` — tablecloth projections of raw messages
  (timeline, reactions, edits, topic links).
- `scicloj.zulipdata.anonymize` — HMAC-SHA256-keyed anonymized
  projections; sender ids and topic strings replaced by stable
  hex keys, message content dropped.
- `scicloj.zulipdata.narrative` — date columns, channel lifecycles,
  channel selection by name or by shared user-base, newcomer-tracking
  helpers.
- `scicloj.zulipdata.graph` — channel co-membership, user
  co-presence, and migration graphs (JGraphT); betweenness
  centrality, Girvan-Newman, label-propagation; conversion to
  `kind/cytoscape` and `kind/graphviz` shapes.
