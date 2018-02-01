(ns ring-middleware-logging.log-wrappers
  (:require [clojure.tools.logging :as log]
            [clojure.stacktrace])
  (:import (java.util UUID)))

(defn exception-logging
  "Logs an error using clojure.tools.logging when a throwable is thrown from the provided handler."
  [handler]
  (fn [request]
    (try (handler request)
         (catch Throwable e
           (let [error-id (str (UUID/randomUUID))]
             (log/error e (format "Exception when handling %s %s (error id: %s)"
                                  (request :request-method) (request :uri) error-id))
             {:status 500
              :body   (str "internal server error (send this error id to a developer: " error-id ")\n" (with-out-str (clojure.stacktrace/print-cause-trace e)))})))))

(defn make-log-msg [request response time-ms]
  "Can handle nil requests and responses, but it will look a bit weird.
   This kind of thing can happen for example if you don't have a not-found handler in
   your routes."
  (format "Completing %s %s in %.1f ms, status %d"
          (get request :request-method nil)
          (get request :uri nil)
          time-ms
          (get response :status nil)))

(defn request-logging [handler]
  (fn [request]
    (let [
          tic-ns   (. System (nanoTime))
          response (handler request)
          toc-ns   (. System (nanoTime))
          time-ms  (float (/ (- toc-ns tic-ns) 1000000))]

      (log/info (make-log-msg request response time-ms))
      response)))
