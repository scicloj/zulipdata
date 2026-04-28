(ns scicloj.zulipdata.graph
  "Graph-shaped views of the corpus, backed by JGraphT.

   The two graphs we routinely care about:

   - **channel co-membership** — undirected weighted; nodes are
     channel names, edges weighted by number of shared users.
   - **user co-presence** — undirected weighted; nodes are
     anonymized user-keys, edges weighted by number of shared
     channels.

   Plus utilities for community detection (Girvan-Newman, Label
   Propagation), betweenness centrality, and conversion to
   `kind/cytoscape` and `kind/graphviz` shapes."
  (:require [tablecloth.api :as tc]
            [clojure.set :as set])
  (:import [org.jgrapht.graph DefaultUndirectedWeightedGraph
            DefaultDirectedWeightedGraph
            DefaultWeightedEdge]
           [org.jgrapht.alg.scoring BetweennessCentrality]
           [org.jgrapht.alg.clustering GirvanNewmanClustering
            LabelPropagationClustering]))

(defn- build-undirected-weighted [edges]
  (let [g     (DefaultUndirectedWeightedGraph. DefaultWeightedEdge)
        nodes (distinct (mapcat (fn [[[a b] _]] [a b]) edges))]
    (doseq [n nodes] (.addVertex g n))
    (doseq [[[a b] w] edges]
      (let [e (.addEdge g a b)]
        (.setEdgeWeight g e (double w))))
    g))

(defn- build-directed-weighted [edges]
  (let [g     (DefaultDirectedWeightedGraph. DefaultWeightedEdge)
        nodes (distinct (mapcat (fn [[[a b] _]] [a b]) edges))]
    (doseq [n nodes] (.addVertex g n))
    (doseq [[[a b] w] edges]
      (let [e (.addEdge g a b)]
        (.setEdgeWeight g e (double w))))
    g))

