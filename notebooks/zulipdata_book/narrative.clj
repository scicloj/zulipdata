;; # Narrative helpers
;;
;; `scicloj.zulipdata.narrative` is a small toolkit for the kinds of
;; questions that recur across analyses on this corpus: enriching a
;; timeline with date columns, summarising channel lifecycles,
;; selecting a sub-set of channels by name or by shared user-base,
;; tracing newcomers' prior paths, and counting monthly activity.
;;
;; Everything in this chapter operates on an *anonymized timeline* —
;; a tablecloth dataset with `:channel`, `:user-key`, and `:timestamp`
;; columns produced by `scicloj.zulipdata.anonymize/anonymized-timeline`.
;; The helpers do not care that it is anonymized; they just want those
;; columns. We work on the anonymized form throughout because that is
;; what the next step (graph views) expects, and because there is no
;; reason to handle real names for these aggregates.

(ns zulipdata-book.narrative
  (:require
   [scicloj.zulipdata.pull :as pull]
   [scicloj.zulipdata.anonymize :as anon]
   [scicloj.zulipdata.narrative :as nar]
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]))

;; ## A multi-channel fixture
;;
;; This chapter needs more than one channel — `channels-by-shared-users`
;; and `prior-channels-of-newcomers` are about cross-channel structure.
;; We use four small-to-medium scicloj channels with overlapping
;; contributors. Subsequent runs are cache-served.

(def fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"])

(def messages
  (->> (pull/pull-channels! fixture-channels)
       (filter (fn [[k _]] (string? k)))
       (mapcat (fn [[_ r]] (pull/all-messages r)))))

(count messages)

(def base-timeline
  (anon/anonymized-timeline messages))

(tc/row-count base-timeline)

(kind/test-last
 (= (count messages)))

;; ## Adding date columns
;;
;; Most analyses bucket activity by month or year. `with-time-columns`
;; adds three derived columns from `:timestamp` (epoch seconds, UTC):
;; `:month-date` (a `LocalDate` set to the first of the month),
;; `:year-month` (a `"YYYY-MM"` string), and `:year` (an integer).
;;
;; The three are different shapes for different uses: `LocalDate`
;; values plot on a real calendar axis, strings sort lexicographically
;; for grouping, integers behave well as numeric facets.

(def timeline (nar/with-time-columns base-timeline))

(tc/column-names timeline)

;; The three new columns:

(-> timeline
    (tc/select-columns [:timestamp :month-date :year-month :year])
    (tc/head 3))

;; The same three derivations are also exposed as scalar helpers
;; (`ts->month-date`, `ts->year-month`, `ts->year`), in case you need
;; them for one-off arithmetic without a dataset:

(let [ts (-> timeline :timestamp first)]
  {:ts         ts
   :month-date (nar/ts->month-date ts)
   :year-month (nar/ts->year-month ts)
   :year       (nar/ts->year ts)})

;; ## Channel lifecycles
;;
;; `channel-lifecycle` is the one-row-per-channel summary that drives
;; most "where are we?" reports. It collapses every message in the
;; timeline into five columns per channel: first month, last month,
;; total messages, distinct active months, and distinct (anonymized)
;; users. Sorted ascending by first-date.

(def lifecycles (nar/channel-lifecycle timeline))

lifecycles

;; The number of channels matches the number of distinct channels in
;; the timeline:

(tc/row-count lifecycles)

(kind/test-last
 (= (-> timeline :channel distinct count)))

;; ## Selecting channels by name pattern
;;
;; `channels-by-name-pattern` is a thin convenience around
;; `re-find` against the distinct `:channel` values. Cheap and useful
;; for grabbing a name-defined cluster — but fragile because it
;; depends on naming conventions.

(nar/channels-by-name-pattern timeline #"clay|tableplot")

;; ## Selecting channels by shared user-base
;;
;; `channels-by-shared-users` is the user-overlap counterpart to the
;; name pattern. Pick a `seed-channel`, take its top-N posters, and
;; return every channel where those users account for at least
;; `share` of activity. Use this to grow a cluster around a seed
;; channel by *who-posts-there*, rather than by name.
;;
;; In a real corpus pull this finds many more channels than a naive
;; name pattern; in our four-channel fixture it picks up only the
;; ones that share a substantial slice of clay-dev's voices.

(nar/channels-by-shared-users timeline "clay-dev"
                              :share 0.4 :min-msgs 30 :top-n 30)

;; ## First posters of a channel
;;
;; `first-posters-of-channel` returns the first `n` distinct
;; `:user-key`s to ever post in a channel, with the date of their
;; first post. Useful for telling a "who started this" story.

(nar/first-posters-of-channel timeline "kindly-dev" 5)

;; ## Tracing newcomers' prior channels
;;
;; `prior-channels-of-newcomers` answers: for everyone whose first
;; post in `channel` falls in `year-month`, where else had they been
;; posting in the timeline before that first post? Returns one row
;; per prior channel with the count of newcomers who passed through
;; it.
;;
;; **A caveat on scope.** "Prior channels" is restricted to whatever
;; you pulled. In our four-channel fixture, anyone whose only prior
;; activity was in `data-science` or `slack-archive` will not show
;; up. Run the same call on a corpus-wide timeline and the answer
;; covers the whole community.

(nar/prior-channels-of-newcomers timeline "kindly-dev" "2024-09")

;; ## Monthly activity per channel
;;
;; `channel-monthly-activity` is the long-form basis for any
;; activity-over-time chart: one row per (channel, month-date) with
;; a `:msgs` count. Pass an optional set of channel names to restrict
;; the output.

(def kindly-monthly
  (nar/channel-monthly-activity timeline #{"kindly-dev"}))

(tc/head kindly-monthly 3)

;; The total over the channel matches the lifecycle row:

(reduce + (:msgs kindly-monthly))

(kind/test-last
 (= (-> lifecycles
        (tc/select-rows #(= "kindly-dev" (:channel %)))
        :total
        first)))

;; ## Where to go next
;;
;; - **Graph views** — `scicloj.zulipdata.graph` builds
;;   co-membership and co-presence graphs from the same anonymized
;;   timeline, plus utilities for community detection and rendering.
;; - **API Reference** — every public function in one chapter, with
;;   docstrings and a worked example each.
