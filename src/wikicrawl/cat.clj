(ns wikicrawl.cat
  (:use [wikicrawl.config]
        [wikicrawl.util])
  (:require [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]))

(def redis-conn {:pool {} :spec {}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(def handlers (atom {}))

(defn enqueue [kind lang page tree path depth progress]
  (println (str ">> " page))
  (wcar* (car-mq/enqueue "wikicrawl:queue" [kind lang page tree path depth progress])))

(defn traverse-tree [lang page tree path depth progress]
  (let [filename (to-file-name lang page)
        curpath (.getAbsolutePath (java.io.File. path filename))
        newtree (conj tree (to-name page))]
    (if (valid? lang page)
      (cond
        (category? lang page)
          (when (and (>= depth 0) (< progress 10))
            (enqueue :dir lang page tree path depth progress))
        (not (specials? page))
          (when (zero? (.length (java.io.File. curpath)))
            (enqueue :file lang page tree path depth progress))))))

(defn handle-dir [lang page tree path depth progress]
  (let [filename (to-file-name lang page)
        curpath (.getAbsolutePath (java.io.File. path filename))
        newtree (conj tree (to-name page))]
    (println (str ".. " curpath))
    (clojure.java.io/make-parents curpath)
    (.mkdir (java.io.File. curpath))
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
                         (inc progress)))))))

(defn handle-file [lang page tree path depth progress]
  (let [filename (to-file-name lang page)
        curpath (.getAbsolutePath (java.io.File. path filename))]
    (clojure.java.io/make-parents curpath)
    (if (not (.exists (java.io.File. curpath)))
      (with-open [newfile (java.io.FileWriter. curpath)]
        (println (str "-> " curpath))
        (if-let [text (gen-content lang page tree)]
          (.write newfile text)))
      (println (str "-- " curpath)))))

(swap! handlers assoc :dir handle-dir)
(swap! handlers assoc :file handle-file)

(defn make-crawl-worker []
  (car-mq/worker {} "wikicrawl:queue"
    {:handler (fn [{:keys [message attempt]}]
               (let [[kind lang page tree path depth progress] message]
                 (println (str "<< " page))
                 (try
                   ((kind @handlers) lang page tree path depth progress)
                   (catch Throwable e (do
                       (println :retry e)
                       {:status :retry}))))
               {:status :success})}))

(defn start-crawl-cat [lang]
  (let [lang-root-path (.getAbsolutePath (java.io.File. root-path (name lang)))]
    (doseq [root-cat (lang root-categories)]
      (let [root-page (to-category lang root-cat)]
        (traverse-tree lang root-page [] lang-root-path max-depth 0)))))

