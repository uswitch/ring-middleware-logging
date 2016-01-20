(ns ring-middleware-logging.log-wrappers
  (:require [clojure.tools.logging :as log])
  (:import (java.util UUID)))

(defn exception-logging [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (let [error-id (str (UUID/randomUUID))]
             (log/error e (format "Exception when handling %s %s (error id: %s)"
                                  (request :request-method) (request :uri) error-id))
             {:status 500
              :body   (str "internal server error (send this error id to a developer: " error-id ")\n" (with-out-str (clojure.stacktrace/print-cause-trace e)))})))))

(defn make-log-msg [request response time-ms]
  (format "Completing %s %s in %.1f ms, status %d"
          (request :request-method)
          (request :uri)
          time-ms
          (response :status)))

(defn request-logging [handler]
  (fn [request]
    (let [
          tic-ns (. System (nanoTime))
          response (handler request)
          toc-ns (. System (nanoTime))
          time-ms (float (/ (- toc-ns tic-ns) 1000000))
          ]
      (log/info (make-log-msg request response time-ms))
      response)))