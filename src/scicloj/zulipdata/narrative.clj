(ns scicloj.zulipdata.narrative
  "Helpers for narrative-style analyses across the corpus: enriching
   timelines with date columns, summarising channel lifecycles,
   selecting channels by name or by shared user-base, and tracing
   newcomers' prior channels.

   Operates on the anonymized timeline returned by
   `scicloj.zulipdata.anonymize/anonymized-timeline`."
  (:require [tablecloth.api :as tc])
  (:import [java.time Instant LocalDate ZoneOffset YearMonth]))

(defn ^LocalDate ts->month-date
  "Epoch-second -> first-of-month LocalDate (UTC)."
  [^long ts]
  (let [d (LocalDate/ofInstant (Instant/ofEpochSecond ts) ZoneOffset/UTC)]
    (LocalDate/of (.getYear d) (.getMonthValue d) 1)))

(defn ts->year-month
  "Epoch-second -> \"YYYY-MM\" string (UTC)."
  [^long ts]
  (let [d (LocalDate/ofInstant (Instant/ofEpochSecond ts) ZoneOffset/UTC)]
    (str (YearMonth/of (.getYear d) (.getMonthValue d)))))

(defn ts->year
  "Epoch-second -> integer year (UTC)."
  [^long ts]
  (.getYear (LocalDate/ofInstant (Instant/ofEpochSecond ts) ZoneOffset/UTC)))

(defn with-time-columns
  "Add `:month-date`, `:year-month`, and `:year` columns to a timeline
   that has a `:timestamp` column (epoch seconds)."
  [timeline]
  (-> timeline
      (tc/add-column :month-date (fn [ds] (mapv ts->month-date  (:timestamp ds))))
      (tc/add-column :year-month (fn [ds] (mapv ts->year-month (:timestamp ds))))
      (tc/add-column :year       (fn [ds] (mapv ts->year       (:timestamp ds))))))

(defn channel-lifecycle
  "One row per channel: first-date, last-date, total messages, active
   months, distinct users. Sorted ascending by first-date by default."
  [timeline]
  (-> timeline
      (tc/group-by [:channel])
      (tc/aggregate
       {:first-date     (fn [ds] (ts->month-date (apply min (:timestamp ds))))
        :last-date      (fn [ds] (ts->month-date (apply max (:timestamp ds))))
        :total          tc/row-count
        :active-months  (fn [ds] (count (distinct (mapv ts->year-month (:timestamp ds)))))
        :distinct-users (fn [ds] (count (distinct (:user-key ds))))})
      (tc/order-by [:first-date])))

(defn channels-by-name-pattern
  "Channels whose name matches `regex`."
  [timeline regex]
  (->> timeline :channel distinct (filter #(re-find regex %)) sort vec))

(defn channels-by-shared-users
  "Channels where the top-N posters of `seed-channel` account for at
   least `share` of messages, restricted to channels with at least
   `min-msgs` total. Returns a sorted vector of channel names.

   Use to build a curated cluster around a seed channel by user-overlap
   rather than name patterns."
  [timeline seed-channel
   & {:keys [share min-msgs top-n] :or {share 0.4 min-msgs 30 top-n 30}}]
  (let [seed-rows  (-> timeline (tc/select-rows #(= seed-channel (:channel %))))
        top-users  (->> (-> seed-rows
                            (tc/group-by [:user-key])
                            (tc/aggregate {:n tc/row-count})
                            (tc/order-by [:n] [:desc])
                            (tc/head top-n))
                        :user-key set)]
    (->> (tc/rows timeline :as-maps)
         (group-by :channel)
         (keep (fn [[c msgs]]
                 (let [n (count msgs)
                       k (count (filter #(top-users (:user-key %)) msgs))]
                   (when (and (>= n min-msgs) (>= (/ k n) share)) c))))
         sort vec)))

(defn first-posters-of-channel
  "First `n` distinct user-keys to post in `channel`, with their
   first-post date. Useful for identifying a channel's earliest
   contributors."
  [timeline channel n]
  (->> (tc/rows timeline :as-maps)
       (filter #(= channel (:channel %)))
       (group-by :user-key)
       (map (fn [[u msgs]]
              (let [first-msg (apply min-key :timestamp msgs)]
                {:user-key u
                 :first-post-date (ts->month-date (:timestamp first-msg))})))
       (sort-by :first-post-date)
       (take n)
       tc/dataset))

(defn prior-channels-of-newcomers
  "For users whose first-ever post in `channel` falls in the given
   `year-month` (\"YYYY-MM\"), tally the channels they had posted in
   before that first post. Returns one row per (prior-channel) with
   counts of how many newcomers passed through it."
  [timeline channel year-month]
  (let [rows         (tc/rows timeline :as-maps)
        first-in-ch  (->> rows
                          (filter #(= channel (:channel %)))
                          (group-by :user-key)
                          (into {} (map (fn [[u ms]]
                                          [u (apply min-key :timestamp ms)]))))
        newcomers    (->> first-in-ch
                          (filter (fn [[_ m]]
                                    (= year-month (ts->year-month (:timestamp m)))))
                          (into {}))
        prior-counts (->> rows
                          (filter (fn [r]
                                    (when-let [first-m (newcomers (:user-key r))]
                                      (and (not= channel (:channel r))
                                           (< (:timestamp r) (:timestamp first-m))))))
                          (group-by :channel)
                          (into {} (map (fn [[c rs]]
                                          [c (count (distinct (map :user-key rs)))]))))]
    (-> (mapv (fn [[c n]] {:prior-channel c :newcomers-touched n}) prior-counts)
        tc/dataset
        (tc/order-by [:newcomers-touched] [:desc]))))

(defn channel-monthly-activity
  "Long-form: one row per (channel, month-date) with `:msgs` count.
   Restricted to `channels` if supplied, else all channels."
  ([timeline] (channel-monthly-activity timeline nil))
  ([timeline channels]
   (-> (cond-> timeline
         channels (tc/select-rows #(channels (:channel %))))
       (tc/group-by [:channel :month-date])
       (tc/aggregate {:msgs tc/row-count})
       (tc/order-by [:channel :month-date]))))
