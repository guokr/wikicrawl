(ns wikicat.util
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clj-yaml.core :as yaml]))

(defn category? [page] (.startsWith page "Category:"))
(defn specials? [page] (.contains page ":"))

(defn to-file-name [page]
  (str (.. page
    (replace "Category:" "")
    (replace " " "_")
    (replace "(" "[")
    (replace ")" "]")) (if (category? page) "" ".yaml")))

(defn query-subcat [lang pagename]
  (map #(get % :title)
    (get-in
      (parse-string (get
          (client/get (str "https://" (name lang) ".wikipedia.org/w/api.php?"
              "action=query&format=json&list=categorymembers&cmtype=subcat&"
              "cmtitle=" pagename)) :body) true)
      [:query :categorymembers])))

(defn query-article [lang pagename]
  (map #(get % :title)
    (get-in
      (parse-string (get
          (client/get (str "https://" (name lang) ".wikipedia.org/w/api.php?"
              "action=query&format=json&list=categorymembers&cmtype=page&cmnamespace=0&redirects&"
              "cmtitle=" pagename)) :body) true)
      [:query :categorymembers])))

(defn query-langlinks [lang pagename]
  (Thread/sleep 100)
  (first (map #(get-in (second %) [:langlinks])
    (get-in
      (println
        (parse-string (get
          (client/get (str "https://" (name lang) ".wikipedia.org/w/api.php?"
              "action=query&format=json&list=langlinks&"
              "titles=" pagename)) :body) true))
        [:query :pages]))))

(defn query-categories [lang pagename]
  (Thread/sleep 100)
  (first (map #(get-in (second %) [:categories :title])
    (get-in (parse-string (:body
          (client/get (str "https://" (name lang) ".wikipedia.org/w/api.php?"
              "action=query&format=json&prop=categories&clshow=!hidden&"
              "titles=" pagename))))) [:query :pages])))

(defn mk-langlinks [page]
  (apply hash-map (flatten
    (for [item (query-langlinks :en page)] [(.getKey item) (.getValue item)]))))

(defn mk-categories [lang page]
  (map #(.replace % "Category:" "") (query-categories lang page)))

(defn gen-content [page]
  (let [langlinks (mk-langlinks page)
        categories (map #(mk-categories (first %) (second %)) langlinks)]
    (yaml/generate-string {:names langlinks :categories categories})))





