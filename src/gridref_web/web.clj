(ns gridref-web.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site api]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [gridref.core :refer [parse-gridref parse-coord gridref2coord coord2gridref]]))

(defn parse-figures
  [figures]
  (try (Integer/parseInt figures) (catch Exception e 10)))

(defn gridref
  [grid]
  (str (if-let [coord (gridref2coord (parse-gridref grid))]
         coord
         "Invalid grid reference.")))

(defn coord
  [e n figures]
  (str (if-let [grid (coord2gridref (parse-coord (str e " " n)) (parse-figures figures))]
         grid
         "Invalid coordinate pair.")))

(defroutes routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str ["Hello" :from 'Heroku])})
  (GET "/gridref/:grid" [grid] (gridref grid))
  (GET "/coord/:e,:n" [e n figures] (coord e n figures))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(def handler (-> routes
                 ((if (env :production)
                    wrap-error-page
                    trace/wrap-stacktrace))
                 api))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty handler
                     {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
