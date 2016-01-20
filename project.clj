(defproject ring-middleware-logging "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.1.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.13"] ;send slf4j to log4j
                 [log4j/log4j "1.2.17"]

                 [ring/ring-mock "0.3.0"]])
