(ns wikicrawl.config)

(def max-depth 4)

(def root-path (java.io.File. "corpus"))

;(def langs #(:en :de :es :fr :zh)
(def langs #{:en :zh})

(def category-ns-of {
  :en "Category"
;  :de "Kategorie"
;  :es "Categoría"
;  :fr "Catégorie"
  :zh "Category"})

(def blacklist {
  :en [#"^Template" #"partals?$"]
  :zh [#"(模版|模板|消歧義|消歧义|重定向|维基资源)$"]})

(def lang-variant {:zh "zh-cn" :en "en"})

(def root-categories {
  :en [  ;culture-and-arts
         "Arts" "Culture" "Entertainment" "Games" "Humanities"
         "Mass media" "Recreation" "Sports" "Toys"
         ;geography-and-places
         "Geography" "Places"
         ;health-and-fitness
         "Self care" "Public health" "Health sciences"
         ;history-and-events
         "History" "Events"
         ;mathematics-and-logic
         "Abstraction" "Mathematics"
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
         "艺术" "人文学科"
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
