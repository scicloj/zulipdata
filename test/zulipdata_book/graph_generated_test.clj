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
  (->
   (pull/pull-channels! sample-channels)
   pull/all-channel-messages
   anon/anonymized-timeline
   nar/with-time-columns)))


(def v5_l54 timeline)


(def v7_l63 (def u->chans (graph/user-channel-sets timeline)))


(def v8_l65 (count u->chans))


(def v10_l69 (->> u->chans (take 5) (into {})))


(def
 v12_l73
 (->> u->chans vals (map count) frequencies (into (sorted-map))))


(def
 v14_l86
 (def
  co-channel
  (graph/channel-comembership-graph timeline :min-shared 1)))


(def v15_l89 (.vertexSet co-channel))


(deftest t16_l91 (is (= v15_l89 (set sample-channels))))


(def v17_l94 (count (.edgeSet co-channel)))


(deftest
 t19_l99
 (is
  (=
   v17_l94
   (let [n (count (.vertexSet co-channel))] (/ (* n (dec n)) 2)))))


(def
 v21_l105
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
 v23_l121
 (def
  co-user
  (graph/user-copresence-graph timeline :min-shared 3 :min-channels 3)))


(def
 v25_l126
 {:nodes (count (.vertexSet co-user)),
  :edges (count (.edgeSet co-user))})


(def
 v27_l140
 (def
  migration
  (graph/migration-graph timeline #{"clojurecivitas"} :min-users 1)))


(def
 v28_l143
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
 v30_l157
 (->>
  (graph/betweenness co-channel)
  (sort-by val >)
  (take 5)
  (into (array-map))))


(def
 v32_l171
 (boolean (some pos? (vals (graph/betweenness co-channel)))))


(deftest t33_l173 (is (= v32_l171 true)))


(def v35_l184 (graph/girvan-newman co-channel 2))


(def v36_l186 (count (graph/girvan-newman co-channel 2)))


(deftest t37_l188 (is (= v36_l186 2)))


(def v39_l195 (graph/label-propagation co-channel))


(def v40_l197 (count (graph/label-propagation co-channel)))


(deftest t41_l199 (is (= v40_l197 1)))


(def
 v43_l210
 (let
  [weights
   (map
    (fn* [p1__41981#] (.getEdgeWeight co-channel p1__41981#))
    (.edgeSet co-channel))
   w-min
   (apply min weights)
   w-max
   (apply max weights)]
  (kind/cytoscape
   {:elements (graph/->cytoscape-elements co-channel),
    :style
    [{:selector "node",
      :css {:label "data(id)", :content "data(id)", :font-size 9}}
     {:selector "edge",
      :css
      {:width (str "mapData(weight, " w-min ", " w-max ", 1, 8)")}}],
    :layout {:name "cose"}})))


(def
 v45_l230
 (def
  co-channel-dot
  (graph/->dot
   co-channel
   :directed
   false
   :edge-label
   (fn [[_ _ w]] (str (long w))))))


(def v46_l235 (kind/graphviz co-channel-dot))
