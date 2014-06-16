(ns wikicat.app
  (:use wikicat.util)
  (:gen-class))

(def max-depth 4)
(def counter (atom 0))

(def root-path (java.io.File. "corpus"))

(def langs [:en :de :es :fr :zh])
(def root-categories {
  :en [  ;culture-and-arts
         "Arts" "Culture" "Entertainment" "Games" "Humanities"
         "Mass media" "Recreation" "Sports" "Toys"
         ;geography-and-places
         "Geography" "Places"
         ;health-and-fitness
         "Self care" "Public health" "Health science"
         ;history-and-events
         "History" "Events"
         ;mathematics-and-logic
         "Abstraction" "Mathematic"
         ;natural-and-physical-sciences
         "Natural sciences" "Nature" "Science"
         ;people-and-self
         "People" "Personal life" "Self" "Surnames"
         ;philosophy-and-thinking
         "Philosophy" "Thought"
         ;religion-and-belief-systems
         "Religion" "Belief"
         ;society-and-social-sciences
         "Society" "Social sciences"
         ;technology-and-applied-sciences
         "Applied sciences" "Technology"]
  :zh [  ;人文、艺术
         "艺术" "人文"
         ;生活、文化
         "生活" "休閒" "娱乐" "体育" "媒体"
         ;地理、地方
         "地理" "地方"
         ;历史、事件
         "历史" "事件"
         ;宗教及信仰
         "宗教" "信仰"
         ;社会、社会科学
         "社会" "社会科学"
         ;自然、自然科学
         "自然" "自然科学"
         ;工程、技术、应用科学
         "技术" "科技" "应用科学"]
})

(defn traverse-tree [lang page tree path depth]
    (Thread/sleep (* (+ 200 (rand-int 200)) @counter))
    (let [filename (to-file-name lang page)
          curpath (java.io.File. path filename)
          newtree (conj tree (to-name page))]
        (cond
            (category? lang page)
                (when (>= depth 0)
                    (println (str ".. " curpath))
                    (let [subcats (query-subcat lang page)
                          articles (query-article lang page)]
                      (doseq [article articles]
                          (traverse-tree lang article newtree curpath depth))
                      (doseq [subcat subcats]
                        (if (< (count articles) 2)
                              (traverse-tree lang subcat newtree curpath depth)
                              (traverse-tree lang subcat newtree curpath (dec depth))))))
            (not (specials? page))
                (when (zero? (.length curpath))
                    (do
                      (clojure.java.io/make-parents curpath)
                      (if (not (.exists curpath))
                          (with-open [newfile (java.io.FileWriter. curpath)]
                              (println (str "-> " curpath))
                              (if-let [text (gen-content lang page tree)]
                                (.write newfile text)))
                          (println (str "-- " curpath))))))))

(defn crawl-wiki [lang]
  (let [lang-root-path (java.io.File. root-path (name lang))]
    (doseq [root-cat (lang root-categories)]
      (let [root-page (to-category lang root-cat)]
        (.start (Thread. (fn []
          (swap! counter inc)
          (traverse-tree lang root-page [] lang-root-path max-depth)
          (swap! counter dec))))))))

(defn -main []
    (crawl-wiki :en)
    (crawl-wiki :zh))

