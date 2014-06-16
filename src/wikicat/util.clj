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
  (apply merge (into [{:lang :en :name page}]
                     (map #(hash-map :lang (:lang %) :name (:* %))
                          (query-langlinks :en page)))))

(defn mk-categories [lang page]
  (apply #(sorted-set-by (fn [x y] (< (first x) (first y))) %)
    (map #({:name %})
      (map #(.substring % (inc (.lastIndexOf % ":")))
        (query-categories lang page)))))

(defn gen-content [page]
  (let [langlinks (mk-langlinks page)
        allcategories (apply merge
                          (map #(hash-map :lang (first %) :categories
                                          (mk-categories (:lang %) (:name %)))
                               langlinks))]
    (tmpl-fn {:names langlinks :allcategories allcategories})))





