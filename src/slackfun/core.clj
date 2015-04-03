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
(def slack-trouts (atom nil))
(def slack-dune-quotes (atom nil))

(defn next-message-id []
  (swap! message-id-counter inc))

(defn resource-file-name [filename]
  (url-decode (.getPath (clojure.java.io/resource filename))))

(defn trouts []
  (or @slack-trouts (reset! slack-trouts (clojure.string/split-lines (slurp (resource-file-name "trout.txt"))))))

(defn dune-quotes []
  (or @slack-dune-quotes (reset! slack-dune-quotes (json/read-str (slurp (resource-file-name "dune.json"))))))

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

(defn get-conference-id [conf-name]
  (:id (first (filter #(= conf-name (:name %1)) (:channels (:body @slack-info))))))

(defn get-chuck-fact []
  (:joke (:value (:body (client/get CHUCK_URL {:as :json})))))

(defn format-dune-quote [quote]
  (clojure.string/join "\n" (map #(format "> %s" %1) (clojure.string/split-lines quote))))

(defn sendMessage [conf message]
  (let [message-id (next-message-id)]
    (ws/send-msg (get-slack-conn) (json/write-str {:id message-id :type "message" :channel (get-conference-id conf) :text message}))))

(defn slap [whom & {:keys [conf] :or {conf "random"}}]
  (sendMessage conf (format ":fish: slaps <@%s> with a %s" whom (rand-nth (trouts)))))

(defn chuck [whom & {:keys [conf] :or {conf "random"}}]
  (sendMessage conf (format ":hoss: astounds <@%s> with a FACT about Chuck Norris:\n> %s" whom (get-chuck-fact))))

(defn dune [whom & {:keys [conf] :or {conf "random"}}]
  (sendMessage conf (format ":wormsign: scrapes the sand off of the wisdom of Dune for <@%s>:\n%s" whom (format-dune-quote (rand-nth (dune-quotes))))))