(ns watchdog.core
  (:import [java.nio.file FileSystems WatchService Path WatchEvent$Kind StandardWatchEventKinds])
  (:require [me.raynes.fs :as fs])
  (:use [lamina.core]))

(def ^:private ^:dynamic *watch-keys* {})
(def ^:private file-system (FileSystems/getDefault))
(def ^:private file-stores (iterator-seq (.iterator (.getFileStores file-system))))
(def ^:private watch-service (.newWatchService file-system))
(def ^:private event-kinds
  {:overflow StandardWatchEventKinds/OVERFLOW
   :create StandardWatchEventKinds/ENTRY_CREATE
   :delete StandardWatchEventKinds/ENTRY_DELETE
   :modify StandardWatchEventKinds/ENTRY_MODIFY})

(defn- get-event-kinds [events]
  (if (= events "all")
    (into-array WatchEvent$Kind (vals event-kinds))
    (into-array WatchEvent$Kind (vals (select-keys event-kinds events)))))

(defn- to-path [path]
  (.getPath file-system path (into-array String [])))

(defn- walk-tree [directory]
  (map #(.toPath %1) (filter fs/directory? (file-seq (fs/file directory)))))

(defn watch
  ([directories events] (watch directories true events))
  ([directories recursive? events]
    (let [all-directories (if recursive? (first (map walk-tree directories)) (map to-path directories))] ;TODO find better way
      (doseq [dir all-directories]
        (let [watch-key (-> dir (.register watch-service (get-event-kinds events)))]
          (alter-var-root (var *watch-keys*) (constantly (conj *watch-keys* (assoc *watch-keys* (.hashCode dir) watch-key))))
          (let [directory-watcher-agent (agent watch-key)]))))
    (prn *watch-keys*)))

(defn un-watch [directories]
  (doseq [directory directories]
    (let [key (.hashCode directory)]
      (let [watch-key (get *watch-keys* key)]
        (.cancel watch-key)
        (alter-var-root (var *watch-keys*) (constantly (dissoc *watch-keys* key)))))))

(defn start-watching []
  (loop []
    (let [key (.take watch-service)]
      (doseq [event (.pollEvents key)]
        (let [event-kind (.kind event)]
          (prn (str "Event: " (.name event-kind)))
          (let [file (.getPath file-system (str (.watchable key)) (into-array String [(str (.context event))]))]
            (prn (str "File: " file)))))
      (.reset key)
      (recur))))

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
