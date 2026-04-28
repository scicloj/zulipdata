(ns scicloj.zulipdata.client
  "Zulip REST client for the Clojurians instance, using HTTP basic
   auth with an account email + API key. Credentials are read from
   `ZULIP_EMAIL` / `ZULIP_API_KEY` env vars, or from `~/.zuliprc`."
  (:require [hato.client :as http]
            [charred.api :as json]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def base-url
  "API root for the Clojurians Zulip instance. All `api-get` paths are
   resolved relative to this prefix."
  "https://clojurians.zulipchat.com/api/v1")

(def ^:private clojurians-site "https://clojurians.zulipchat.com")

(defn- read-zuliprc
  "Parse the `[api]` section of `~/.zuliprc` if present.
   Returns a map with :email, :key, :site (any may be nil), or nil
   if the file does not exist."
  []
  (let [f (io/file (System/getProperty "user.home") ".zuliprc")]
    (when (.exists f)
      (with-open [r (io/reader f)]
        (let [in-api? (atom false)
              acc     (atom {})]
          (doseq [line (line-seq r)]
            (let [t (str/trim line)]
              (cond
                (= "[api]" t)                       (reset! in-api? true)
                (and (str/starts-with? t "[")
                     (str/ends-with? t "]"))        (reset! in-api? false)
                (and @in-api? (str/includes? t "="))
                (let [[k v] (str/split t #"=" 2)]
                  (swap! acc assoc (keyword (str/trim k))
                         (str/trim v))))))
          (not-empty @acc))))))

(defn- credentials-from-env []
  (let [email (System/getenv "ZULIP_EMAIL")
        key   (System/getenv "ZULIP_API_KEY")]
    (when (and email key) {:email email :api-key key})))

(defn- credentials-from-rc []
  (when-let [rc (read-zuliprc)]
    (let [{:keys [email key site]} rc]
      (cond
        (and site (not= site clojurians-site))
        (throw (ex-info
                (str "~/.zuliprc points to " site " but this project "
                     "is for " clojurians-site
                     ". Set ZULIP_EMAIL and ZULIP_API_KEY env vars to override.")
                {:zuliprc-site site :expected clojurians-site}))

        (and email key) {:email email :api-key key}))))

(defn- credentials []
  (or (credentials-from-env)
      (credentials-from-rc)
      (throw (ex-info
              (str "No Zulip credentials found. Either:\n"
                   "  - set ZULIP_EMAIL and ZULIP_API_KEY env vars, or\n"
                   "  - place a [api] section in ~/.zuliprc with\n"
                   "      email = your@address\n"
                   "      key = ...\n"
                   "      site = " clojurians-site "\n"
                   "Get your API key from\n"
                   "  Settings → Account & privacy → API key (Show/Generate)")
              {}))))

(def ^:private resolved-credentials (delay (credentials)))

(defn- account-email [] (:email    @resolved-credentials))
(defn- api-key       [] (:api-key  @resolved-credentials))

(def ^:private request-timeout-ms 90000)
(def ^:private max-retries 4)

(defn- http-get-with-retry [url opts]
  (loop [attempt 0]
    (let [result (try
                   (http/get url opts)
                   (catch java.io.IOException e
                     (if (< attempt max-retries) ::retry (throw e)))
                   (catch java.util.concurrent.TimeoutException e
                     (if (< attempt max-retries) ::retry (throw e))))]
      (if (= result ::retry)
        (let [backoff (* 1000 (bit-shift-left 1 attempt))]
          (log/warnf "HTTP retry %d for %s after %dms" (inc attempt) url backoff)
          (Thread/sleep backoff)
          (recur (inc attempt)))
        result))))

(defn api-get
  "Authenticated GET against the Clojurians Zulip API. `path` is
   resolved relative to `base-url`; `query-params` is an optional map.
   Wraps the request in a small retry loop with exponential backoff
   and a 90-second per-request timeout. Returns the JSON body parsed
   with keyword keys."
  ([path] (api-get path nil))
  ([path query-params]
   (-> (http-get-with-retry
        (str base-url path)
        {:basic-auth   {:user (account-email) :pass (api-key)}
         :query-params query-params
         :timeout      request-timeout-ms
         :as           :string})
       :body
       (json/read-json :key-fn keyword))))

(defn whoami
  "Smoke-test: hits `/users/me` and returns a short summary of the
   authenticated identity. Use this after configuring credentials to
   confirm everything works before running a pull."
  []
  (let [u (api-get "/users/me")]
    {:email     (:email u)
     :full-name (:full_name u)
     :user-id   (:user_id u)
     :is-bot?   (:is_bot u)
     :is-admin? (:is_admin u)
     :role      (:role u)}))

(defn get-me
  "Full `/users/me` response for the authenticated account. Use
   `whoami` for a trimmed summary."
  []
  (api-get "/users/me"))

(defn get-streams
  "Full `/streams` response — every stream the authenticated user can
   see. Returns the raw Zulip API map; the stream entries live under
   `:streams`."
  []
  (api-get "/streams"))

(defn get-messages
  "Fetch messages matching a narrow. `narrow` is a vector of maps, e.g.
   [{:operator \"channel\" :operand \"data-science\"}].
   `anchor` may be \"newest\", \"oldest\", \"first_unread\", or a message id.
   Returns up to `num-before + num-after + 1` messages around the anchor."
  [{:keys [narrow anchor num-before num-after apply-markdown]
    :or   {anchor         "newest"
           num-before     100
           num-after      0
           apply-markdown false}}]
  (api-get "/messages"
           {"narrow"         (json/write-json-str narrow)
            "anchor"         anchor
            "num_before"     num-before
            "num_after"      num-after
            "apply_markdown" apply-markdown}))
