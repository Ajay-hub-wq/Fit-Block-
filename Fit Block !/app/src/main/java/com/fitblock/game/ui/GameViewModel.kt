
package com.fitblock.game.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitblock.game.board.BoardData
import com.fitblock.game.board.LineDetector
import com.fitblock.game.board.PlacementValidator
import com.fitblock.game.core.*
import com.fitblock.game.data.SaveManager
import com.fitblock.game.pieces.PieceGenerator
import com.fitblock.game.pieces.PieceProvider
import com.fitblock.game.ui.theme.FitBlockColors
import kotlinx.coroutines.launch
import java.time.LocalDate

class GameViewModel : ViewModel() {
    var board by mutableStateOf(BoardData())
        private set
    var tray by mutableStateOf(listOf<PieceDefinition?>())
        private set
    var score by mutableStateOf(0)
    var bestScore by mutableStateOf(0)
    var coins by mutableStateOf(0)
    var gems by mutableStateOf(0)
    var combo by mutableStateOf(0)
    var maxCombo by mutableStateOf(0)
    var gameState by mutableStateOf(GameState.MainMenu)
    var showNewBest by mutableStateOf(false)
    var lastCleared by mutableStateOf<ClearResult?>(null)
    var dailyStreak by mutableStateOf(0)
    var canClaimDaily by mutableStateOf(true)
    var musicEnabled by mutableStateOf(true)
    var sfxEnabled by mutableStateOf(true)
    var vibrationEnabled by mutableStateOf(true)
    var totalGames by mutableStateOf(0)

    private var generator = PieceGenerator()
    private var saveManager: SaveManager? = null
    private var saveData: SaveData = SaveData()

    // drag state
    var draggingIndex by mutableStateOf<Int?>(null)
    var dragPreviewPos by mutableStateOf<Pair<Int,Int>?>(null)
    var isValidPreview by mutableStateOf(false)

    fun init(context: Context) {
        saveManager = SaveManager(context)
        viewModelScope.launch {
            saveData = saveManager!!.load()
            bestScore = saveData.bestScore
            coins = saveData.coins
            gems = saveData.gems
            dailyStreak = saveData.dailyStreak
            musicEnabled = saveData.musicEnabled
            sfxEnabled = saveData.sfxEnabled
            vibrationEnabled = saveData.vibrationEnabled
            totalGames = saveData.totalGamesPlayed
            // daily check
            try {
                if (saveData.lastRewardDate.isNotEmpty()) {
                    val last = java.time.LocalDate.parse(saveData.lastRewardDate)
                    val today = java.time.LocalDate.now()
                    canClaimDaily = last != today
                } else canClaimDaily = true
            } catch (e: Exception) { canClaimDaily = true }
        }
    }

    private fun save() {
        viewModelScope.launch {
            saveData = saveData.copy(
                bestScore = bestScore,
                coins = coins,
                gems = gems,
                musicEnabled = musicEnabled,
                sfxEnabled = sfxEnabled,
                vibrationEnabled = vibrationEnabled,
                dailyStreak = dailyStreak,
                totalGamesPlayed = totalGames,
                totalLinesCleared = saveData.totalLinesCleared,
                lastRewardDate = saveData.lastRewardDate,
                tutorialCompleted = true
            )
            saveManager?.save(saveData)
        }
    }

    fun startGame() {
        board = BoardData().apply { clearAll() }
        score = 0
        combo = 0
        maxCombo = 0
        showNewBest = false
        lastCleared = null
        tray = generator.generateBatch(3, board)
        gameState = GameState.Playing
    }

    fun getColorForPiece(piece: PieceDefinition): Color {
        return FitBlockColors.PieceColors[piece.colorIndex % FitBlockColors.PieceColors.size]
    }

    fun onDragStart(index: Int) {
        if (gameState != GameState.Playing) return
        draggingIndex = index
        gameState = GameState.Dragging
    }

