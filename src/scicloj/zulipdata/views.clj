(ns scicloj.zulipdata.views
  "Projections of raw Zulip messages into purpose-built tablecloth datasets.
   Raw messages are the source of truth; these views are built on demand."
  (:require [tablecloth.api :as tc]))

(defn- timestamp->instant [ts]
  (when ts (java.time.Instant/ofEpochSecond ts)))

(defn messages-timeline
  "One row per message — simple-valued fields only. Good for
   activity-over-time and sender/topic analyses."
  [messages]
  (tc/dataset
   (map (fn [m]
          {:id             (:id m)
           :timestamp      (:timestamp m)
           :instant        (timestamp->instant (:timestamp m))
           :stream-id      (:stream_id m)
           :channel        (:display_recipient m)
           :subject        (:subject m)
           :sender-id      (:sender_id m)
           :sender         (:sender_full_name m)
           :client         (:client m)
           :edited         (contains? m :last_edit_timestamp)
           :last-edit-ts   (:last_edit_timestamp m)
           :content        (:content m)
           :content-length (count (:content m))})
        messages)))

(defn reactions-long
  "One row per (message, reaction). Fields: message-id, stream-id, channel,
   subject, emoji-name, emoji-code, reaction-type, user-id, message-ts."
  [messages]
  (tc/dataset
   (for [m messages
         r (:reactions m)]
     {:message-id    (:id m)
      :message-ts    (:timestamp m)
      :stream-id     (:stream_id m)
      :channel       (:display_recipient m)
      :subject       (:subject m)
      :emoji-name    (:emoji_name r)
      :emoji-code    (:emoji_code r)
      :reaction-type (:reaction_type r)
      :user-id       (:user_id r)})))

(defn edits-long
  "One row per edit event in :edit_history. Note: some edits record only
   topic/stream moves (no :prev_content); we include prev-content as-is."
  [messages]
  (tc/dataset
   (for [m messages
         e (:edit_history m)]
     {:message-id    (:id m)
      :stream-id     (:stream_id m)
      :channel       (:display_recipient m)
      :edit-ts       (:timestamp e)
      :edit-user-id  (:user_id e)
      :prev-content  (:prev_content e)
      :prev-subject  (:prev_topic e)
      :prev-stream   (:prev_stream e)})))

(defn topic-links-long
  "One row per auto-linked URL inside a message."
  [messages]
  (tc/dataset
   (for [m messages
         l (:topic_links m)]
     {:message-id (:id m)
      :stream-id  (:stream_id m)
      :channel    (:display_recipient m)
      :link-text  (:text l)
      :link-url   (:url l)})))
