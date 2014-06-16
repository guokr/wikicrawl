(ns wikicat.util
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [me.shenfeng.mustache :as mustache]))

(mustache/deftemplate tmpl-fn (slurp "templates/article.tpl"))

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
  (:langlinks (first (vals
    (get-in
        (parse-string (:body
          (client/get (str "https://" (name lang) ".wikipedia.org/w/api.php?"
              "action=query&format=json&prop=langlinks&"
              "titles=" pagename))) true)
        [:query :pages])))))

(defn query-categories [lang pagename]
  (Thread/sleep 100)
  (map #(get % :title)
    (:categories (first (vals (get-in (parse-string (:body
          (client/get (str "https://" (name lang) ".wikipedia.org/w/api.php?"
              "action=query&format=json&prop=categories&clshow=!hidden&"
              "titles=" pagename))) true) [:query :pages]))))))

(defn mk-langlinks [page]
  (sort-by #(:lang %)
           (into [{:lang (name :en) :name page}]
                 ;(map #(clojure.set/rename % {:* :name})
                 (map #(hash-map :lang (:lang %) :name (:* %))
                      (query-langlinks :en page)))))

(defn mk-categories [lang page]
  (sort-by last
    (map #(hash-map :name %)
      (map #(.substring % (inc (.lastIndexOf % ":")))
        (query-categories lang page))))
  )

(defn gen-content [page]
  (let [langlinks (mk-langlinks page)
        allcategories (map #(hash-map :lang (:lang %) :categories
                                      (mk-categories (:lang %) (:name %)))
                           langlinks)]
    (tmpl-fn {:names langlinks :allcategories allcategories})))





