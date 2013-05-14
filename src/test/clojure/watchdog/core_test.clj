(ns watchdog.core-test
  (:require [clojure.test :refer :all ]
            [watchdog.core :refer :all :as c])
  (:use [clojure.java.io :as io]))

(def ^:private base-path (io/file "src/test/resources/"))

(defn- watch-callback [event]
  (prn event))

(deftest test-watch
  (testing "Watching directories non-recursive"
    (watch [(str base-path)] false :all watch-callback)
    (is (= (count (watching)) 1)))

  (testing "Watching directories recursive"
    (watch [(str base-path)] :all watch-callback)
    (is (= (count (watching)) 2)))

  (testing "Unwatching non-recursive"
    (unwatch [(str base-path)] false)
    (is (= (count (watching)) 1)))

  (testing "Unwatching recursive"
    (watch [(str base-path)] :all watch-callback)
    (unwatch [(str base-path)])
    (is (= (count (watching)) 0))))
