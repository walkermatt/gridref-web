(ns gridref-web.web-test
  (:require [clojure.test :refer :all]
            [gridref-web.web :refer :all]
            [ring.mock.request :refer :all]
            [clojure.edn]
            [clojure.data.json]))

(deftest test-substitute
  (testing "Substitute place holder strings"
    (is (= (substitute "Hi {name}!" {"name" "Matt"}) "Hi Matt!"))
    (is (= (substitute "Hi {first} {last}." {"first" "Matt" "last" "Walker"}) "Hi Matt Walker."))
    (is (= (substitute "Hi {first} {last}. {unknown}" {"first" "Matt" "last" "Walker"}) "Hi Matt Walker. {unknown}"))))

(deftest test-convert
  (testing "Valid grid ref returns a map"
    (let [result (convert "ST" nil)]
      (is (= (:gridref result) "ST"))
      (is (= (:coord result) [300000.0 100000.0]))))
  (testing "Valid coord returns a map"
    (let [result (convert "[300000.0 100000.0]" "0")]
      (is (= (:gridref result) "ST"))
      (is (= (:coord result) [300000.0 100000.0]))))
  (testing "Invalid input returns nil"
    (is (= (convert "- this is junk -" nil) nil))))
; (test-convert)

(deftest test-resp-or-nil
  (testing "When result is truthy a response is returned"
    (is (not (nil? (resp-or-nil {})))))
  (testing "When result is falsey nil is returned"
    (is (nil? (resp-or-nil nil)))))
; (test-resp-or-nil)

(deftest test-handler
  (testing "Convert gridref"
    (let [resp (handler (header (request :get "/convert/ST12") "Accept" "application/edn"))]
      (is (= (:status resp) 200))
      (is (re-find #"^application/edn" (get (:headers resp) "Content-Type")))
      (is (= (clojure.edn/read-string (slurp (:body resp))) {:gridref "ST12" :coord [310000.0 120000.0]})))
    (let [resp (handler (header (request :get "/convert/ST12") "Accept" "application/json"))]
      (is (= (:status resp) 200))
      (is (re-find #"^application/json" (get (:headers resp) "Content-Type")))
      (is (= (clojure.data.json/read-str (slurp (:body resp))) {"gridref" "ST12" "coord" [310000.0 120000.0]}))))
  (testing "Convert coord"
    (let [resp (handler (header (query-string (request :get "/convert/123456.0,123456.0") {:figures 6}) "Accept" "application/edn"))]
      (is (= (:status resp) 200))
      (is (re-find #"^application/edn" (get (:headers resp) "Content-Type")))
      (is (= (clojure.edn/read-string (slurp (:body resp))) {:gridref "SR234234" :figures 6 :coord [123456.0 123456.0]})))
    (let [resp (handler (header (query-string (request :get "/convert/123456.0,123456.0") {:figures 6}) "Accept" "application/json"))]
      (is (= (:status resp) 200))
      (is (re-find #"^application/json" (get (:headers resp) "Content-Type")))
      (is (= (clojure.data.json/read-str (slurp (:body resp))) {"gridref" "SR234234" "figures" 6 "coord" [123456.0 123456.0]}))))
  (testing "Invalid input"
    (let [resp (handler (header (query-string (request :get "/convert/-foo-") {:figures 6}) "Accept" "application/edn"))]
      (is (= (:status resp) 400))
      (is (re-find #"^application/edn" (get (:headers resp) "Content-Type")))
      (let [body (clojure.edn/read-string (slurp (:body resp)))]
        (is (= (:status body) "invalid-input"))))
    (let [resp (handler (header (query-string (request :get "/convert/-foo-") {:figures 6}) "Accept" "application/json"))]
      (is (= (:status resp) 400))
      (is (re-find #"^application/json" (get (:headers resp) "Content-Type")))
      (let [body (clojure.data.json/read-str (slurp (:body resp)))]
        (is (= (get body "status") "invalid-input"))))))
; (test-handler)
