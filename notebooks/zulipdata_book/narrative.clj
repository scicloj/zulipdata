;; # Narrative
;;
;; `scicloj.zulipdata.narrative` is a small toolkit for the kinds of
;; questions that recur across analyses on this corpus: enriching a
;; timeline with date columns, summarising channel lifecycles,
;; selecting a sub-set of channels by name or by shared user-base,
;; tracing newcomers' prior paths, and counting monthly activity.
;;
;; Everything in this chapter operates on an *anonymized timeline* —
;; a tablecloth dataset with `:channel`, `:user-key`, and `:timestamp`
;; columns produced by `anonymized-timeline` (see
;; [**Anonymized views**](./zulipdata_book.anonymize.html)).
;; The helpers do not depend on anonymization; they only require
;; those columns. We work on the anonymized form throughout because
;; that is what the next step
;; ([**Graph views**](./zulipdata_book.graph.html))
;; expects, and because there is no reason to handle real names for
;; these aggregates.

(ns zulipdata-book.narrative
  (:require
   [scicloj.zulipdata.pull :as pull]
   [scicloj.zulipdata.anonymize :as anon]
   [scicloj.zulipdata.narrative :as nar]
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]))

;; ## A multi-channel sample
;;
;; This chapter needs more than one channel — `channels-by-shared-users`
;; and `prior-channels-of-newcomers` are about cross-channel structure.
;; We pull every web-public channel of the Clojurians Zulip; the cache
;; serves repeated runs.

(def sample-channels
  (pull/web-public-channel-names))

(def messages
  (->> (pull/pull-channels! sample-channels)
       (filter (fn [[k _]] (string? k)))
       (mapcat (fn [[_ r]] (pull/all-messages r)))))

(count messages)

(def base-timeline
  (anon/anonymized-timeline messages))

base-timeline

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

(-> timeline tc/column-names sort)

(every? (set (tc/column-names timeline))
        [:month-date :year-month :year])

(kind/test-last
 (= true))

;; The three new columns, freshest first:

(-> timeline
    (tc/select-columns [:timestamp :month-date :year-month :year])
    (tc/order-by :timestamp :desc)
    (tc/head 5))

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
;; `channel-lifecycle` is the one-row-per-channel summary used in
;; activity reports. It summarises every message in the
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
;; `re-find` against the distinct `:channel` values. Quick and useful
;; for picking out a name-defined cluster — but fragile because it
;; depends on naming conventions.

(nar/channels-by-name-pattern timeline #"civitas|gratitude")

(kind/test-last
 (= ["clojurecivitas" "gratitude"]))

;; ## Selecting channels by shared user-base
;;
;; `channels-by-shared-users` is the user-overlap counterpart to the
;; name pattern. Pick a `seed-channel`, take its top-N posters, and
;; return every channel where those users account for at least
;; `share` of activity. Use this to build a cluster around a seed
;; channel by who posts there, rather than by name.
;;
;; Tightening `:share` shrinks the result: at `0.5` the seed's top
;; posters account for at least half the activity in only three
;; channels (clojurecivitas itself, events, scicloj-webpublic).

(nar/channels-by-shared-users timeline "clojurecivitas"
                              :share 0.5 :min-msgs 5 :top-n 5)

(kind/test-last
 (= ["clojurecivitas" "events" "scicloj-webpublic"]))

;; ## First posters of a channel
;;
;; `first-posters-of-channel` returns the first `n` distinct
;; `:user-key`s to ever post in a channel, with the date of their
;; first post. Useful for identifying a channel's earliest contributors.

(def civitas-first-posters
  (nar/first-posters-of-channel timeline "clojurecivitas" 5))

civitas-first-posters

(tc/row-count civitas-first-posters)

(kind/test-last
 (= 5))

;; ## Tracing newcomers' prior channels
;;
;; `prior-channels-of-newcomers` answers: for everyone whose first
;; post in `channel` falls in `year-month`, where else had they been
;; posting in the timeline before that first post? Returns one row
;; per prior channel with the count of newcomers who passed through
;; it.
;;
;; **A note on scope.** "Prior channels" is restricted to whatever
;; you pulled. We pulled every web-public channel, so the answer
;; covers the whole web-public community; non-web-public prior
;; activity is invisible.

(nar/prior-channels-of-newcomers timeline "clojurecivitas" "2025-10")

;; ## Monthly activity per channel
;;
;; `channel-monthly-activity` is the long-form basis for any
;; activity-over-time chart: one row per (channel, month-date) with
;; a `:msgs` count. Pass an optional set of channel names to restrict
;; the output.

(def civitas-monthly
  (nar/channel-monthly-activity timeline #{"clojurecivitas"}))

civitas-monthly

;; The total over the channel matches the lifecycle row:

(reduce + (:msgs civitas-monthly))

(kind/test-last
 (= (-> lifecycles
        (tc/select-rows #(= "clojurecivitas" (:channel %)))
        :total
        first)))

;; ## Where to go next
;;
;; - [**Graph views**](./zulipdata_book.graph.html) —
;;   `scicloj.zulipdata.graph` builds co-membership and
;;   co-presence graphs from the same anonymized timeline, plus
;;   utilities for community detection and rendering.
;; - [**API Reference**](./zulipdata_book.api_reference.html) —
;;   every public function in one chapter, with docstrings and a
;;   worked example each.
