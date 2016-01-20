(ns ring-middleware-logging.request-logger-test
  (:require
    [ring.mock.request :as mock]
    [clojure.test :refer :all]
    [compojure.core :refer :all]
    [ring.util.response :only [redirect response content-type]]
    [compojure.route :as route]
    [clojure.tools.logging :as log]
    [ring-middleware-logging.log-wrappers :as log-wrappers]))

(def response-stub {:status  200
                    :headers {"content-type" "text/plain"}
                    :body    "Your expected result"})

(def fail-response-stub {:status  400
                         :headers {"content-type" "text/plain"}
                         :body    "bad input"})

(defroutes my-routes
           (GET "/test" [] "okay")
           (GET "/boom" [] (throw (Exception. "boom!" (Exception. "original cause"))))
           (GET "/megaboom" [] (assert false))
           (route/not-found "Url not found"))

(deftest ok-log-msg
  (is (= "Completing :get /test/url in 44.4 ms, status 200"
         (log-wrappers/make-log-msg (mock/request :get "/test/url") response-stub 44.43333))))

(deftest fail-log-msg
  (is (= "Completing :post /bad in 11.1 ms, status 400"
         (log-wrappers/make-log-msg (mock/request :post "/bad") fail-response-stub 11.09422))))

(deftest survive-null-values
  (is (= "Completing null null in 11.1 ms, status null"
         (log-wrappers/make-log-msg nil nil 11.09422))))

(deftest request-passes-through-request-logger
  (is (= "okay"
         ((my-routes (mock/request :get "/test")) :body)
         (((log-wrappers/request-logging my-routes) (mock/request :get "/test")) :body))))

(deftest request-passes-through-exception-logger
  (is (= "okay"
         ((my-routes (mock/request :get "/test")) :body)
         (((log-wrappers/exception-logging my-routes) (mock/request :get "/test")) :body))))

(deftest log-message
  (let [log-posts (transient [])
        ; capture the logging output into the log-posts mutable list
        ; we redef log* since log/info and friends are macros (which can't be redefed in this way)
        _ (with-redefs [log/log* (fn [_logger level _throwable message] (conj! log-posts [level message]))]
            ((log-wrappers/request-logging my-routes) (mock/request :get "/test")))
        log-posts (persistent! log-posts)
        first-log-post (first log-posts)]

    (is (= 1 (count log-posts)))
    (is (= :info (first first-log-post)))
    (is (re-matches #"Completing :get /test in \d\.\d* ms, status 200" (second first-log-post)))))

(deftest exception-causes-500-response
  (let [response ((log-wrappers/exception-logging my-routes) (mock/request :get "/boom"))
        response-body (response :body)]
    (is (= 500 (response :status)))
    (println response-body)
    (is (re-find #"(?m)^internal server error \(send this error id to a developer:.*\)$"
                    response-body))
    (is (re-find #"(?m)^java.lang.Exception: boom!\n at ring_middleware_logging.request_logger_test/fn \(request_logger_test.clj:"
                    response-body))
    (is (re-find #"(?m)^Caused by: java.lang.Exception: original cause$"
                    response-body))))

(deftest error-causes-500-response
  (let [response ((log-wrappers/exception-logging my-routes) (mock/request :get "/megaboom"))
        response-body (response :body)]
    (is (= 500 (response :status)))
    (println response-body)
    (is (re-find #"(?m)^internal server error \(send this error id to a developer:.*\)$"
                 response-body))))

(deftest log-exception
  (let [log-posts (transient [])
        ; capture the logging output into the log-posts mutable list
        ; we redef log* since log/info and friends are macros (which can't be redefed in this way)
        _ (with-redefs [log/log* (fn [_logger level throwable message] (conj! log-posts [level message throwable]))]
            ((log-wrappers/exception-logging my-routes) (mock/request :get "/boom")))
        log-posts (persistent! log-posts)
        first-log-post (first log-posts)]

    (is (= 1 (count log-posts)))
    (is (= :error (first first-log-post)))
    (is (re-matches #"Exception when handling :get /boom \(error id:.*\)" (second first-log-post)))
    (is (= "boom!" (.getMessage (last first-log-post))))))

(deftest log-exception-error-id
  (let [log-posts (transient [])
        ; capture the logging output into the log-posts mutable list
        ; we redef log* since log/info and friends are macros (which can't be redefed in this way)
        response (with-redefs [log/log* (fn [_logger level throwable message] (conj! log-posts [level message throwable]))]
                   ((log-wrappers/exception-logging my-routes) (mock/request :get "/boom")))
        log-posts (persistent! log-posts)
        first-log-post (first log-posts)]

    (is (= 1 (count log-posts)))
    (let [id1 (second (re-matches #"(?s).*error id: (.*)\)" (second first-log-post)))
          id2 (second (re-matches #"(?s).*send this error id to a developer: (.*?)\).*" (response :body)))]
      (is (some? id1))
      (is (some? id2))
      (is (= id1 id2)))))

(deftest log-exception-and-response
  (let [log-posts (transient [])
        ; capture the logging output into the log-posts mutable list
        ; we redef log* since log/info and friends are macros (which can't be redefed in this way)
        _ (with-redefs [log/log* (fn [_logger level throwable message] (conj! log-posts [level message throwable]))]
            ((-> my-routes log-wrappers/exception-logging log-wrappers/request-logging) (mock/request :get "/boom")))
        log-posts (persistent! log-posts)
        exception-log-post (first log-posts)
        response-log-post (second log-posts)]

    (is (= 2 (count log-posts)))

    (is (= :error (first exception-log-post)))
    (is (re-matches #"Exception when handling :get /boom \(error id: (.*)\)" (second exception-log-post)))
    (is (= "boom!" (.getMessage (last exception-log-post))))

    (is (= :info (first response-log-post)))
    (is (re-matches #"Completing :get /boom in \d+\.\d* ms, status 500" (second response-log-post)))))
