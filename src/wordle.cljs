(ns wordle
  (:require [squint-cljs/string.js :as str]))

(println "=== Play Wordle (println) ===")
;; (println "=== squint-cljs/string.js ===" str)

(def board-state (atom []))
(def counter (atom 0))
(def attempt (atom 0))
(def word-of-the-day (atom "hello"))

;; (println "=== board-state ===" board-state)
;; (js/console.log "=== board-state ===" board-state)

(defn write-letter [cell letter]
  (set! (.-textContent cell) letter))

(defn make-cell []
  ;; (println "make a cell")
  (let [cell (js/document.createElement "div")]
    (set! (.-className cell) "cell")
    cell))

(defn make-board [n]
  (println "make a board of" n "cells")
  (let [board (js/document.createElement "div")]
    (set! (.-className board) "board")
    ;; adding cells
    ;; (println "=== range ===" (range n))
    (doseq [i (range n)]
      (println "iteration" i))

    ;; (let [cell (make-cell)]
    ;;   (.appendChild board cell))
    
    ;; (range n) returns a lazy evaluator, but it seems not to work. At least
    ;; it's not working here (maybe because it's inside doseq?).
    ;; As a workaround, I force the evaluation of the lazy evaluator with
    ;; str/join, then I re-split the string.
    (doseq [_ (str/split (str/join "-" (range n)) "-")]
      (let [cell (make-cell)]
        ;; (println "board-state before swap!" board-state)
        (swap! board-state conj cell)
        ;; (println "board-state after swap!" board-state)
        (.appendChild board cell)))
    
      board))

(defn get-letter [cell]
  (.-textContent cell))

(defn color-cell [idx cell]
  (let [color (fn [el color]
                (set! (-> el .-style .-backgroundColor)
                      color))
        letter (get-letter cell)]
    (cond
      (= letter (nth @word-of-the-day idx))
      (color cell "green")

      (contains? (set @word-of-the-day) letter)
      (color cell "aqua")

      :else
      (color cell "#333333"))))

(defn check-solution [cells]
  (doseq [[idx cell] (map-indexed vector cells)]
    (color-cell idx cell))
  (= (apply str (mapv get-letter cells))
     (str @word-of-the-day)))

(defn user-input [key]
  (let [start (* 5 @attempt)
        end (* 5 (inc @attempt))]
    (cond
      (and (re-matches #"[a-z]" key)
           (< @counter end))
      (do
        (write-letter (nth @board-state @counter) key)
        (swap! counter inc))

      (and (= key "backspace")
           (> @counter start))
      (do
        (swap! counter dec)
        (write-letter (nth @board-state @counter) ""))

      (and (= key "enter")
           (= @counter end))
      (do
        (when (check-solution (subvec @board-state start end))
          (js/alert "You won"))
        (swap! attempt inc)))))

(defonce listener (atom nil))

(defn ^:dev/before-load unmount []
  (println "unmount the app component")
  (js/document.removeEventListener "keydown" @listener)
  (let [app (js/document.getElementById "app")]
    (set! (.-innerHTML app) "")))

(defn mount []
  (println "mount the app component")
  (let [app (js/document.getElementById "app")
        board (make-board 30)
        input-listener
        (fn [e]
          (user-input (.toLowerCase (.-key e))))]
    (.appendChild app board)
    (reset! listener input-listener)
    (js/document.addEventListener
     "keydown"
     input-listener)))

(mount)

(comment
  (do
    (def sim ["a" "a" "a" "a" "a" "enter"
              "e" "h" "o" "l" "o"
              "backspace" "k" "enter"
              "h" "e" "l" "l" "o" "enter"])
    (run! user-input sim)))

;; :thanks 👾