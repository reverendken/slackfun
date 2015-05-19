(ns slackfun.core-test
  (:require [clojure.test :refer :all]
            [slackfun.core :refer :all]
             [clojure.data.json :as json]))

(defn- is-list-of-strings? [l]
  (and (vector? l)
       (every? string? l)))

(defn is-valid-basic-json? [json-file-name] 
  (let [funny-data (json/read-str (slurp (resource-file-name json-file-name)))]
    (and (vector? funny-data)
         (every? string? funny-data))))

(defn is-valid-agree-json? [json-file-name]
  (let [funny-data (json/read-str (slurp (resource-file-name json-file-name)))]
    (and (map? funny-data)
         (= #{"agreements" "disagree_quantities" "disagree_things"} (set (keys funny-data)))
         (every? is-list-of-strings? (vals funny-data)))))

(defn is-valid-greeting-json? [json-file-name]
  (let [funny-data (json/read-str (slurp (resource-file-name json-file-name)))]
    (and (map? funny-data)
         (every? string? (keys funny-data))
         (every? is-list-of-strings? (vals funny-data)))))

(defn is-valid-appoint-json? [json-file-name]
  (let [funny-data (json/read-str (slurp (resource-file-name json-file-name)))]
    (and (map? funny-data)
         (= #{"titles" "dominions"} (set (keys funny-data)))
         (every? is-list-of-strings? (vals funny-data)))))

(defn is-valid-quest-json? [json-file-name]
  (let [funny-data (json/read-str (slurp (resource-file-name json-file-name)))]
    (and (map? funny-data)
         (= #{"locations" "adjectives" "treasures" "foes"} (set (keys funny-data)))
         (every? #(is-list-of-strings? (get funny-data %)) ["locations" "adjectives" "treasures"])
         (let [foes (get funny-data "foes")]
           (and (= #{"names" "adjectives"} (set (keys foes)))
                (every? is-list-of-strings? (vals foes)))))))

(deftest test-basic-json-formats
  (testing "Test JSON format"  
           (are [json-file-name] (is-valid-basic-json? json-file-name)
                "bruce.json"
                "dune.json"
                "kimjongun.json"
                "lron.json")))
      
(deftest test-agreements
  (testing "Test agree.json format"
           (is is-valid-agree-json? "agree.json")))

(deftest test-greetings
  (testing "Test greetings.json format"
           (is is-valid-greeting-json? "greetings.json")))

(deftest test-appoint
  (testing "Test titles.json"
           (is is-valid-appoint-json? "titles.json")))

(deftest test-quest
  (testing "Test quest.json"
           (is is-valid-quest-json? "quest.json")))
