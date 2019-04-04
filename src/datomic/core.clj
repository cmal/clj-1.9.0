(require '[clojure.core.async :refer (<!!)]
         '[datomic.client :as client])


(def conn
  (<!! (client/connect
        {:db-name    "hello"
         :account-id client/PRO_ACCOUNT
         :secret     "mysecret"
         :region     "none"
         :endpoint   "localhost:8998"
         :service    "peer-server"
         :access-key "myaccesskey"})))


(def movie-schema [{:db/ident       :movie/title
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The title of the movie"}

                   {:db/ident       :movie/genre
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The genre of the movie"}

                   {:db/ident       :movie/release-year
                    :db/valueType   :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The year the movie was released in theaters"}])

(<!! (client/transact conn {:tx-data movie-schema}))

(def first-movies [{:movie/title        "The Goonies"
                    :movie/genre        "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title        "Commando"
                    :movie/genre        "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title        "Repo Man"
                    :movie/genre        "punk dystopia"
                    :movie/release-year 1984}])

(def db (client/db conn))

(def all-movies-q '[:find ?e
                    :where [?e :movie/title]])

(<!! (client/q conn {:query all-movies-q :args [db]}))

(def all-titles-q '[:find ?movie-title
                    :where [_ :movie/title ?movie-title]])

(<!! (client/q conn {:query all-titles-q :args [db]}))

(def titles-from-1985 '[:find ?title
                        :where [?e :movie/title ?title]
                        [?e :movie/release-year 1985]])

(<!! (client/q conn {:query titles-from-1985 :args [db]}))

(def all-data-from-1985 '[:find ?title ?year ?genre
                          :where [?e :movie/title ?title]
                          [?e :movie/release-year ?year]
                          [?e :movie/genre ?genre]
                          [?e :movie/release-year 1985]])

(<!! (client/q conn {:query all-data-from-1985 :args [db]}))

(<!! (client/q conn {:query '[:find ?e
                              :where [?e :movie/title "Commando"]]
                     :args  [db]}))

(def commando-id 
  (ffirst (<!! (client/q conn {:query '[:find ?e 
                                        :where [?e :movie/title "Commando"]] 
                               :args  [db]}))))

(<!! (client/transact conn {:tx-data [{:db/id commando-id :movie/genre "future governor"}]}))

(<!! (client/q conn {:query all-data-from-1985 :args [db]}))

(def hdb (client/history db))




(defn make-idents
  [x]
  (mapv #(hash-map :db/ident %) x))

(def sizes [:small :medium :large :xlarge])

(make-idents sizes)

(def types [:shirt :pants :dress :hat])

(def colors [:red :green :blue :yellow])

(<!! (client/transact conn {:tx-data (make-idents sizes)}))

(<!! (client/transact conn {:tx-data (make-idents types)}))


(def schema-1
  [{:db/ident       :inv/sku
    :db/valueType   :db.type/string
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident       :inv/color
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident       :inv/size
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident       :inv/type
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}])

(<!! (client/transact conn {:tx-data schema-1}))

(def sample-data
  (->> (for [color colors size sizes type types]
         {:inv/color color
          :inv/size  size
          :inv/type  type})
       (map-indexed
        (fn [idx map]
          (assoc map :inv/sku (str "SKU-" idx))))
       vec))

(<!! (client/transact conn {:tx-data sample-data}))


