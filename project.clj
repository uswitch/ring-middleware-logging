(defproject uswitch/ring-middleware-logging "0.1.3"
  :description "Ring wrappers to log requests and exception to clojure.tools/logging"
  :url "https://github.com/uswitch/ring-middleware-logging"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.1.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17"]

                 [ring/ring-mock "0.3.0"]])
