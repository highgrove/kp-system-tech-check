(ns kp.tech-check.views
  (:require ["google-map-react" :as google-map]
            ["react" :as react]
            [re-frame.core :as re-frame]
            [kp.tech-check.config :refer [GOOGLE_MAPS_API_KEY]]
            [kp.tech-check.subs :as subs]
            [kp.tech-check.events :as events]
            [kp.tech-check.ui-components :as ui]))

(defn feature-properties [feature]
  (let [status (get-in feature [:properties :status])
        name (get-in feature [:properties :name])]
    [:div {:class "shadow-md shadow-gray-300 mb-5 p-2 cursor-pointer"
           :on-click #(events/select-feature name)}
     [:div
      "Namn: " name]
     [:h5  "Status: " status]]))

(defn- coordinates [feature]
  (get-in feature [:geometry :coordinates]))

(defn- feature-key [feature prefix]
  (str prefix "-" (get-in feature [:properties :name])))

(defn gis-map [features]
  (let [selected-feature @(re-frame/subscribe [::subs/selected-feature])
        bounds @(re-frame/subscribe [::subs/bounds])
        [lng lat] (or (coordinates selected-feature) [56.89399129675018 14.80511420271593]) ;; Växjö by default, just as good as any other place :)
        selected-feature-id (feature-key selected-feature "point")]
    [:> google-map
     {:bootstrapURLKeys {:key GOOGLE_MAPS_API_KEY}
      :center {:lat lat :lng lng}
      :yesIWantToUseGoogleMapApiInternals true
      :onGoogleApiLoaded (fn [maps] (.fitBounds maps.map (clj->js bounds)))
      :zoom (if selected-feature 15 10)}
     (for [feature features
           :let [key (feature-key feature "point")
                 [lng lat] (coordinates feature)
                 selected? (= selected-feature-id key)
                 transform (str "translate(-50%, -50%)" (when selected? "scale(1.4)"))]]
       ^{:key key}
       [:div {:lat lat
              :lng lng
              :id key
              :style {:background-color (if selected? "red" "green")
                      :z-index (when selected? 999)
                      :position "relative"
                      :width "10px"
                      :height "10px"
                      :border-radius "100%"
                      :transform transform}}])]))


(defn effect-filter []
  (let [feature-effects (re-frame/subscribe [::subs/feature-effects])]
    [:div
     [ui/label {:for "features-by-effect"} "Påverkan"]
     [ui/select {:id "features-by-effect"}
      [:<>
       [:option {:class "italic" :value ""} "Alla"]
       (for [feature-effect @feature-effects]
          ;; TODO:FIXME: I have no idea how to get key to be propageted via a custom component in a idomatic way 
         ^{:key feature-effect}
         [:option {:value feature-effect} feature-effect])]]]))

(defn type-filter []
  (let [feature-types (re-frame/subscribe [::subs/feature-types])]
    [:div
     [ui/label {:for "features-by-type"} "Typ"]
     [ui/select {:id "features-by-type"}
      [:<>
       [:option {:class "italic" :value ""} "Alla"]
       (for [feature-type @feature-types]
         ^{:key feature-type}
         [:option {:value feature-type} feature-type])]]]))

(defn apply-filter [e]
  (let [filter-type (.. e -target -name)
        filter-value (.. e -target -value)]
    (re-frame/dispatch [::events/apply-filter filter-type filter-value])))

(defn filter-form []
  [:form {:on-change apply-filter}
   [effect-filter]
   [:div {:class "divide-solid divide-y mt-1 mb-1"}]
   [type-filter]])

(defn features-list [features]
  [:div {:class "overflow-x-auto w-80 p-2"}
   [filter-form]
   [:div {:class "mt-4 mb-4 divide-solid divide-y"}]
   (for [feature features]
     ^{:key (feature-key feature "details")}
     [feature-properties feature])])

(defn main-panel []
  (let [loading-state (re-frame/subscribe [::subs/gis-loading-state])
        gis-features (re-frame/subscribe [::subs/gis-features])]
    [:div {:class "w-screen h-screen flex"}
     (case @loading-state
       :loaded
       [:<>
        [:div {:class "flex-1"}
         [:f> gis-map @gis-features]]
        [features-list @gis-features]]
       :loading
       [:div "laddar..."]
       :load-failed
       [:div "Något gick snett, prova igen senare."])]))

