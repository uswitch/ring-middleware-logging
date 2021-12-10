# ring-middleware-logging

Ring wrappers to log requests and exceptions to clojure.tools/logging

## Usage

Import it into your ring project by adding this to your project.clj:

```clojure
[uswitch/ring-middleware-logging "0.1.5"]
```

You then only need to add the middleware to your ring handler, e.g:
```clojure
(:require [ring-middleware-logging.log-wrappers :as log-wrappers])

(def app
  (-> #'main-routes
      api
      log-wrappers/exception-logging
      log-wrappers/request-logging
      ))
```

Now, requests and exceptions will be nicely logged:
```clojure
2016-01-21 14:39:39,797 INFO  energy-fraud-detection.web.log-wrappers:invoke - Completing :get /main.html in 0.2 ms, status 200
2016-01-21 14:48:18.331 ERROR ring-middleware-logging.log-wrappers  - Exception when handling :get /boom (error id: eef92cb8-45a9-460d-984a-2cb626eb1751)
java.lang.Exception: boom 3!
	at energy_back_office_accrued_revenue_report.handler$fn__8323.invoke(handler.clj:69)
    ...
	at java.lang.Thread.run(Thread.java:745)
Caused by: java.lang.Exception: original
	... 42 more
2016-01-21 14:48:18.348 INFO  ring-middleware-logging.log-wrappers  - Completing :get /boom in 59.6 ms, status 500
```
In case of an exception, a decently formatted message will be returned to the user as well, 
along with an error id that can be grepped for in the logs.

## Deployment
- Manually increment the version number in project.clj
- drone build will release to internal jars
