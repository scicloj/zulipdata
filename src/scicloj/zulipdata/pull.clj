(ns scicloj.zulipdata.pull
  "Paginated, resumable pulls of Zulip channel history. Each forward
   window is keyed on `(stream-name, anchor-id, batch-size)` and cached
   on disk via pocket — so repeated runs and crash recovery are idempotent.

   Cache directory: `ZULIP_CACHE_DIR` env var, or
   `~/.cache/zulipdata-clojurians/` by default."
  (:require [scicloj.zulipdata.client :as client]
            [scicloj.pocket :as pocket]
            [clojure.java.io :as io]))

(defn- resolve-cache-dir []
  (let [dir (or (System/getenv "ZULIP_CACHE_DIR")
                (str (System/getProperty "user.home")
                     "/.cache/zulipdata-clojurians/"))
        f   (io/file dir)]
    (when-not (.exists f) (.mkdirs f))
    dir))

(pocket/set-base-cache-dir! (resolve-cache-dir))

(def default-batch-size
  "Messages requested per window when `pull-channel!` is called without
   an explicit `:batch-size`. 5000 is also Zulip's per-request cap."
  5000)

(defn- fetch-window-impl
  "Fetch a forward window of messages in the given channel, starting at
   (and including) `anchor-id`. `batch-size` is the number of messages
   after the anchor to request (Zulip caps this around 5000)."
  [stream-name anchor-id batch-size]
  (client/get-messages
   {:narrow         [{:operator "channel" :operand stream-name}]
    :anchor         anchor-id
    :num-before     0
    :num-after      batch-size
    :apply-markdown false}))

(def ^:private fetch-window-cached
  (pocket/caching-fn #'fetch-window-impl))

(defn fetch-window
  "Cached forward window. Returns the deref'd page map."
  [stream-name anchor-id batch-size]
  (deref (fetch-window-cached stream-name anchor-id batch-size)))

(defn- invalidate-window! [stream-name anchor-id batch-size]
  (pocket/invalidate! #'fetch-window-impl stream-name anchor-id batch-size))

(defn pull-channel!
  "Walk forward through `stream-name` in cached windows, starting at
   `start-anchor-id`. Returns `{:pages [...], :message-count n}`.

   Options:
     :batch-size   — messages per window (default 5000)
     :refresh-tip? — when true, any cached page with `found_newest: true`
                     is invalidated and re-fetched once, then the walk
                     continues if new full windows appeared. Use to catch
                     up after messages were posted since the last pull.

   With `:refresh-tip? false` (default), repeated calls are served
   entirely from cache."
  [stream-name start-anchor-id
   & {:keys [batch-size refresh-tip?]
      :or   {batch-size default-batch-size}}]
  (loop [anchor          start-anchor-id
         pages           []
         total           0
         refreshed-here? false]
    (let [page (fetch-window stream-name anchor batch-size)
          msgs (:messages page)
          n    (count msgs)]
      (cond
        ;; stale tip — invalidate once, loop at the same anchor
        (and refresh-tip? (:found_newest page) (not refreshed-here?))
        (do (invalidate-window! stream-name anchor batch-size)
            (recur anchor pages total true))

        (or (:found_newest page) (zero? n))
        {:pages         (conj pages page)
         :message-count (+ total n)}

        :else
        (recur (inc (:id (last msgs)))
               (conj pages page)
               (+ total n)
               false)))))

(defn all-messages
  "Flatten the :pages result of pull-channel! into a single seq of messages,
   de-duplicating by :id (windows are non-overlapping by construction, but
   this is a cheap belt-and-braces safeguard)."
  [pull-result]
  (->> (:pages pull-result)
       (mapcat :messages)
       (into [] (distinct))))

(defn- streams-by-name
  "Map of stream-name → stream entry, via a fresh /streams call."
  []
  (->> (client/get-streams) :streams
       (map (juxt :name identity))
       (into {})))

(defn pull-channels!
  "Pull a collection of channels by name. Returns a map
   `{channel-name {:pages ... :message-count ... :stream-id ... :first-message-id ...}}`.

   First-message ids are resolved from `/streams`. Any unknown channel
   names are returned under key `:not-found` as a vector."
  [channel-names & {:keys [batch-size refresh-tip?] :as opts}]
  (let [by-name   (streams-by-name)
        {known true unknown false} (group-by #(contains? by-name %) channel-names)]
    (into {:not-found (vec unknown)}
          (for [name known
                :let [{:keys [stream_id first_message_id]} (by-name name)
                      pulled (pull-channel! name first_message_id opts)]]
            [name (assoc pulled
                         :stream-id stream_id
                         :first-message-id first_message_id)]))))

(defn public-channel-names
  "Names of all channels visible to the bot that are either public or web-public."
  []
  (->> (client/get-streams) :streams
       (filter #(or (not (:invite_only %)) (:is_web_public %)))
       (mapv :name)))

(defn pull-public-channels!
  "Convenience: pull every public + web-public channel visible to the bot.
   Same options as `pull-channels!`."
  [& opts]
  (apply pull-channels! (public-channel-names) opts))
