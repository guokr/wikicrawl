(ns wikicrawl.app
  (:use [wikicrawl.util]
        [wikicrawl.cat]
        [wikicrawl.text])
  (:gen-class))

(defn -main []
    (crawl-cat :en)
    ;(crawl-cat :zh)
  )

