(ns hello.routes
  (:require
    [bidi.bidi :as bidi]
    [hello.db :as db]
    [hiccups.runtime]
    [macchiato.util.response :as r])
  (:require-macros
    [hiccups.core :refer [html]]))


(defn not-found [req res raise]
  (-> (html
        [:html
         [:body
          [:h2 (:uri req) " was not found"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))

(defn json-serialization
  "Test 1: JSON serialization"
  [_ res _]
  (-> (clj->js {:message "Hello, World!"})
      (r/ok)
      (r/content-type "application/json")
      (res)))

(defn plaintext [_ res _]
  (-> (r/ok "Hello, World!")
      (r/content-type "text/html")
      (res)))

(defn single-query-test [req res raise]
  (db/run-query
    #(-> (js/JSON.stringify %)
         (r/ok)
         (r/content-type "application/json")
         (res))
    raise))

(defn fortunes-test [req res raise]
  (db/get-fortunes
    #(-> (html
           [:html
            [:body
             (into
               [:table
                [:tr [:th "id"] [:th "message"]]]
               (for [message %]
                 [:tr [:td (:id message)] [:td (:message message)]]))]])
         (r/ok)
         (r/content-type "text/html")
         (res))
    raise))

(defn queries-test [req res raise]
  (db/run-queries
    (or (-> req :route-params :queries) 1)
    #(-> (js/JSON.stringify %)
         (r/ok)
         (r/content-type "text/plain")
         (res))
    raise))

(defn update-test [req res raise]
  (db/update-and-persist
    (or (-> req :route-params :queries) 1)
    #(-> (js/JSON.stringify %)
         (r/ok)
         (r/content-type "text/plain")
         (res))
    raise))

(def routes
  ["/" {""         {:get (fn [_ res _] (res (r/ok "Hello, World!")))}
        "plain"    {:get plaintext}
        "json"     {:get json-serialization}
        "db"       {:get single-query-test}
        "fortunes" {:get fortunes-test}
        "queries"
                   {"/"            {:get single-query-test}
                    ["/" :queries] {:get queries-test}}
        "updates" {"/"            {:get update-test}
                    ["/" :queries] {:get update-test}}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
