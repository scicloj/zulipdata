(ns
 zulipdata-book.graph-generated-test
 (:require
  [scicloj.zulipdata.pull :as pull]
  [scicloj.zulipdata.anonymize :as anon]
  [scicloj.zulipdata.narrative :as nar]
  [scicloj.zulipdata.graph :as graph]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [clojure.test :refer [deftest is]]))


(def
 v3_l36
 (def
  fixture-channels
  ["kindly-dev" "tableplot-dev" "clay-dev" "noj-dev"]))


(def
 v4_l39
 (def
  timeline
  (->>
   (pull/pull-channels! fixture-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r)))
   anon/anonymized-timeline
   nar/with-time-columns)))


(def v5_l46 (tc/row-count timeline))


(def v7_l55 (def u->chans (graph/user-channel-sets timeline)))


(def v8_l57 (count u->chans))


(def v10_l61 (->> u->chans (take 5) (into {})))


(def
 v12_l65
 (->> u->chans vals (map count) frequencies (into (sorted-map))))


(def
 v14_l78
 (def
  co-channel
  (graph/channel-comembership-graph timeline :min-shared 1)))


(def v15_l81 (.vertexSet co-channel))


(deftest t16_l83 (is (= v15_l81 (set fixture-channels))))


(def v17_l86 (count (.edgeSet co-channel)))


(def
 v19_l90
 (->>
  (.edgeSet co-channel)
  (map
   (fn
    [e]
    {:from (.getEdgeSource co-channel e),
     :to (.getEdgeTarget co-channel e),
     :weight (.getEdgeWeight co-channel e)}))
  (sort-by :weight >)
  tc/dataset))


(def
 v21_l106
 (def
  co-user
  (graph/user-copresence-graph timeline :min-shared 2 :min-channels 2)))


(def
 v23_l111
 {:nodes (count (.vertexSet co-user)),
  :edges (count (.edgeSet co-user))})


(def
 v25_l126
 (def
  migration
  (graph/migration-graph timeline #{"clay-dev"} :min-users 1)))


(def
 v26_l129
 (->>
  (.edgeSet migration)
  (map
   (fn
    [e]
    {:from (.getEdgeSource migration e),
     :to (.getEdgeTarget migration e),
     :weight (.getEdgeWeight migration e)}))
  (sort-by :weight >)
  tc/dataset))


(def v28_l142 (graph/betweenness co-channel))


(def v30_l158 (graph/girvan-newman co-channel 2))


(def v32_l164 (graph/label-propagation co-channel))


(def
 v34_l174
 (kind/cytoscape
  {:elements (graph/->cytoscape-elements co-channel),
   :style
   [{:selector "node", :css {:label "data(id)", :content "data(id)"}}
    {:selector "edge", :css {:width "mapData(weight, 0, 50, 1, 8)"}}],
   :layout {:name "cose"}}))


(def
 v36_l189
 (def
  co-channel-dot
  (graph/->dot
   co-channel
   :directed?
   false
   :edge-label
   (fn [[_ _ w]] (str (long w))))))


(def v37_l194 (kind/graphviz co-channel-dot))
