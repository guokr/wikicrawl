(ns wikicrawl.app
  (:use [wikicrawl.util]
        [wikicrawl.cat]
        [wikicrawl.text])
  (:gen-class))

(defn -main []
    ;(start-crawl-cat :en)
    ;(start-crawl-cat :zh)
    (make-crawl-worker)
    ;(crawl-text :en)
    ;(crawl-text :zh)
  )

