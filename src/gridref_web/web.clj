(ns gridref-web.web
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.handler :refer [api]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.util.response :refer [response status]]
            [ring.middleware.format-response :refer [wrap-restful-response]]
            [ring.middleware.stacktrace :as trace]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [gridref.core :refer [parse-gridref parse-coord parse-figures gridref2coord coord2gridref]]))

(defn convert
  [arg figures]
  (if-let [gridref (parse-gridref arg)]
    {:coord (gridref2coord gridref) :gridref gridref}
    (if-let [coord (parse-coord arg)]
      (let [figures (parse-figures figures)]
        {:gridref (coord2gridref coord figures) :figures figures :coord coord}))))

(defn resp-or-nil
  [result]
  (if result (response result)))

(defroutes routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "GridRef"})
  ; Support passing a gridref or space separated coord (spaces must be url encoded)
  (GET "/convert/:arg" [arg figures] (resp-or-nil (convert arg figures)))
  ; Support passing a comma separated coord: 123456,123456
  (GET "/convert/:e,:n" [e n figures] (resp-or-nil (convert (str e " " n) figures)))
  (GET "/convert/*" [:as req] (status (response {:status "invalid-input"
                                                 :message "Please pass a grid reference or coordinate pair for example: /convert/st12 or /convert/123456,123456"}) 400))
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
                 (wrap-restful-response)
                 api))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty handler
                     {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