(defn user-channel-sets
  "Map of user-key → set of channels they posted in. Drops users with
   fewer than `min-channels` channels (default 1)."
  ([timeline] (user-channel-sets timeline 1))
  ([timeline min-channels]
   (->> (tc/rows timeline :as-maps)
        (group-by :user-key)
        (into {} (map (fn [[u msgs]]
                        [u (set (map :channel msgs))])))
        (into {} (filter #(>= (count (val %)) min-channels))))))

(defn channel-comembership-graph
  "Undirected weighted graph: nodes are channels, edges weighted by
   shared user count. `min-shared` filters out edges with fewer than
   N shared users."
  [timeline & {:keys [min-shared] :or {min-shared 1}}]
  (let [u-chan (user-channel-sets timeline)
        edges  (reduce (fn [acc [_ chans]]
                         (if (< (count chans) 2)
                           acc
                           (reduce (fn [a [c1 c2]]
                                     (update a [c1 c2] (fnil inc 0)))
                                   acc
                                   (for [a chans b chans
                                         :when (neg? (compare a b))]
                                     [a b]))))
                       {} u-chan)
        kept   (into {} (filter #(>= (val %) min-shared) edges))]
    (build-undirected-weighted kept)))

(defn user-copresence-graph
  "Undirected weighted graph: nodes are users, edges weighted by
   shared channel count. `min-shared` filters edges; `min-channels`
   filters users (active in ≥ N channels)."
  [timeline & {:keys [min-shared min-channels]
               :or   {min-shared 3 min-channels 3}}]
  (let [u-chan (user-channel-sets timeline min-channels)
        users  (vec (keys u-chan))
        edges  (reduce (fn [acc [u1 u2]]
                         (let [overlap (count (set/intersection
                                               (u-chan u1) (u-chan u2)))]
                           (if (>= overlap min-shared)
                             (assoc acc [u1 u2] overlap)
                             acc)))
                       {}
                       (for [i (range (count users))
                             j (range (inc i) (count users))]
                         [(nth users i) (nth users j)]))]
    (build-undirected-weighted edges)))

(defn migration-graph
  "Directed weighted graph: edge from `from-channel` to `to-channel`
   weighted by the number of users who posted in `from-channel` and
   *later* (after their last post in any `from-set` channel) posted
   in `to-channel`. Excludes self-loops and edges within `from-set`.

   Only users with at least 5 posts in `from-set` are considered."
  [timeline from-set & {:keys [min-users] :or {min-users 3}}]
  (let [rows  (tc/rows timeline :as-maps)
        per-u (group-by :user-key rows)
        edges (reduce
               (fn [acc [_u msgs]]
                 (let [in-from (filter #(from-set (:channel %)) msgs)]
                   (if (< (count in-from) 5)
                     acc
                     (let [last-ts (apply max (map :timestamp in-from))
                           after   (->> msgs
                                        (filter #(and (> (:timestamp %) last-ts)
                                                      (not (from-set (:channel %)))))
                                        (group-by :channel)
                                        keys)
                           sources (set (map :channel in-from))]
                       (reduce (fn [a [s d]]
                                 (update a [s d] (fnil inc 0)))
                               acc
                               (for [s sources d after] [s d]))))))
               {} per-u)
        kept  (into {} (filter #(>= (val %) min-users) edges))]
    (build-directed-weighted kept)))

(defn betweenness
  "Map node → betweenness centrality score."
  [^org.jgrapht.Graph g]
  (-> (BetweennessCentrality. g) .getScores (into {})))

(defn girvan-newman
  "Vector of node-sets, one per cluster. `k` is the desired number
   of clusters."
  [^org.jgrapht.Graph g k]
  (->> (GirvanNewmanClustering. g k)
       .getClustering
       (mapv #(set (seq %)))))

(defn label-propagation
  "Vector of node-sets — communities found by label propagation
   (number of clusters chosen by the algorithm)."
  [^org.jgrapht.Graph g]
  (->> (LabelPropagationClustering. g)
       .getClustering
       (mapv #(set (seq %)))))

(defn ->cytoscape-elements
  "Convert a JGraphT graph to a `:elements` map for `kind/cytoscape`.
   `node-attrs` and `edge-attrs` are optional fns of the node /
   [u v weight] returning a map of extra attributes (merged into
   `:data`)."
  [^org.jgrapht.Graph g
   & {:keys [node-attrs edge-attrs]
      :or   {node-attrs (constantly {})
             edge-attrs (constantly {})}}]
  {:nodes (mapv (fn [n]
                  {:data (merge {:id (str n)}
                                (node-attrs n))})
                (.vertexSet g))
   :edges (mapv (fn [e]
                  (let [u (.getEdgeSource g e)
                        v (.getEdgeTarget g e)
                        w (.getEdgeWeight g e)]
                    {:data (merge {:id     (str u "->" v)
                                   :source (str u)
                                   :target (str v)
                                   :weight w}
                                  (edge-attrs [u v w]))}))
                (.edgeSet g))})

(defn ->dot
  "Render a JGraphT graph as Graphviz DOT source. `directed?` chooses
   between `digraph`/`graph`. `node-label` and `edge-label` are
   optional fns producing label strings."
  [^org.jgrapht.Graph g
   & {:keys [directed? node-label edge-label name]
      :or   {directed?   true
             node-label  str
             edge-label  (constantly nil)
             name        "G"}}]
  (let [arrow (if directed? "->" "--")
        kind  (if directed? "digraph" "graph")
        sb    (StringBuilder.)]
    (.append sb (str kind " " name " {\n"))
    (.append sb "  rankdir=LR;\n  node [shape=box, style=rounded];\n")
    (doseq [n (.vertexSet g)]
      (.append sb (str "  \"" n "\" [label=\"" (node-label n) "\"];\n")))
    (doseq [e (.edgeSet g)]
      (let [u (.getEdgeSource g e)
            v (.getEdgeTarget g e)
            w (.getEdgeWeight g e)
            l (edge-label [u v w])]
        (.append sb (str "  \"" u "\" " arrow " \"" v "\""
                         (when l (str " [label=\"" l "\"]"))
                         ";\n"))))
    (.append sb "}\n")
    (str sb)))
