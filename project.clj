(defproject uswitch/ring-middleware-logging "0.1.7"
  :description "Ring wrappers to log requests and exception to clojure.tools/logging"
  :url "https://github.com/uswitch/ring-middleware-logging"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.1"]
                 [compojure "1.7.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.logging.log4j/log4j-core "2.25.1"]
                 [ring/ring-mock "0.3.0"]
                 [org.apache.commons/commons-fileupload2-core "2.0.0-M4"]]
  :plugins [[s3-wagon-private "1.3.4" :exclusions [commons-logging com.fasterxml.jackson.core/jackson-databind]]
            [commons-logging "1.2"]
            [com.fasterxml.jackson.core/jackson-databind "2.5.5"] ;; later versions cause trouble
            ]
  :repositories {"s3-releases"
                 {:url           "s3p://uswitch-internal-jars/releases"
                  :sign-releases false
                  :no-auth       true}
                 "s3-snapshots"
                 {:url           "s3p://uswitch-internal-jars/snapshots"
                  :sign-releases false
                  :no-auth       true}}
  :lein-release {:deploy-via "s3-releases"})
