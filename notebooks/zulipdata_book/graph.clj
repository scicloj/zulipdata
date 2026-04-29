;; # Graph views
;;
;; `scicloj.zulipdata.graph` lifts the corpus into graph-shaped
;; structures backed by [JGraphT](https://jgrapht.org/). Two graphs
;; cover most analyses:
;;
;; - **Channel co-membership** — undirected weighted; nodes are
;;   channel names, edges weighted by the number of users who post
;;   in both endpoints.
;; - **User co-presence** — undirected weighted; nodes are
;;   anonymized user-keys, edges weighted by the number of channels
;;   the two users share.
;;
;; A third — **migration** — is directed and answers "after these
;; users last posted in `from-set`, where did they show up next?"
;;
;; Plus utilities for community detection (Girvan-Newman, Label
;; Propagation), betweenness centrality, and conversion to shapes
;; that `kind/cytoscape` and `kind/graphviz` know how to render.

(ns zulipdata-book.graph
  (:require
   [scicloj.zulipdata.pull :as pull]
   [scicloj.zulipdata.anonymize :as anon]
   [scicloj.zulipdata.narrative :as nar]
   [scicloj.zulipdata.graph :as graph]
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]))

;; ## A multi-channel fixture
;;
;; The same web-public anonymized timeline used in
;; [**Narrative helpers**](./zulipdata_book.narrative.html) —
;; small enough to render, large enough to have non-trivial
;; structure.

(def fixture-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"])

(def timeline
  (->> (pull/pull-channels! fixture-channels)
       (filter (fn [[k _]] (string? k)))
       (mapcat (fn [[_ r]] (pull/all-messages r)))
       anon/anonymized-timeline
       nar/with-time-columns))

(tc/row-count timeline)

;; ## Users to channels
;;
;; The building block both whole-graph constructors share:
;; `user-channel-sets` returns a map from user-key to the set of
;; channels they have posted in. `min-channels` is a lower bound —
;; users in fewer than `min-channels` channels are dropped.

(def u->chans (graph/user-channel-sets timeline))

(count u->chans)

;; A peek at five entries:

(->> u->chans (take 5) (into {}))

;; The distribution over how many channels users participate in:

(->> u->chans
     vals
     (map count)
     frequencies
     (into (sorted-map)))

;; ## Channel co-membership
;;
;; `channel-comembership-graph` returns a JGraphT
;; `DefaultUndirectedWeightedGraph`. Nodes are channels, edges are
;; weighted by the number of shared users. The `:min-shared` option
;; drops thin edges.

(def co-channel
  (graph/channel-comembership-graph timeline :min-shared 1))

(.vertexSet co-channel)

(kind/test-last
 (= (set fixture-channels)))

(count (.edgeSet co-channel))

;; The graph is complete on the four channels — every pair shares at
;; least one user — so there are `C(4,2) = 6` edges:

(kind/test-last
 (= 6))

;; The edge-weight table:

(->> (.edgeSet co-channel)
     (map (fn [e]
            {:from   (.getEdgeSource co-channel e)
             :to     (.getEdgeTarget co-channel e)
             :weight (.getEdgeWeight co-channel e)}))
     (sort-by :weight >)
     tc/dataset)

;; ## User co-presence
;;
;; `user-copresence-graph` flips the construction: nodes are users,
;; edges are weighted by shared-channel count. The defaults
;; (`:min-shared 3 :min-channels 3`) are tuned for corpus-scale runs
;; where you want to see only the densely-connected core. We loosen
;; them for our four-channel fixture.

(def co-user
  (graph/user-copresence-graph timeline :min-shared 2 :min-channels 2))

;; Node and edge counts:

{:nodes (count (.vertexSet co-user))
 :edges (count (.edgeSet co-user))}

;; ## Migration: where did people go next?
;;
;; `migration-graph` is directed. For each user with at least five
;; posts in `from-set`, it looks at every channel they posted in
;; *after* their last `from-set` post, and adds an edge from each
;; `from-set` source they used to each later destination. Edges with
;; fewer than `:min-users` are dropped.
;;
;; In our fixture, taking `clojurecivitas` as the seed shows where
;; clojurecivitas posters subsequently appeared (within the four
;; channels we pulled).

(def migration
  (graph/migration-graph timeline #{"clojurecivitas"} :min-users 1))

(->> (.edgeSet migration)
     (map (fn [e]
            {:from   (.getEdgeSource migration e)
             :to     (.getEdgeTarget migration e)
             :weight (.getEdgeWeight migration e)}))
     (sort-by :weight >)
     tc/dataset)

;; ## Centrality
;;
;; `betweenness` returns a map from node to its betweenness centrality
;; score — the share of shortest paths that pass through the node.

(graph/betweenness co-channel)

;; All four scores are zero on this graph — every pair of channels is
;; directly connected, so no node ever lies on the *interior* of a
;; shortest path. Betweenness comes alive on graphs with structural
;; bottlenecks; on a four-node clique there are none.

(every? zero? (vals (graph/betweenness co-channel)))

(kind/test-last
 (= true))

;; ## Communities
;;
;; Two algorithms, both returning a vector of node-sets — one set per
;; cluster.

;; Girvan-Newman needs you to pick `k` (the desired number of
;; clusters). On a four-node graph, `k = 2` produces the most useful
;; split.

(graph/girvan-newman co-channel 2)

(count (graph/girvan-newman co-channel 2))

(kind/test-last
 (= 2))

;; Label propagation chooses `k` itself. On a small dense graph it
;; will often collapse to one cluster — which is informative in its
;; own right.

(graph/label-propagation co-channel)

(count (graph/label-propagation co-channel))

(kind/test-last
 (= 1))

;; ## Rendering: `kind/cytoscape`
;;
;; `->cytoscape-elements` converts a JGraphT graph to the
;; `:elements` shape that `kind/cytoscape` consumes. Optional
;; `:node-attrs` and `:edge-attrs` functions inject extra attributes
;; into each `:data` map — handy for colour-coding by community or
;; thickness by weight.

(kind/cytoscape
 {:elements (graph/->cytoscape-elements co-channel)
  :style    [{:selector "node"
              :css      {:label   "data(id)"
                         :content "data(id)"}}
             {:selector "edge"
              :css      {:width "mapData(weight, 0, 50, 1, 8)"}}]
  :layout   {:name "cose"}})

;; ## Rendering: `kind/graphviz`
;;
;; `->dot` returns a Graphviz DOT string. Pass it straight to
;; `kind/graphviz` for a static rendering. `directed?`, `node-label`,
;; and `edge-label` are optional knobs.

(def co-channel-dot
  (graph/->dot co-channel
               :directed false
               :edge-label (fn [[_ _ w]] (str (long w)))))

(kind/graphviz co-channel-dot)

;; ## A note on scale
;;
;; All examples in this chapter use a four-channel slice. The graph
;; helpers were designed for corpus-scale work — full-corpus analyses
;; over `(pull/pull-public-channels!)` build co-membership graphs of
;; dozens of channels and co-presence graphs of around a thousand
;; users. The same functions, the same shapes, just larger inputs.

;; ## Where to go next
;;
;; You have now walked the full tutorial. From here:
;;
;; - [**API Reference**](./zulipdata_book.api_reference.html) —
;;   every public function in one chapter, with docstrings and a
;;   worked example each. The right place to look when you know
;;   which function you want.
;; - The source under `src/scicloj/zulipdata/` is small enough to
;;   read straight through whenever a docstring leaves you uncertain.
