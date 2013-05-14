(defproject watchdog "1.0.0"
  :description "A Clojure wrapper for the Java 7 directory watcher API"
  :url "http://github.com/ezand/watchdog"
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clojure"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [me.raynes/fs "1.4.2"]])
