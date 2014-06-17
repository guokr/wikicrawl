(ns wikicrawl.text
  (:use [wikicrawl.config]
        [wikicrawl.util])
  (:require [clj-yaml.core :as yaml]))

(defn crawl-file [lang file]
  (try
    (let [pagename (lang (:name (yaml/parse-string (slurp file))))]
      (gen-page lang pagename))
    (catch Throwable e (println e))))

(defn crawl-dir [lang dir]
  (for [file (.listFiles dir)]
    (if (.isFile file) (crawl-file lang file) (crawl-dir lang file))))

(defn crawl-text [lang]
  (let [lang-root-path (java.io.File. root-path (name lang))]
    (crawl-dir lang-root-path)))
