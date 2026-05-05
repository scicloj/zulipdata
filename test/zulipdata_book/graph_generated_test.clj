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


(def v3_l46 (def sample-channels (pull/web-public-channel-names)))


(def
 v4_l49
 (def
  timeline
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r)))
   anon/anonymized-timeline
   nar/with-time-columns)))


(def v5_l56 timeline)


(def v7_l65 (def u->chans (graph/user-channel-sets timeline)))


(def v8_l67 (count u->chans))


(def v10_l71 (->> u->chans (take 5) (into {})))


(def
 v12_l75
 (->> u->chans vals (map count) frequencies (into (sorted-map))))


(def
 v14_l88
 (def
  co-channel
  (graph/channel-comembership-graph timeline :min-shared 1)))


(def v15_l91 (.vertexSet co-channel))


(deftest t16_l93 (is (= v15_l91 (set sample-channels))))


(def v17_l96 (count (.edgeSet co-channel)))


(deftest
 t19_l101
 (is
  (=
   v17_l96
   (let [n (count (.vertexSet co-channel))] (/ (* n (dec n)) 2)))))


(def
 v21_l107
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
 v23_l123
 (def
  co-user
  (graph/user-copresence-graph timeline :min-shared 3 :min-channels 3)))


(def
 v25_l128
 {:nodes (count (.vertexSet co-user)),
  :edges (count (.edgeSet co-user))})


(def
 v27_l142
 (def
  migration
  (graph/migration-graph timeline #{"clojurecivitas"} :min-users 1)))


(def
 v28_l145
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


(def
 v30_l159
 (->>
  (graph/betweenness co-channel)
  (sort-by val >)
  (take 5)
  (into (array-map))))


(def
 v32_l173
 (boolean (some pos? (vals (graph/betweenness co-channel)))))


(deftest t33_l175 (is (= v32_l173 true)))


(def v35_l186 (graph/girvan-newman co-channel 2))


(def v36_l188 (count (graph/girvan-newman co-channel 2)))


(deftest t37_l190 (is (= v36_l188 2)))


(def v39_l197 (graph/label-propagation co-channel))


(def v40_l199 (count (graph/label-propagation co-channel)))


(deftest t41_l201 (is (= v40_l199 1)))


(def
 v43_l216
 (def
  co-channel-tight
  (graph/channel-comembership-graph timeline :min-shared 5)))


(def
 v44_l219
 (kind/cytoscape
  {:elements (graph/->cytoscape-elements co-channel-tight),
   :style
   [{:selector "node", :css {:label "data(id)", :content "data(id)"}}
    {:selector "edge", :css {:width "mapData(weight, 0, 50, 1, 8)"}}],
   :layout {:name "cose"}}))


(def
 v46_l234
 (def
  co-channel-dot
  (graph/->dot
   co-channel-tight
   :directed
   false
   :edge-label
   (fn [[_ _ w]] (str (long w))))))


(def v47_l239 (kind/graphviz co-channel-dot))
