(ns kp.tech-check.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::gis-loading-state
 (fn [db]
   (:gis-data-state db)))

(re-frame/reg-sub
 ::gis-features
 (fn [db]
   (:features-of-interest db)))

(re-frame/reg-sub
 ::selected-feature
 (fn [db] (:selected-feature db)))

(re-frame/reg-sub
 ::feature-effects
 (fn [db] (keys (:features-by-effect db))))

(re-frame/reg-sub
 ::feature-types
 (fn [db] (keys (:features-by-type db))))

(re-frame/reg-sub
 ::bounds
 (fn [db] (:bounds db)))