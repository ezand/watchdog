(ns watchdog.core
  (:import [java.nio.file FileSystems WatchService Path WatchEvent$Kind StandardWatchEventKinds])
  (:use [clojure.java.io :as io]))

(def ^:private ^:dynamic *watch-keys* {})
(def ^:private ^:dynamic *callbacks* {})
(def ^:private file-system (FileSystems/getDefault))
(def ^:private watch-service (.newWatchService file-system))
(def ^:private event-kinds
  {:overflow StandardWatchEventKinds/OVERFLOW
   :create StandardWatchEventKinds/ENTRY_CREATE
   :delete StandardWatchEventKinds/ENTRY_DELETE
   :modify StandardWatchEventKinds/ENTRY_MODIFY})

(future
  (loop []
    (let [key (.take watch-service)]
      (doseq [event (.pollEvents key)]
        (let [event-kind (.kind event)]
          (if-not (= event-kind StandardWatchEventKinds/OVERFLOW)
            (let [callback (get *callbacks* key)]
              (let [file (.getPath file-system (str (.watchable key)) (into-array String [(str (.context event))]))]
                (callback {:event (.name event-kind) :file file}))))))
      (.reset key)
      (recur))))

(defn- get-event-kinds [events]
  (if (= events :all)
    (into-array WatchEvent$Kind (vals event-kinds))
    (into-array WatchEvent$Kind (vals (select-keys event-kinds events)))))

(defn- to-path [path]
  (.getPath file-system path (into-array String [])))

(defn- walk-tree [directory]
  (map #(.toPath %1) (filter #(.isDirectory %1) (file-seq (io/file directory)))))

(defn watch
  ([directories events callback] (watch directories true events callback))
  ([directories recursive? events callback]
    (let [all-directories (if recursive? (first (map walk-tree directories)) (map to-path directories))] ;TODO find better way
      (doseq [dir all-directories]
        (let [watch-key (-> dir (.register watch-service (get-event-kinds events)))]
          (alter-var-root (var *watch-keys*) (constantly (conj *watch-keys* (assoc *watch-keys* (str dir) watch-key))))
          (alter-var-root (var *callbacks*) (constantly (conj *callbacks* (assoc *callbacks* watch-key callback)))))))))

(defn unwatch
  ([directories] (un-watch directories true))
  ([directories recursive?]
    (let [all-directories (if recursive? (first (map walk-tree directories)) (map to-path directories))]
      (doseq [directory all-directories]
        (let [key (str directory)]
          (let [watch-key (get *watch-keys* key)]
            (.cancel watch-key)
            (alter-var-root (var *watch-keys*) (constantly (dissoc *watch-keys* key)))))))))

(defn watching [] (map to-path (keys *watch-keys*)))
