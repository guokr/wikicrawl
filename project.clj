(defproject wikicat "0.1.0"

  :description "A crawler to achieve the category structure of wikipedia"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.1"]
                 [clj-yaml "0.4.0"]
                 [enlive "1.1.5"]
                 [me.shenfeng/mustache "1.1"]
                 [cheshire "5.3.1"]]

  :main wikicrawl.app)
