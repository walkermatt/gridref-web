(defproject gridref-web "0.1.1"
  :description "A super simple service to convert an alpha numeric Ordnance Survey grid reference to easting / northing or easting / northing to a grid reference."
  :url "http://gridref.longwayaround.org.uk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [ring/ring-devel "1.2.1"]
                 [ring-middleware-format "0.3.1"]
                 [org.apache.commons/commons-lang3 "3.1"]
                 [ring-cors "0.1.0"]
                 [org.clojure/data.json "0.2.3"]
                 [environ "0.4.0"]
                 [gridref "0.1.4"]
                 [ring-mock "0.1.5"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ring "0.8.8"]]
  :ring {:handler gridref-web.web/handler}
  :hooks [environ.leiningen.hooks]
  :main gridref-web.web
  :profiles {:production {:env {:production false}}})
