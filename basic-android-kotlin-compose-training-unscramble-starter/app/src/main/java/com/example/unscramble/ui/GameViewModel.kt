package com.example.unscramble.ui

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.unscramble.data.allWords
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.MAX_NO_OF_WORDS // 🔥 Ditambahkan: Import untuk konstanta batas kata

// -----------------------------------------------------------
// 1. Data Model (GameUiState) - Sudah Benar
// -----------------------------------------------------------
data class GameUiState(
    val currentScrambledWord: String = "",
    val currentWordCount: Int = 1,
    val score: Int = 0,
    val isGuessedWordWrong: Boolean = false,
    val isGameOver: Boolean = false // Sudah Benar
)

class GameViewModel : ViewModel() {

    // ... (StateFlow, userGuess, currentWord, usedWords tidak berubah)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set

    private lateinit var currentWord: String

    private var usedWords: MutableSet<String> = mutableSetOf()

    // ... (init, updateUserGuess, checkUserGuess, skipWord tidak berubah)

    init {
        resetGame()
    }

    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord

        if (guessedWord.isNotEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = false)
            }
        }
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        userGuess = ""
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        userGuess = ""
    }


    // -----------------------------------------------------------
    // 6. Metode Helper (Fungsi Bantuan)
    // -----------------------------------------------------------

    /**
     * Metode untuk memperbarui UI status game dan memeriksa apakah game berakhir.
     */
    private fun updateGameState(updatedScore: Int) {
        // 🔥 Perubahan Utama: Implementasi logika akhir game (MAX_NO_OF_WORDS)
        if (usedWords.size == MAX_NO_OF_WORDS){
            // Putaran terakhir: set isGameOver = true, jangan pilih kata baru
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // Putaran normal: Lanjutkan ke kata berikutnya
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore
                )
            }
        }
    }

    // ... (pickRandomWordAndShuffle, shuffleCurrentWord tidak berubah)

    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()

        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()

        while (String(tempWord).equals(word, ignoreCase = true)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    /**
     * Melakukan inisialisasi atau memulai ulang permainan.
     */
    fun resetGame() {
        usedWords.clear()
        // 🔥 Pastikan isGameOver direset ke false secara default di GameUiState
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }
}