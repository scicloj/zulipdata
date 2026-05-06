(ns scicloj.zulipdata.emoji
  "Helpers for displaying Zulip reactions as actual glyphs or images.

   A reaction's `:reaction-type` and `:emoji-code` (as carried in the
   `views/reactions-long` projection) determine what to render:

   - `\"unicode_emoji\"` — `:emoji-code` is a hyphen-separated sequence
     of unicode codepoints. `\"1f64f\"` is 🙏, `\"1f1fa-1f1f8\"` is 🇺🇸.
   - `\"realm_emoji\"` / `\"zulip_extra_emoji\"` — `:emoji-code` is the
     workspace's custom-emoji id; the source URL lives in the
     `/realm/emoji` endpoint."
  (:require [clojure.string :as str]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.zulipdata.client :as client]))

(defn decode-unicode
  "Decode a hyphen-separated unicode codepoint sequence into a string.
   `\"1f64f\"` → \"🙏\", `\"1f1fa-1f1f8\"` → \"🇺🇸\"."
  [emoji-code]
  (->> (str/split emoji-code #"-")
       (mapcat #(Character/toChars (Integer/parseInt % 16)))
       char-array
       String.))

(defn realm-emoji-map
  "Fetch the workspace's custom-emoji map from `/realm/emoji`.
   Returns `{:<id> {:source_url ... :name ... ...}}` keyed by the
   realm emoji's id (as keyword)."
  []
  (-> (client/api-get "/realm/emoji") :emoji))

(defn display
  "Render a reaction as a value suitable for a `kind/table` cell. Given
   a realm-emoji map (from `realm-emoji-map`) and a reaction's three
   identifying fields, returns:

   - For `unicode_emoji`: the glyph as a string.
   - For `realm_emoji` / `zulip_extra_emoji`: a `kind/hiccup` `<img>`
     element pointing at the realm emoji's `:source_url`.
   - Otherwise (or when the realm id is unknown): `nil`.

   In a tablecloth pipeline, the natural call is

       (tc/map-columns :emoji [:reaction-type :emoji-code :emoji-name]
                       (partial emoji/display realm-emoji))"
  [realm-emoji reaction-type emoji-code emoji-name]
  (case reaction-type
    "unicode_emoji"
    (decode-unicode emoji-code)
    ("realm_emoji" "zulip_extra_emoji")
    (when-let [src (get-in realm-emoji [(keyword emoji-code) :source_url])]
      (kind/hiccup [:img {:src src :width 24 :height 24 :alt emoji-name}]))
    nil))
