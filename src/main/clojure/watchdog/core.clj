(ns watchdog.core
  (:import [java.nio.file FileSystems WatchService Path WatchEvent$Kind StandardWatchEventKinds]))

(def ^:private file-system (FileSystems/getDefault))
(def ^:private file-stores (iterator-seq (.iterator (.getFileStores file-system))))
(def ^:private watch-service (.newWatchService file-system))
(def ^:private event-kinds
  {:overflow StandardWatchEventKinds/OVERFLOW
   :create StandardWatchEventKinds/ENTRY_CREATE
   :delete StandardWatchEventKinds/ENTRY_DELETE
   :modify StandardWatchEventKinds/ENTRY_MODIFY})

(defn- get-path [directory]
  (.getPath file-system directory (into-array String [])))

(defn- get-event-kinds [events]
  (if (= events nil)
    (into-array WatchEvent$Kind (vals event-kinds))
    (into-array WatchEvent$Kind (vals (select-keys event-kinds events)))))

(defn watch [directory & events]
  (-> (get-path directory) (.register watch-service (get-event-kinds events))))