    fun onDragMove(boardX: Int?, boardY: Int?) {
        val idx = draggingIndex ?: return
        val piece = tray[idx] ?: return
        if (boardX == null || boardY == null) {
            dragPreviewPos = null
            isValidPreview = false
            return
        }
        dragPreviewPos = boardX to boardY
        isValidPreview = PlacementValidator.canPlace(board, piece, boardX, boardY)
    }

    fun onDragEnd(boardX: Int?, boardY: Int?): Boolean {
        val idx = draggingIndex ?: return false
        val piece = tray.getOrNull(idx) ?: run { draggingIndex=null; gameState=GameState.Playing; return false }
        var placed = false
        if (boardX != null && boardY != null && PlacementValidator.canPlace(board, piece, boardX, boardY)) {
            // place
            val color = getColorForPiece(piece)
            for (off in piece.cells) {
                val x = boardX + off.x
                val y = boardY + off.y
                board.set(x, y, BoardCell(true, piece.id, color))
            }
            score += piece.cellCount * GameBalance.scorePerCell

            // clear
            val clearResult = LineDetector.detect(board)
            if (clearResult.hasClear) {
                lastCleared = clearResult
                // clear cells
                for ((x,y) in clearResult.cellsToClear) board.set(x,y, BoardCell())
                val lines = clearResult.totalLines
                val mult = GameBalance.getComboMultiplier(combo+1)
                val base = GameBalance.getScoreForLines(lines)
                val finalScore = (base * mult).toInt()
                score += finalScore
                combo += 1
                maxCombo = maxOf(maxCombo, combo)
                coins += lines * GameBalance.coinsPerLine
                if (combo > 1) coins += GameBalance.coinsPerCombo
                if (combo % 3 == 0 && combo>0) gems += 1
                // save total lines
                saveData = saveData.copy(totalLinesCleared = saveData.totalLinesCleared + lines)
            } else {
                combo = 0
                lastCleared = null
            }

            // update tray
            val mutable = tray.toMutableList()
            mutable[idx] = null
            if (mutable.all { it == null }) {
                mutable.clear()
                mutable.addAll(generator.generateBatch(3, board))
            }
            tray = mutable.filterNotNull()

            // if tray empty after filtering (all nulls replaced), we already have new batch
            if (tray.isEmpty()) tray = generator.generateBatch(3, board)

            placed = true

            // check game over
            if (PlacementValidator.isGameOver(board, tray)) {
                // game over
                if (score > bestScore) { bestScore = score; showNewBest = true }
                totalGames++
                gameState = GameState.GameOver
                save()
            } else {
                gameState = GameState.Playing
            }
        } else {
            gameState = GameState.Playing
        }
        draggingIndex = null
        dragPreviewPos = null
        isValidPreview = false
        if (placed) save()
        return placed
    }

    fun claimDaily(): Int {
        if (!canClaimDaily) return 0
        dailyStreak = (dailyStreak + 1).coerceAtMost(7)
        val isGemDay = dailyStreak % 3 == 0
        if (isGemDay) gems += 5 else coins += when(dailyStreak) { 1->50; 2->80; 3->0; 4->120; 5->0; 6->150; 7->300; else->50 }
        if (!isGemDay && dailyStreak==3) coins += 0 // handled
        if (isGemDay && dailyStreak!=3) {} // already added gems
        // For day 3,7 give gems: already handled 5, for 7 give 10
        if (dailyStreak==7) { gems += 5 } // extra
        saveData = saveData.copy(lastRewardDate = java.time.LocalDate.now().toString(), dailyStreak = dailyStreak)
        canClaimDaily = false
        save()
        return if (isGemDay) 5 else 50
    }

    fun toggleMusic() { musicEnabled = !musicEnabled; save() }
    fun toggleSfx() { sfxEnabled = !sfxEnabled; save() }
    fun toggleVibration() { vibrationEnabled = !vibrationEnabled; save() }

    fun goToMenu() { gameState = GameState.MainMenu }
    fun pause() { if (gameState==GameState.Playing) gameState = GameState.Paused }
    fun resume() { if (gameState==GameState.Paused) gameState = GameState.Playing }
}
