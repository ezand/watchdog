# WatchDog

WatchDog is a Clojure library that lets you watch for directory- and file changes.

[![Build Status](https://travis-ci.org/ezand/watchdog.png?branch=master)](https://travis-ci.org/ezand/watchdog)

## Install

### Leiningen

```clojure
[watchdog "1.0"]
```

### Maven

```xml
<dependency>
  <groupId>watchdog</groupId>
  <artifactId>watchdog</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage

WatchDog has two main functions, `watch` and `unwatch` which registeres and unregisteres directories against the watch service.

### Watching directories
```clojure
;Function to handle the watch event. This will be called for all registered events.
(defn- watch-callback [watch-event]
  (prn watch-event))

;Watch specified paths including all sub-folders. Listen for all change events.
(watch ["/your/path" "/some/path"] :all watch-callback)

;Watch specified paths excluding all sub-folders. Listen for all change events.
(watch ["/your/path" "/some/path"] false :all watch-callback)
```

### View currently watched paths

```clojure
;Return all currently watched paths
(watching)
```

