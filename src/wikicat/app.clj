(ns wikicat.app
  (:use wikicat.util)
  (:gen-class))

(def max-depth 4)
(def counter (atom 0))

(def root-path (java.io.File. "corpus"))

(def langs [:en :de :es :fr :zh-cn :zh-tw])
(def root-categories {
  :en [
         ;culture-and-arts
         "Arts" "Culture" "Entertainment" "Games" "Humanities"
         "Mass media" "Recreation" "Sports" "Toys"
         ;geography-and-places
         "Geography" "Places"
         ;health-and-fitness
         "Self care" "Public health" "Health science"
         ;history-and-events
         "Natural sciences" "Nature" "Science"
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
         "Applied sciences" "Technology"]})

(defn traverse-tree [page path depth]
    (Thread/sleep (* (+ 200 (rand-int 200)) @counter))
    (let [filename (to-file-name page)
          curpath (java.io.File. path filename)]
        (cond
            (category? page)
                (when (>= depth 0)
                    (println (str ".. " curpath))
                    (.mkdir curpath)
                    (let [subcats (query-subcat :en page)
                          articles (query-article :en page)]
                      (doseq [article articles]
                          (traverse-tree article curpath depth))
                      (doseq [subcat subcats]
                        (let [false-cats?
                                (or (empty? articles) (re-find #" by " subcat))]
                          (if false-cats?
                              (traverse-tree subcat curpath depth)
                              (traverse-tree subcat curpath (dec depth)))))))
            (not (specials? page))
                (when (zero? (.length curpath))
                    (if (not (.exists curpath))
                        (with-open [newfile (java.io.FileWriter. curpath)]
                            (println (str "-> " curpath))
                            (if-let [text (gen-content page)]
                              (.write newfile text)))
                        (println (str "-- " curpath)))))))

(defn -main []
  (doseq [root-cat (:en root-categories)]
    (let [root-page (str "Category:" root-cat)]
      (.start (Thread. (fn []
        (swap! counter inc)
        (traverse-tree root-page root-path max-depth)
        (swap! counter dec)))))))
