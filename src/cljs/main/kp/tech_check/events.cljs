(ns kp.tech-check.events
  (:require [clojure.set]
            [kp.tech-check.config :as config]
            [kp.tech-check.db :as db]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(defn- load-gis [url]
  (.. (js/fetch url)
      (then #(.json %))
      (then #(js->clj % {:keywordize-keys true}))
      (then #(re-frame/dispatch [::gis-loaded %]))
      (catch #(re-frame/dispatch [::gis-load-failed %]))))

(re-frame/reg-fx
 ::gis-data-loader
 load-gis)

(re-frame/reg-event-fx
 ::load-gis-data
 (fn [{db :db}]
   (if (#{:loading :loaded} (:gis-data-state db))
     {:db db}
     {:db (assoc db :gis-data-state :loading)
      ::gis-data-loader (str config/BACKEND_URL "/api/data")})))

(defn- get-feature-property [feature & path]
  (get-in feature (cons :properties path)))

(defn- get-name [feature]
  (get-feature-property feature :name))

(defn- name-and-feature [feature]
  [(get-name feature) feature])

(defn- effects-and-feature [feature]
  (map (fn [effect] [(:name effect) feature])
       (vals (get-feature-property feature :estimated-effects))))

(defn- group-features-by-effect [features]
  (persistent!
   (transduce
    (mapcat effects-and-feature)
    (fn [result [effect feature]]
      (let [features-for-effect (conj (get result effect []) feature)]
        (assoc! result
                effect
                features-for-effect)))
    (transient {})
    features)))



(defn- coordinates [feature]
  (get-in feature [:geometry :coordinates]))


(def ^:private init-bounds
  [(.-MAX_VALUE js/Number) (.-MIN_VALUE js/Number) (.-MAX_VALUE js/Number) (.-MIN_VALUE js/Number)])

(defn calculate-bounds [features]
  (let [[west east south north] (reduce
                                 (fn [[min-lng max-lng min-lat max-lat] feature]
                                   (let [[lng lat]  (coordinates feature)]
                                     [(Math/min lng min-lng)
                                      (Math/max lng max-lng)
                                      (Math/min lat min-lat)
                                      (Math/max lat max-lat)]))
                                 init-bounds features)]
    {:west west :east east :south south :north north}))


(re-frame/reg-event-db
 ::gis-loaded
 (fn [db [_ data]]
   (let [features (:features data)]
     (merge db {:gis-data-state :loaded
                :all-features features
                :features-of-interest features
                :bounds (calculate-bounds features)
                :feature-by-name (into {} (map name-and-feature) features)
                :features-by-type (group-by #(get-feature-property % :type) features)
                :features-by-effect (group-features-by-effect features)}))))

(re-frame/reg-event-db
 ::feature-selected
 (fn [db [_ name]]
   (assoc db :selected-feature (get-in db [:feature-by-name name]))))

(defn update-filter [filters filter-type filter-value]
  (if (or (empty? filter-value) (nil? filter-value))
    (dissoc filters filter-type)
    (assoc filters filter-type filter-value)))

(def empty-sorted-set-by-property-name (sorted-set-by (fn [a b] (compare (get-name a) (get-name b)))))

(defn apply-filters [db filters]
  (reduce clojure.set/intersection
          (map (fn [[filter-type filter-value]]
                 (into empty-sorted-set-by-property-name
                       (get-in db [filter-type filter-value])))
               filters)))

(re-frame/reg-event-db
 ::apply-filter
 (fn [db [_ filter-type filter-value]]
   (let [filter-type (keyword filter-type)
         db (update db :applied-filters update-filter filter-type filter-value)
         applied-filters (:applied-filters db)]
     (if (empty? applied-filters)
       (assoc db :features-of-interest (:all-features db))
       (assoc db :features-of-interest (apply-filters db applied-filters))))))

(defn select-feature [name]
  (re-frame/dispatch [::feature-selected name]))

