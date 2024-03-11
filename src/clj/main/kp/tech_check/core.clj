(ns kp.tech-check.core
  (:require [io.pedestal.http :as http]
            [environ.core :refer [env]]
            [cheshire.core :as json]))

(defonce data* (atom nil))

(defn- fetch []
  (when (nil? @data*)
    (reset! data* (slurp "https://vaxjo.panorama-gis.se/api/v1/public/objects")))
  @data*)

(defn hello-world [_]
  {:status 200
   :body (fetch)
   :headers {"Content-Type" "application/json"}}) ; Route handler

(def service-map {::http/routes #{["/api/data" :get hello-world :route-name :hello-world]} ; Routes
                  ::http/host   "0.0.0.0"
                  ::http/type   :jetty
                  ::http/join?  false
                  ::http/allowed-origins (constantly true)
                  ::http/port   (Integer. (or (env :port) 5000))}) ; Service map

(defonce server* (atom nil))

(defn start []
  (when (nil? @server*)
    (reset! server*
            (-> service-map http/create-server http/start))))

(defn stop []
  (when-not (nil? @server*)
    (http/stop @server*)
    (reset! server* nil)))

(defn restart []
  (stop)
  (start))

(defn -main [] (start))

(comment
  (restart)
  (start)
  @server*
  @data*
  (json/parse-string @data*)
  (http/stop @server*)
  (stop))