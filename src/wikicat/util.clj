(ns wikicat.util
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [me.shenfeng.mustache :as mustache]))

; please check with ns id = 14 at the page
; https://{{lang}}.wikipedia.org/w/api.php?action=query&meta=siteinfo&siprop=namespaces
(def category-ns-of {
        :en "Category"
        :de "Kategorie"
        :es "Categoría"
        :fr "Catégorie"
        :zh "Category"})

(mustache/deftemplate tmpl-fn (slurp "templates/article.tpl"))

(defn category? [lang page]
  (.startsWith page (str (lang category-ns-of) ":")))

(defn specials? [page]
  (.contains page ":"))

(defn to-name [page]
  (.substring page (inc (.lastIndexOf page ":"))))

(defn to-category [lang page]
  (str (lang category-ns-of) ":" page))

(defn to-file-name [lang page]
  (str
    (.. (to-name page)
        (replace "'" "_")
        (replace "," "_")
        (replace " " "_")
        (replace "(" "[")
        (replace ")" "]"))
    (if (category? lang page) "" ".yaml")))

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

(defn mk-langlinks [lang page]
  (sort-by #(:lang %)
           (into [{:lang (name lang) :name page}]
                 (clojure.set/rename (query-langlinks lang page) {:* :name}))))

(defn mk-categories [lang page]
  (sort-by last
    (map #(hash-map :name %)
      (map to-name
        (query-categories lang page)))))

(defn gen-content [lang page tree]
  (let [treepath (map #(hash-map :name %) tree)
        langlinks (mk-langlinks lang page)
        allcategories (map #(hash-map :lang (:lang %) :categories
                                      (mk-categories (:lang %) (:name %)))
                           langlinks)]
    (tmpl-fn {:treepath treepath :names langlinks :allcategories allcategories})))
