(ns wikicrawl.text
  (:use [wikicrawl.config]
        [wikicrawl.util])
  (:require [clj-yaml.core :as yaml]))

(defn crawl-file [lang file]
  (try
    (let [pagename (lang (apply merge (:Names (yaml/parse-string
                                                (slurp file)))))
          path (.getPath file)
          newname (str (.substring path 0 (inc (.lastIndexOf path ".")))
                       "txt")]
      (if (nil? pagename) nil
        (if (zero? (.length (java.io.File. newname)))
          (with-open [newfile (java.io.FileWriter. newname)]
            (if-let [text (gen-page lang pagename)]
              (do
                (println "->" newname)
                (.write newfile text)))))))
      (catch Throwable e (.printStackTrace e))))

(defn crawl-dir [lang dir]
  (doall (doseq [file (.listFiles dir)]
    (if (.isFile file)
      (if (.endsWith (.getName file) ".yaml")
          (crawl-file lang file))
      (crawl-dir lang file)))))

(defn crawl-text [lang]
  (let [lang-root-path (java.io.File. root-path (name lang))]
    (.start (Thread. (fn [] (crawl-dir lang lang-root-path))))))
