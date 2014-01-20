(ns gridref-web.web
  (:require [compojure.core :refer [defroutes OPTIONS GET ANY]]
            [compojure.handler :refer [api]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.util.response :refer [response status header]]
            [ring.middleware.format-response :refer [wrap-restful-response]]
            [ring.middleware.cors :refer [wrap-cors]]
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

(defn port-or-nil
  [req]
  (if-let [port (:server-port req)]
    (if (= port 80) nil port)))

(defn convert-url
  [req]
  (str (name (:scheme req)) "://" (:server-name req) (if-let [port (port-or-nil req)] (str ":" port)) "/convert"))

(def usage-body {:routes {"/convert/<gridref>" "Convert a grid reference to coordinate pair."
                          "/convert/<coord>?figures=<n>" "Convert a coordinate pair to grid reference optionally specifying the number of figures."}})

(defroutes routes
  (GET "/" [:as req] (clojure.string/replace (slurp (io/resource "home.html")) #"\{convert_url\}" (convert-url req)))
  ; Support passing a gridref or space separated coord (spaces must be url encoded)
  (GET "/convert/:arg" [arg figures] (resp-or-nil (convert arg figures)))
  ; Support passing a comma separated coord: 123456,123456
  (GET "/convert/:e,:n" [e n figures] (resp-or-nil (convert (str e " " n) figures)))
  (GET "/convert/*" [:as req] (status (response (merge
                                                  usage-body
                                                  {:status "invalid-input"
                                                   :message "Please pass a grid reference or coordinate pair (try: /convert/st12 or /convert/123456,123456)"})) 400))
  (OPTIONS "/*" [] (header (response usage-body) "Allow" "GET"))
  (route/resources "/")
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
                 ; Return JSON, EDN or YAML based on Accept header
                 (wrap-restful-response)
                 ; Allow CORS requests with any origin
                 (wrap-cors :access-control-allow-origin #".*")
                 api))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty handler
                     {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
