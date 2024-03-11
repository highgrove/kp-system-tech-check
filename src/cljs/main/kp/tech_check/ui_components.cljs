(ns kp.tech-check.ui-components)

(defn label [{for :for class :class :or {for "" class ""}} children]
  [:label (merge
           {:class (str "block  text-sm font-medium leading-6 text-gray-900" class)
            :for for})
   children])

(defn select [{id :id name :name :or {name id}} children]
  [:select {:class "w-full block rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:max-w-xs sm:text-sm sm:leading-6"
            :name name
            :id id}
   children])