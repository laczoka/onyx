(ns onyx.peer.params-test
  (:require [midje.sweet :refer :all]
            [onyx.queue.hornetq-utils :as hq-util]
            [onyx.api]))

(def hornetq-host "localhost")

(def hornetq-port 5445)

(def hornetq-cluster-name "onyx-cluster")

(def hornetq-group-address "231.7.7.7")

(def hornetq-refresh-timeout 5000)

(def hornetq-discovery-timeout 5000)

(def hornetq-group-port 9876)

(def id (str (java.util.UUID/randomUUID)))

(def hq-config {"host" hornetq-host "port" hornetq-port})

(def in-queue (str (java.util.UUID/randomUUID)))

(def out-queue (str (java.util.UUID/randomUUID)))

(hq-util/create-queue! hq-config in-queue)
(hq-util/create-queue! hq-config out-queue)

(def n-messages 1000)

(def echo 100)

(def batch-size 100)

(defn my-adder [factor {:keys [n] :as segment}]
  (assoc segment :n (+ n factor)))

(def workflow {:in {:add :out}})

(def coord-opts {:hornetq-cluster-name hornetq-cluster-name
                 :hornetq-group-address hornetq-group-address
                 :hornetq-group-port hornetq-group-port
                 :hornetq-refresh-timeout hornetq-refresh-timeout
                 :hornetq-discovery-timeout hornetq-discovery-timeout
                 :zk-addr "127.0.0.1:2181"
                 :onyx-id id
                 :revoke-delay 5000})

(def peer-opts {:hornetq-cluster-name hornetq-cluster-name
                :hornetq-group-address hornetq-group-address
                :hornetq-group-port hornetq-group-port
                :hornetq-refresh-timeout hornetq-refresh-timeout
                :hornetq-discovery-timeout hornetq-discovery-timeout                
                :zk-addr "127.0.0.1:2181"
                :onyx-id id
                :fn-params {:add [42]}})

(hq-util/write-and-cap! hq-config in-queue (map (fn [x] {:n x}) (range n-messages)) echo)

(def catalog
  [{:onyx/name :in
    :onyx/ident :hornetq/read-segments
    :onyx/type :input
    :onyx/medium :hornetq
    :onyx/consumption :concurrent
    :hornetq/queue-name in-queue
    :hornetq/host hornetq-host
    :hornetq/port hornetq-port
    :onyx/batch-size batch-size}

   {:onyx/name :add
    :onyx/fn :onyx.peer.params-test/my-adder
    :onyx/type :transformer
    :onyx/consumption :concurrent
    :onyx/batch-size batch-size}

   {:onyx/name :out
    :onyx/ident :hornetq/write-segments
    :onyx/type :output
    :onyx/medium :hornetq
    :onyx/consumption :concurrent
    :hornetq/queue-name out-queue
    :hornetq/host hornetq-host
    :hornetq/port hornetq-port
    :onyx/batch-size batch-size}])

(def conn (onyx.api/connect (str "onyx:memory//localhost/" id) coord-opts))

(def v-peers (onyx.api/start-peers conn 1 peer-opts))

(onyx.api/submit-job conn {:catalog catalog :workflow workflow})

(def results (hq-util/read! hq-config out-queue (inc n-messages) echo))

(doseq [v-peer v-peers]
  (try
    ((:shutdown-fn v-peer))
    (catch Exception e
      (.printStackTrace e))))

(try
  (onyx.api/shutdown conn)
  (catch Exception e
    (.printStackTrace e)))

(fact results => (conj (vec (map (fn [x] {:n (+ x 42)}) (range n-messages))) :done))

