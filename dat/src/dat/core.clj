(ns dat.core
  (:require [clojure.spec :as s :refer [fdef]]
            [clojure.spec.test :as stest]))

(def good-numbers [0.21 1/7 42])
(def not-so-good-numbers [0.21 1/7 42 :keyword "str"])

(s/def ::num (s/or :float float?
                   ;; :int   integer?
                   :ratio ratio?))
;; => :dat.core/num
(s/def ::even-int (s/and integer? even?))
;; => :dat.core/even-int

(map #(s/valid? ::num %) not-so-good-numbers)
;; => (true true true false false)
(map #(s/valid? ::even-int %) not-so-good-numbers)
;; => (false false true false false)

(s/conform (s/coll-of ::num) good-numbers)
;; => [[:float 0.21] [:ratio 1/7] [:int 42]]
(s/conform (s/coll-of ::num) not-so-good-numbers)
;; => :clojure.spec/invalid

(s/explain-data (s/coll-of ::num) not-so-good-numbers)

;; {:clojure.spec/problems
;;  ({:path [:float],
;;    :pred float?,
;;    :val :keyword,
;;    :via [:dat.core/num],
;;    :in [3]}
;;   {:path [:int],
;;    :pred integer?,
;;    :val :keyword,
;;    :via [:dat.core/num],
;;    :in [3]}
;;   {:path [:ratio],
;;    :pred ratio?,
;;    :val :keyword,
;;    :via [:dat.core/num],
;;    :in [3]}
;;   {:path [:float],
;;    :pred float?,
;;    :val "str",
;;    :via [:dat.core/num],
;;    :in [4]}
;;   {:path [:int],
;;    :pred integer?,
;;    :val "str",
;;    :via [:dat.core/num],
;;    :in [4]}
;;   {:path [:ratio],
;;    :pred ratio?,
;;    :val "str",
;;    :via [:dat.core/num],
;;    :in [4]})}

(s/explain ::even-int 0.21)
;; => "val: 0.21 fails spec: :dat.core/even-int predicate: integer?\n"

(s/explain ::even-int 5)
;; => "val: 5 fails spec: :dat.core/even-int predicate: even?\n"

(s/exercise ::num)
;; ([-2.0 [:float -2.0]]
;;  [-1 [:int -1]]
;;  [-2 [:int -2]]
;;  [1/2 [:ratio 1/2]]
;;  [1.0 [:float 1.0]]
;;  [1/2 [:ratio 1/2]]
;;  [8 [:int 8]]
;;  [-1 [:int -1]]
;;  [-5 [:int -5]]
;;  [7/4 [:ratio 7/4]])

(s/exercise ::even-int)
;; ([0 0]
;;  [0 0]
;;  [-4 -4]
;;  [-2 -2]
;;  [0 0]
;;  [-4 -4]
;;  [-2 -2]
;;  [0 0]
;;  [-508 -508]
;;  [1548 1548])

(defn simple-sum
  "Just a sum function."
  [a b]
  (+ a b))



(s/fdef simple-sum
        :args (s/cat :a ::num :b ::num)
        :ret ::num)

(s/exercise-fn `simple-sum)
;; ([(-2.0 -2.0) -4.0]
;;  [(2.0 0) 2.0]
;;  [(-2 -0.5) -2.5]
;;  [(-2 Infinity) Infinity]
;;  [(4/5 -0.625) 0.17500000000000004]
;;  [(-1 -0.5) -1.5]
;;  [(-0.5 -3/7) -0.9285714285714286]
;;  [(1/3 -1) -2/3]
;;  [(-1.0 9) 8.0]
;;  [(1.1953125 -1) 0.1953125])


;; =============================================================================

(def credentials
  {:admin   {:email "admin@test.io" :password "test#secret"}
   :manager {:email "manager@test.io" :password "test#secret"}
   :worker  {:email "worker@test.io" :password "test#secret"}
   :user    {:email "user@test.io" :password "test#secret"}})

(s/def ::user-type (-> credentials keys set))

(s/def ::token (s/and string?
                      #(< 6 (count %))))

(s/def ::real-token (s/and string?
                           #(< 6 (count %))
                           #(= "Token " (subs % 0 6))))

(s/def ::token-map (s/keys :req-un [::real-token]))

;; (clojure.pprint/pprint (s/exercise ::token-map))


(defn get-token-for
  "Simple docstring here"
  [user-type]
  ;; somehow generate a token
  (case user-type
    :admin {:token "admin token"}
    :user {:token "user token"}
    {:token "Token default token"}))


;; (get-token-for :admin)
;; (get-token-for 2)


;; (print "=======================================================\n")
;; (println (:doc (meta #'get-token-for)))

;; (print "=======================================================\n")
;; (alter-meta! #'get-token-for (fn [m] (update-in m [:doc] #(str % "\n\n" "some additional info"))))




(fdef get-token-for
      :args (s/cat :user-type ::user-type)
      :ret ::token-map)

;; (print "=======================================================\n")
;; (clojure.pprint/pprint (s/exercise-fn `get-token-for))

;; ([(:admin) {:token "admin token"}]
;;  [(:user) {:token "user token"}]
;;  [(:user) {:token "user token"}]
;;  [(:user) {:token "user token"}]
;;  [(:admin) {:token "admin token"}]
;;  [(:admin) {:token "admin token"}]
;;  [(:admin) {:token "admin token"}]
;;  [(:manager) {:token "Token default token"}]
;;  [(:user) {:token "user token"}]
;;  [(:user) {:token "user token"}])

(def test-result
  (-> (stest/check `get-token-for)
      stest/summarize-results
      with-out-str
      read-string))

;; (clojure.pprint/pprint (stest/check `get-token-for))
;; => ({:spec #object[clojure.spec$fspec_impl$reify__21618 0x77325b7f "clojure.spec$fspec_impl$reify__21618@77325b7f"], :clojure.spec.test.check/ret {:result true, :num-tests 1000, :seed 1493656956122}, :sym dat.core/get-token-for})

;; (clojure.pprint/pprint test-result)
;; (when (:failure test-result) (println "test failed"))

;; (clojure.pprint/pprint (:failure test-result))
