(ns slackfun.core)

(require '[clj-http.client :as client])
(require '[cemerick.url :refer (url url-encode url-decode)])
(require '[gniazdo.core :as ws])
(require '[clojure.data.json :as json])

(def SLACK_URL "https://slack.com/api/")
(def RTM_START "rtm.start")
(def CHUCK_URL "http://api.icndb.com/jokes/random")

(def message-id-counter (atom 0N))
(def slack-conn (atom nil))
(def slack-info (atom nil))
(def slack-greetings (atom nil))

(defn next-message-id []
  (swap! message-id-counter inc))

(defn resource-file-name [filename]
  (url-decode (.getPath (clojure.java.io/resource filename))))

(defn greetings []
  (or @slack-greetings (reset! slack-greetings (json/read-str (slurp (resource-file-name "greetings.json"))))))

(defn get-conference-id [conf-name]
  (:id (first (filter #(= conf-name (:name %1)) (:channels (:body @slack-info))))))

(defn format-dune-quote [quote]
  (clojure.string/join "\n" (map #(format "> %s" %1) (clojure.string/split-lines quote))))

(defn rtmStart [token]
  (client/get (str (url SLACK_URL RTM_START)) {:query-params {"token" token} :as :json}))
  
(defn startRealtime [startResponse]
  (ws/connect (:url (:body startResponse))))

(defn get-slack-token []
  (clojure.string/trim (slurp (str (System/getProperty "user.home") "/.slack/token"))))

(defn slack-connect []
  (startRealtime (reset! slack-info (rtmStart (get-slack-token)))))

(defn get-slack-conn []
  (or @slack-conn (reset! slack-conn (slack-connect))))

(defn sendMessage [conf message]
  (let [message-id (next-message-id)]
    (ws/send-msg (get-slack-conn) (json/write-str {:id message-id :type "message" :channel (get-conference-id conf) :text message}))))

(defn create-funny-txt [input-file-name message-format]
  (let [quote-store (atom nil)
        get-quotes #(or @quote-store (reset! quote-store
                                             (clojure.string/split-lines
                                               (slurp
                                                 (resource-file-name input-file-name)))))]
    (fn [whom & {:keys [conf] :or {conf "random"}}]
      (sendMessage conf (format message-format whom (rand-nth (get-quotes)))))))

(defn create-funny-json [input-file-name message-format]
  (let [quote-store (atom nil)
        get-quotes #(or @quote-store (reset! quote-store
                                             (json/read-str
                                               (slurp (resource-file-name input-file-name)))))]
    (fn [whom & {:keys [conf] :or {conf "random"}}]
      (sendMessage conf (format message-format whom (format-dune-quote
                                                      (rand-nth
                                                        (get-quotes))))))))

(defn get-chuck-fact []
  (:joke (:value (:body (client/get CHUCK_URL {:as :json})))))

(def slap (create-funny-txt
            "trout.txt"
            ":fish: slaps <@%s> with a %s"))

(defn chuck [whom & {:keys [conf] :or {conf "random"}}]
  (sendMessage conf (format ":hoss: astounds <@%s> with a *FACT* about Chuck Norris:\n> %s" whom (get-chuck-fact))))

(def dune (create-funny-json
            "dune.json"
            ":wormsign: scrapes the sand off of the wisdom of Dune for <@%s>:\n%s"))

(def bofh (create-funny-txt
            "bofh.txt"
            ":troll: diagnoses <@%s>'s computer problem: %s"))

(defn hello [whom & {:keys [conf] :or {conf "random"}}]
  (let [which-language (rand-nth (keys (greetings)))]
    (sendMessage conf (format ":wave: greets <@%s> in %s: `%s`" whom which-language (rand-nth (get (greetings) which-language))))))

(defn access-book [whom & {:keys [conf] :or {conf "random"}}]
  (let [which-language (rand-nth (keys (greetings)))]
    (sendMessage conf (format ":books: thanks <@%s> with a gift of a Microsoft Access 97 book written in %s"
                              whom which-language))))

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
  (defn quest [whom & {:keys [conf] :or {conf "random"}}]
    (let [quest-location (gen-quest-location)
          quest-foe (gen-quest-foe)
          quest-treasure (gen-quest-treasure)]
      (sendMessage conf (format ":crown: bestows a quest upon <@%s>: to sojourn to the %s, face the %s, and retrieve the %s."
                                whom
                                quest-location
                                quest-foe
                                quest-treasure)))))

