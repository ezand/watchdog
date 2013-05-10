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
  (let [watch-key (-> (get-path directory) (.register watch-service (get-event-kinds events)))]
    (let [directory-watcher-agent (agent watch-key)])
    ))

(defn start-watching []
  (loop []
    (let [key (.take watch-service)]
      (prn (-> (.watchable key) (.getFileName)))
      (if (= (.reset key) true) (recur)))))

;  (while true
;    (prn "Listenting...")
;    (let [key (.take watch-service)]
;      (prn (-> (.watchable key) (.getFileName)))
;      (if-not (.reset key) (break))
;      )))

; Clojure agent example
;(defn read-agent-error-handler [agnt, exception]
;  (prn "Whoops! " agnt " had a problem: " exception))
;
;(def read-agent
;  (agent
;    "zero bytes"
;    :validator string?
;    :error-handler read-agent-error-handler))
;
;(defn big-read [old-value seconds]
;  "Pretent to read a really big file"
;  (time (Thread/sleep (* seconds 1000)))
;  "<contents of big file>")
;
;(defn read-watch [key agnt old-value new-value]
;  (prn "File has been read!")
;  (prn (str "New file data is: " new-value))
;  (prn ""))
;
;(add-watch read-agent "reader-01" read-watch)
