(ns wikicrawl.cat
  (:use [wikicrawl.config]
        [wikicrawl.util]))

(defn traverse-tree [lang page tree path depth progress]
  (Thread/sleep (* (+ 400 (rand-int 200)) @counter))
  (let [filename (to-file-name lang page)
        curpath (java.io.File. path filename)
         newtree (conj tree (to-name page))]
    (if (valid? lang page)
      (cond
        (category? lang page)
          (when (and (>= depth 0) (< progress 10))
            (println (str ".. " curpath))
            (clojure.java.io/make-parents curpath)
            (.mkdir curpath)
            (let [subcats (query-subcat lang page)
                  articles (query-article lang page)]
              (with-open [newfile (java.io.FileWriter.
                           (java.io.File. curpath "_Category"))]
                (if-let [text (gen-content lang page tree)]
                  (.write newfile text)))
              (doseq [article articles]
                (traverse-tree lang article newtree curpath depth progress))
              (doseq [subcat subcats]
                (if (< (count articles) 2)
                  (traverse-tree lang subcat newtree curpath depth
                                 (inc progress))
                  (traverse-tree lang subcat newtree curpath (dec depth)
                                 (inc progress))))))
      (not (specials? page))
        (when (zero? (.length curpath))
          (do
            (clojure.java.io/make-parents curpath)
            (if (not (.exists curpath))
              (with-open [newfile (java.io.FileWriter. curpath)]
                (println (str "-> " curpath))
                (if-let [text (gen-content lang page tree)]
                    (.write newfile text)))
                  (println (str "-- " curpath)))))))))

(defn crawl-cat [lang]
  (let [lang-root-path (java.io.File. root-path (name lang))]
    (doseq [root-cat (lang root-categories)]
      (let [root-page (to-category lang root-cat)]
        (.start (Thread. (fn []
          (swap! counter inc)
          (traverse-tree lang root-page [] lang-root-path max-depth 0)
          (swap! counter dec))))))))

