;; # Preface
;;

^{:clay {:hide-code true}}
(ns index
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   ;; Kindly -- notebook rendering protocol
   [scicloj.kindly.v4.kind :as kind]))

^{:kindly/hide-code true
  :kind/md true}
(->> "README.md"
     slurp
     str/split-lines
     (drop 1)
     (str/join "\n"))

;; ## Chapters in this book

^:kind/hidden
(defn chapter->title [chapter]
  (or (some->> chapter
               (format "notebooks/zulipdata_book/%s.clj")
               slurp
               str/split-lines
               (filter #(re-matches #"^;; # .*" %))
               first
               (#(str/replace % #"^;; # " "")))
      chapter))

^:kind/md
(->> "notebooks/chapters.edn"
     slurp
     edn/read-string
     (mapcat (fn [[part chapters]]
               (cons (format "- %s" part)
                     (map (fn [chapter]
                            (format "  - [%s](zulipdata_book.%s.html)"
                                    (chapter->title chapter)
                                    chapter))
                          chapters))))
     (str/join "\n"))
