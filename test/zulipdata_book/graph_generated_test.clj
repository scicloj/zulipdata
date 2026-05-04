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
 v3_l40
 (def
  sample-channels
  ["clojurecivitas" "scicloj-webpublic" "gratitude" "events"]))


(def
 v4_l43
 (def
  timeline
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r)))
   anon/anonymized-timeline
   nar/with-time-columns)))


(def v5_l50 (tc/row-count timeline))


(def v7_l59 (def u->chans (graph/user-channel-sets timeline)))


(def v8_l61 (count u->chans))


(def v10_l65 (->> u->chans (take 5) (into {})))


(def
 v12_l69
 (->> u->chans vals (map count) frequencies (into (sorted-map))))


(def
 v14_l82
 (def
  co-channel
  (graph/channel-comembership-graph timeline :min-shared 1)))


(def v15_l85 (.vertexSet co-channel))


(deftest t16_l87 (is (= v15_l85 (set sample-channels))))


(def v17_l90 (count (.edgeSet co-channel)))


(deftest t19_l95 (is (= v17_l90 6)))


(def
 v21_l100
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
 v23_l116
 (def
  co-user
  (graph/user-copresence-graph timeline :min-shared 2 :min-channels 2)))


(def
 v25_l121
 {:nodes (count (.vertexSet co-user)),
  :edges (count (.edgeSet co-user))})


(def
 v27_l136
 (def
  migration
  (graph/migration-graph timeline #{"clojurecivitas"} :min-users 1)))


(def
 v28_l139
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


(def v30_l152 (graph/betweenness co-channel))


(def v32_l161 (every? zero? (vals (graph/betweenness co-channel))))


(deftest t33_l163 (is (= v32_l161 true)))


(def v35_l174 (graph/girvan-newman co-channel 2))


(def v36_l176 (count (graph/girvan-newman co-channel 2)))


(deftest t37_l178 (is (= v36_l176 2)))


(def v39_l185 (graph/label-propagation co-channel))


(def v40_l187 (count (graph/label-propagation co-channel)))


(deftest t41_l189 (is (= v40_l187 1)))


(def
 v43_l200
 (kind/cytoscape
  {:elements (graph/->cytoscape-elements co-channel),
   :style
   [{:selector "node", :css {:label "data(id)", :content "data(id)"}}
    {:selector "edge", :css {:width "mapData(weight, 0, 50, 1, 8)"}}],
   :layout {:name "cose"}}))


(def
 v45_l215
 (def
  co-channel-dot
  (graph/->dot
   co-channel
   :directed
   false
   :edge-label
   (fn [[_ _ w]] (str (long w))))))


(def v46_l220 (kind/graphviz co-channel-dot))
