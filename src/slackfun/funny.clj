(ns slackfun.funny
  (:require [slackfun.core :refer (resource-file-name sendMessage is-real-user?)]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.repl :refer (doc)]))

(def ^:private CHUCK_URL "http://api.icndb.com/jokes/random")

(defn- format-target [whom]
  (if (is-real-user? whom)
    (format "<@%s>" whom)
    whom))

(defn- format-dune-quote [quote]
  (clojure.string/join "\n" (map #(format "> %s" %1) (clojure.string/split-lines quote))))

(defn- create-funny-txt [input-file-name message-format]
  (let [quote-store (atom nil)
        get-quotes #(or @quote-store (reset! quote-store
                                             (clojure.string/split-lines
                                               (slurp
                                                 (resource-file-name input-file-name)))))]
    (fn [whom & {:keys [conf] :or {conf "random"}}]
      (sendMessage conf (format message-format (format-target whom) (rand-nth (get-quotes)))))))

(defn- create-funny-json [input-file-name message-format]
  (let [quote-store (atom nil)
        get-quotes #(or @quote-store (reset! quote-store
                                             (json/read-str
                                               (slurp (resource-file-name input-file-name)))))]
    (fn [whom & {:keys [conf] :or {conf "random"}}]
      (sendMessage conf (format message-format (format-target whom) (format-dune-quote
                                                                      (rand-nth
                                                                        (get-quotes))))))))

(defn- get-chuck-fact []
  (clojure.string/replace (:joke (:value (:body (client/get CHUCK_URL {:as :json})))) "&quot;" "\""))

(def slap "Slap some sense into someone by means of aquatic wildlife"
  (create-funny-txt
    "trout.txt"
    ":fish: slaps %s with a %s"))

(defn ^:pandora chuck "Reveal something awesome about Chuck Norris"
  [whom & {:keys [conf] :or {conf "random"}}]
  (sendMessage conf (format ":hoss: astounds %s with a *FACT* about Chuck Norris:\n> %s" (format-target whom) (get-chuck-fact))))

(def ^:pandora dune "Eventually you will need to face my gom jabbar"
  (create-funny-json
    "dune.json"
    ":wormsign: scrapes the sand off of the wisdom of Dune for %s:\n%s"))

(def ^:pandora bofh "We save on tech support costs by automating the helpdesk"
  (create-funny-txt
    "bofh.txt"
    ":troll: diagnoses %s's computer problem: %s"))

(def ^:pandora bruce "The secret key is hidden in this docstring"
  (create-funny-json
    "bruce.json"
    ":lock: decrypts a Bruce Schneier fact for %s:\n%s"))

(def ^:pandora lron "Let's all join the Sea Org"
  (create-funny-json
    "lron.json"
    ":rocket: baffles %s with the ramblings of L Ron Hubbard:\n%s"))

(def ^:pandora kim "I really have no appropriate docstring for this"
  (create-funny-json
    "kimjongun.json"
    ":fist: inspires %s with the galvanizing slogans of Kim Jong-un:\n%s"))

(let [slack-greetings (atom nil)
      greetings #(or
                   @slack-greetings
                   (reset! slack-greetings
                           (json/read-str (slurp (resource-file-name "greetings.json")))))]
  (defn hello "Politeness is the foundation of civilization"
    [whom & {:keys [conf] :or {conf "random"}}]
    (let [which-language (rand-nth (keys (greetings)))]
      (sendMessage conf (format ":wave: greets %s in %s: `%s`" (format-target whom) which-language (rand-nth (get (greetings) which-language))))))

  (defn ^:pandora access-book "Express gratitude with a useful technical manual"
    [whom & {:keys [conf] :or {conf "random"}}]
    (let [which-language (rand-nth (keys (greetings)))]
      (sendMessage conf (format ":books: thanks %s with a gift of a Microsoft Access 97 book written in %s"
                                (format-target whom) which-language)))))

(let [quest-store (atom nil)
      quests #(or @quest-store
                  (reset! quest-store (json/read-str
                                               (slurp (resource-file-name "quest.json")))))
      gen-quest-location #(format "%s of %s"
                                  (rand-nth (get (quests) "locations"))
                                  (rand-nth (get (quests) "adjectives")))
      gen-quest-treasure-simple #(rand-nth (get (get (quests) "treasures") "plain"))
      gen-quest-treasure-complex #(format "%s of %s"
                                          (rand-nth (get (get (quests) "treasures") "extended"))
                                          (rand-nth (get (quests) "adjectives")))
      gen-quest-treasure #((rand-nth [gen-quest-treasure-simple gen-quest-treasure-complex]))
      gen-quest-foe #(let [foe-info (get (quests) "foes")]
                       (format "%s of %s"
                               (rand-nth (get foe-info "names"))
                               (rand-nth (get foe-info "adjectives"))))]
  (defn ^:pandora quest "Bestow a noble quest upon someone"
    [whom & {:keys [conf] :or {conf "random"}}]
    (let [quest-location (gen-quest-location)
          quest-foe (gen-quest-foe)
          quest-treasure (gen-quest-treasure)]
      (sendMessage conf (format ":crown: bestows a quest upon %s: to sojourn to the %s, face the %s, and retrieve the %s."
                                (format-target whom)
                                quest-location
                                quest-foe
                                quest-treasure)))))

(let [agree-store (atom nil)
      agreements #(or @agree-store
                      (reset! agree-store (json/read-str
                                            (slurp (resource-file-name "agree.json")))))]
  (defn agree "Yes"
    [whom & {:keys [conf] :or {conf "random"}}]
    (sendMessage conf (format ":o: agrees with %s: %s"
                              (format-target whom)
                              (rand-nth (get (agreements) "agreements")))))
  (defn disagree "Um no."
    [whom & {:keys [conf] :or {conf "random"}}]
    (let [qty (rand-nth (get (agreements) "disagree_quantities"))
          qty_of (rand-nth (get (agreements) "disagree_things"))]
      (sendMessage conf (format ":x: disagrees with %s: That is a %s of %s"
                                (format-target whom)
                                qty
                                qty_of)))))

(let [title-store (atom nil)
      titles #(or @title-store
                  (reset! title-store (json/read-str
                                        (slurp (resource-file-name "titles.json")))))]
  (defn ^:pandora appoint "Comes with full powers and responsibilities of office"
    [whom & {:keys [conf] :or {conf "random"}}]
    (let [the-title (rand-nth (get (titles) "titles"))
          the-domain (rand-nth (get (titles) "dominions"))]
      (sendMessage conf (format ":tada: appoints %s to %s of %s"
                                (format-target whom)
                                the-title
                                the-domain)))))
                              

(defn funny-list [] "Show a list of time-wasters"
  (let [funnies (ns-publics 'slackfun.funny)
        exclusions #{"funny-list"}]
    (for [[funny-name f] (filter #(not (contains? exclusions (name (get % 0)))) funnies)]
      (println (format "%s ==> %s" (name funny-name) (:doc (meta f)))))))

(defn pandora "Let me out of the box" [whom & {:keys [conf] :or {conf "random"}}]
  (let [pandora-list (map #(get % 0) (filter #(:pandora (meta (get % 1))) (ns-publics 'slackfun.funny)))]
    ((resolve (rand-nth pandora-list)) whom :conf conf)))
