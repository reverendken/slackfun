(ns slackfun.core
  (:require [clj-http.client :as client]
            [cemerick.url :refer (url url-encode url-decode)]
            [gniazdo.core :as ws]
            [clojure.data.json :as json]))

(def SLACK_URL "https://slack.com/api/")
(def RTM_START "rtm.start")
(def POST_MESSAGE "chat.postMessage")

(def slack-conn (atom nil))
(def slack-info (atom nil))

(let [message-id-counter (atom 0N)]
  (defn next-message-id []
    (swap! message-id-counter inc)))

(defn resource-file-name [filename]
  (url-decode (.getPath (clojure.java.io/resource filename))))

(defn get-conference-id [conf-name]
  (:id (first (filter #(= conf-name (:name %1)) (:channels (:body @slack-info))))))

(defn rtmStart [token]
  (client/get (str (url SLACK_URL RTM_START)) {:query-params {"token" token} :as :json}))
 
(let [callback-fns (atom [])]
  (defn startRealtime [startResponse]
    (let [rt-callback #(doseq [f @callback-fns] (f %))]
      (ws/connect (:url (:body startResponse)) :on-receive #(rt-callback (json/read-str % :key-fn keyword)))))
  (defn add-realtime-callback [f]
    (swap! callback-fns conj f)))

(defn get-slack-token []
  (clojure.string/trim (slurp (str (System/getProperty "user.home") "/.slack/token"))))

(defn get-user-name [user-id]
  (:name (first (filter #(= user-id (:id %)) (:users (:body @slack-info))))))

(defn slack-connect []
  (startRealtime (reset! slack-info (rtmStart (get-slack-token)))))

(defn get-slack-conn []
  (or @slack-conn (reset! slack-conn (slack-connect))))

(defn is-real-user? [user-name]
  (do
    (get-slack-conn)
    (not (nil? (first
                 (filter #(= user-name (:name %)) (:users (:body @slack-info))))))))

(defn sendMessage [conf message]
  (let [message-id (next-message-id)]
    (ws/send-msg (get-slack-conn) (json/write-str {:id message-id :type "message" :channel (get-conference-id conf) :text message}))))

(defn send-message-post [channel message user-id emoji]
  (client/post (str (url SLACK_URL POST_MESSAGE))
               {:query-params {"token" (get-slack-token)
                               "channel" channel
                               "text" message
                               "username" user-id
                               "as_user" false
                               "icon_emoji" emoji}}))
