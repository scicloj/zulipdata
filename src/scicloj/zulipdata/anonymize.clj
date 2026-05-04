(ns scicloj.zulipdata.anonymize
  "Anonymized projections of the corpus. Sender identities and
   subject lines are replaced by stable hash keys; message content
   is dropped. Suitable for sharing dataset views without exposing
   real names or message text.

   The salt is committed in source: anonymization here is about not
   displaying real identities in published files, not about
   preventing re-identification by anyone with access to the original
   Zulip data."
  (:require [tablecloth.api :as tc])
  (:import [javax.crypto Mac]
           [javax.crypto.spec SecretKeySpec]))

(def ^:private salt
  "scicloj-zulipdata-2026-anon-v1")

(def ^:private user-key-len    16)
(def ^:private subject-key-len 16)

(defn- hmac-sha256 ^bytes [^String k ^String msg]
  (let [mac (Mac/getInstance "HmacSHA256")]
    (.init mac (SecretKeySpec. (.getBytes k "UTF-8") "HmacSHA256"))
    (.doFinal mac (.getBytes msg "UTF-8"))))

(defn- bytes->hex [^bytes bs]
  (apply str (map #(format "%02x" %) bs)))

(defn- key-of [^String value len]
  (-> (hmac-sha256 salt value) bytes->hex (subs 0 len)))

(defn user-key
  "Stable, irreversible 16-hex-char identifier for a sender id."
  [sender-id]
  (when sender-id (key-of (str sender-id) user-key-len)))

(defn subject-key
  "Stable 16-hex-char identifier for a topic/subject string. Wide
   enough that two distinct subjects almost never collide, so the
   key uniquely identifies a topic given the full corpus."
  [subject]
  (when subject (key-of subject subject-key-len)))

(defn anonymized-timeline
  "One row per message, anonymized. Sender identity and subject are
   replaced by stable hash keys; message text is replaced by length
   only. Reaction count is kept; the per-emoji breakdown lives in
   `anonymized-reactions`."
  [messages]
  (tc/dataset
   (map (fn [m]
          {:id             (:id m)
           :timestamp      (:timestamp m)
           :stream-id      (:stream_id m)
           :channel        (:display_recipient m)
           :subject-key    (subject-key (:subject m))
           :user-key       (user-key (:sender_id m))
           :client         (:client m)
           :edited         (contains? m :last_edit_timestamp)
           :last-edit-ts   (:last_edit_timestamp m)
           :content-length (count (:content m))
           :reaction-count (count (:reactions m))})
        messages)))

(defn anonymized-reactions
  "One row per (message, reaction). Both the message author's
   subject and the reactor's identity are anonymized; the emoji name
   is preserved (it captures community sentiment, not message
   content)."
  [messages]
  (tc/dataset
   (for [m messages
         r (:reactions m)]
     {:message-id       (:id m)
      :message-ts       (:timestamp m)
      :stream-id        (:stream_id m)
      :channel          (:display_recipient m)
      :subject-key      (subject-key (:subject m))
      :emoji-name       (:emoji_name r)
      :emoji-code       (:emoji_code r)
      :reaction-type    (:reaction_type r)
      :reactor-user-key (user-key (:user_id r))})))

(defn anonymized-edits
  "One row per edit event. Editor and prior subject are anonymized;
   prior content is dropped. `prev-stream` is left as-is — it is a
   stream id, not personal data."
  [messages]
  (tc/dataset
   (for [m messages
         e (:edit_history m)]
     {:message-id       (:id m)
      :stream-id        (:stream_id m)
      :channel          (:display_recipient m)
      :edit-ts          (:timestamp e)
      :editor-user-key  (user-key (:user_id e))
      :prev-subject-key (subject-key (:prev_topic e))
      :prev-stream      (:prev_stream e)})))
