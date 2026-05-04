;; # Repository
;;
;; This chapter is for people who clone the repository — to read it,
;; to edit a chapter, or to release a new version. Everything below
;; is about the files in this repo, not the library API itself.
;; If you only want to *use* zulipdata as a dependency, start at the
;; [**Quickstart**](./zulipdata_book.quickstart.html) and follow the
;; tutorial chapters.

(ns zulipdata-book.repository
  (:require
   [scicloj.kindly.v4.kind :as kind]))

;; ## Layout
;;
;; - `src/scicloj/zulipdata/` — library source.
;; - `notebooks/zulipdata_book/` — the book chapters that you are
;;   reading right now.
;; - `notebooks/index.clj` — book preface; reads `README.md` to
;;   avoid duplication.
;; - `notebooks/chapters.edn` — chapter ordering, used by both
;;   `make-book!` and `make-gfm!`.
;; - `notebooks/dev.clj` — small helpers exposed in the dev REPL
;;   for rendering the book.
;; - `test/scicloj/zulipdata/` — hand-written tests.
;; - `test/zulipdata_book/` — tests *generated* from the notebooks.
;; - `docs/` — rendered HTML book, published via GitHub Pages.
;; - `gfm/` — GitHub-flavoured Markdown renders, used as a
;;   verification step for the tutorial chapters.

;; ## REPL workflow
;;
;; Start a REPL with the `:dev` alias, then connect from your editor:
;;
;; ```bash
;; clojure -M:dev -m nrepl.cmdline
;; ```
;;
;; The `:dev` alias adds the `notebooks/` and `test/` directories to
;; the classpath, plus the rendering dependencies (Clay, plotje, and
;; other rendering libraries) that the notebooks need.

;; ## Rendering the book
;;
;; The notebook helper `dev/make-book!` builds the full HTML book
;; into `docs/` via Clay + Quarto. That directory is what GitHub
;; Pages publishes:
;;
;; ```clojure
;; (require '[dev :as dev])
;; (dev/make-book!)
;; ```
;;
;; To render a single chapter for quick iteration, pass a path to
;; `dev/make-gfm!` — same Clay pipeline, GitHub-flavoured Markdown
;; output, into `gfm/`:
;;
;; ```clojure
;; (dev/make-gfm! "zulipdata_book/quickstart.clj")
;; ```
;;
;; Calling `dev/make-gfm!` with no arguments renders every registered
;; chapter.

;; ## GFM as a verification step
;;
;; The published documentation site is the HTML build under `docs/`.
;; The Markdown files under `gfm/` are not the publishing target —
;; they exist because Clay's GFM render also writes a generated test
;; file per notebook to `test/zulipdata_book/<name>_generated_test.clj`.
;; Each `kind/test-last` form in a notebook becomes a `clojure.test`
;; assertion in that file.
;;
;; The standard test runner picks up both the hand-written tests
;; under `test/scicloj/zulipdata/` and the generated tests, so a
;; full pass guarantees that every notebook example evaluates to
;; what its prose claims.
;;
;; The flow when you change a notebook:
;;
;; 1. Edit the notebook under `notebooks/zulipdata_book/`.
;; 2. `(dev/make-gfm! "zulipdata_book/<name>.clj")` — re-renders the
;;    GFM and regenerates the test file.
;; 3. `./run_tests.sh` — confirms the assertions still hold.
;; 4. `(dev/make-book!)` — rebuilds the HTML book in `docs/` for
;;    publication.

;; ## Running tests
;;
;; ```bash
;; ./run_tests.sh
;; ```
;;
;; The runner is the cognitect test-runner via the `:test` alias and
;; covers both directories under `test/`. The notebook-derived tests
;; pull live data from Zulip the first time (cache-served thereafter),
;; so the suite needs valid credentials — see `quickstart` if you
;; have not configured them yet.

;; ## Releases
;;
;; Two scripts wrap `clojure -T:build`:
;;
;; ```bash
;; ./snapshot.sh   # build + deploy a SNAPSHOT to Clojars
;; ./release.sh    # build + deploy a tagged release
;; ```
;;
;; Both run the test suite first via `clojure -T:build ci`. The
;; library coordinates and version live in `build.clj`.

;; ## Where to send things
;;
;; - **Chapter prose or example fixes** — edit the notebook,
;;   regenerate, send a PR. The generated test file is checked in.
;; - **Library bug or feature** — open an issue or PR against the
;;   relevant namespace under `src/scicloj/zulipdata/`.
