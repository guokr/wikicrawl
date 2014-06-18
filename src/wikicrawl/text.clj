(ns wikicrawl.text
  (:use [wikicrawl.config]
        [wikicrawl.util])
  (:require [clj-yaml.core :as yaml]))

(defn crawl-file [lang file]
  (Thread/sleep (* (+ 400 (rand-int 200)) @counter))
  (try
    (let [pagename (lang (apply merge (:Names (yaml/parse-string
                                                (slurp file)))))
          ck (println "pn:" pagename)
          path (.getPath file)
          newname (str (.substring path 0 (inc (.lastIndexOf path ".")))
                       "txt")]
      (if (nil? pagename) nil
        (with-open [newfile (java.io.FileWriter. newname)]
          (if-let [text (gen-page lang pagename)]
            (do
              (println "->" pagename)
              (.write newfile text))))))
    (catch Throwable e (println e))))

(defn crawl-dir [lang dir]
  (for [file (.listFiles dir)]
    (if (.isFile file)
      (if (.endsWith (.getName file) ".yaml")
        (.start (Thread. (fn []
          (swap! counter inc)
          (crawl-file lang file)
          (swap! counter dec)))))
      (crawl-dir lang file))))

(defn crawl-text [lang]
  (let [lang-root-path (java.io.File. root-path (name lang))]
    (crawl-dir lang root-path)))
