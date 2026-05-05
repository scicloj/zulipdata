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
 v3_l44
 (def
  sample-channels
  ["clojurecivitas"
   "scicloj-webpublic"
   "gratitude"
   "events"
   "calva"
   "clojure-uk"
   "clojure-europe"
   "news-and-articles"]))


(def
 v4_l48
 (def
  timeline
  (->>
   (pull/pull-channels! sample-channels)
   (filter (fn [[k _]] (string? k)))
   (mapcat (fn [[_ r]] (pull/all-messages r)))
   anon/anonymized-timeline
   nar/with-time-columns)))


(def v5_l55 timeline)


(def v7_l64 (def u->chans (graph/user-channel-sets timeline)))


(def v8_l66 (count u->chans))


(def v10_l70 (->> u->chans (take 5) (into {})))


(def
 v12_l74
 (->> u->chans vals (map count) frequencies (into (sorted-map))))


(def
 v14_l87
 (def
  co-channel
  (graph/channel-comembership-graph timeline :min-shared 1)))


(def v15_l90 (.vertexSet co-channel))


(deftest t16_l92 (is (= v15_l90 (set sample-channels))))


(def v17_l95 (count (.edgeSet co-channel)))


(deftest
 t19_l100
 (is
  (=
   v17_l95
   (let [n (count (.vertexSet co-channel))] (/ (* n (dec n)) 2)))))


(def
 v21_l106
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
 v23_l122
 (def
  co-user
  (graph/user-copresence-graph timeline :min-shared 3 :min-channels 3)))


(def
 v25_l127
 {:nodes (count (.vertexSet co-user)),
  :edges (count (.edgeSet co-user))})


(def
 v27_l141
 (def
  migration
  (graph/migration-graph timeline #{"clojurecivitas"} :min-users 1)))


(def
 v28_l144
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
 v30_l158
 (->>
  (graph/betweenness co-channel)
  (sort-by val >)
  (take 5)
  (into (array-map))))


(def
 v32_l172
 (boolean (some pos? (vals (graph/betweenness co-channel)))))


(deftest t33_l174 (is (= v32_l172 true)))


(def v35_l185 (graph/girvan-newman co-channel 2))


(def v36_l187 (count (graph/girvan-newman co-channel 2)))


(deftest t37_l189 (is (= v36_l187 2)))


(def v39_l196 (graph/label-propagation co-channel))


(def v40_l198 (count (graph/label-propagation co-channel)))


(deftest t41_l200 (is (= v40_l198 1)))


(def
 v43_l211
 (kind/cytoscape
  {:elements (graph/->cytoscape-elements co-channel),
   :style
   [{:selector "node", :css {:label "data(id)", :content "data(id)"}}
    {:selector "edge", :css {:width "mapData(weight, 0, 50, 1, 8)"}}],
   :layout {:name "cose"}}))


(def
 v45_l226
 (def
  co-channel-dot
  (graph/->dot
   co-channel
   :directed
   false
   :edge-label
   (fn [[_ _ w]] (str (long w))))))


(def v46_l231 (kind/graphviz co-channel-dot))
