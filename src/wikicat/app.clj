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
         "Applied sciences" "Technology"]
  :zh [  ;生活、艺术与文化
         "生活" "艺术" "文化" "收藏" "飲食" "服裝" "交通" "體育" "娛樂" "旅遊" "游戏"
         "愛好" "工具" "音乐" "舞蹈" "电影" "戏剧" "电视" "摄影" "繪畫" "雕塑"
         "手工艺" "家庭" "文明" "文物" "節日" "虛構" "符號" "次文化" "動畫" "漫畫"
         ;中華文化
         "中國歷史" "中國神話" "中國音樂" "戏曲曲艺" "中華民俗" "中國文學"
         "中文古典典籍" "武術" "中醫" "国画" "書法" "佛教" "道教" "生肖"
         ;社会
         "社会" "文化" "歷史" "語言" "宗教" "教育" "家庭" "組織" "族群" "經濟" "政治"
         "政府" "國家" "傳統" "產業" "媒體" "体育" "安全" "法律" "犯罪" "獎勵" "城市"
         ;宗教及信仰
         "宗教" "信仰" "各國宗教" "宗教人物" "宗教史" "宗教建筑" "宗教節日" "宗教哲學"
         "宗教場所" "宗教學" "宗教組織" "神祇" "神话" "神學"
         ;世界各地
         "亞洲" "非洲" "大洋洲" "北美洲" "南美洲" "歐洲" "南极洲"
         ;人文與社会科学
         "人文" "社会科学" "哲學" "文学" "艺术" "语言学" "歷史學" "地理学" "心理學"
         "社會學" "政治學" "法學" "軍事學" "传播学" "新闻学" "考古學" "人類學" "民族学"
         "教育學" "圖書資訊科學" "經濟學" "人口学" "家政学" "管理學" "性學"
         ;自然與自然科学
         "自然" "自然科学" "生物" "動物" "植物" "氣象" "季節" "化學元素" "礦物" "地理"
         "数学" "物理學" "力學" "化學" "天文學" "星座" "地球科學" "地質學" "生物學"
         "醫學" "藥學" "农学" "資訊科學" "系统科学" "密碼學"
         ;工程、技术與應用科學
         "科技" "应用科学" "交通" "建筑学" "土木工程" "电气工程" "计算机科学"
         "机械工程" "能源科学" "测绘学" "航空航天" "礦業" "冶金学" "印刷" "化學工程"
         "水利工程" "通信技術" "生物工程" "材料科学" "環境科學"]
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
                        (let [false-cats?
                                (or (empty? articles) (re-find #" by " subcat))]
                          (if false-cats?
                              (traverse-tree lang subcat newtree curpath depth)
                              (traverse-tree lang subcat newtree curpath (dec depth)))))))
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
      (let [root-page (str "Category:" root-cat)]
        (.start (Thread. (fn []
          (swap! counter inc)
          (traverse-tree lang root-page [] lang-root-path max-depth)
          (swap! counter dec))))))))

(defn -main []
    (crawl-wiki :en)
    (crawl-wiki :zh))

