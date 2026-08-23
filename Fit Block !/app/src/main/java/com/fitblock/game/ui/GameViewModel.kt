package com.fitblock.game.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitblock.game.board.BoardData
import com.fitblock.game.board.PlacementValidator
import com.fitblock.game.core.*
import com.fitblock.game.data.SaveManager
import com.fitblock.game.ui.theme.FitBlockColors
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val saveManager = SaveManager(application)

    var gameState by mutableStateOf(GameState.MainMenu)
    var board by mutableStateOf(BoardData())
    var tray by mutableStateOf<List<PieceDefinition?>>(emptyList())
    var score by mutableStateOf(0)
    var bestScore by mutableStateOf(0)
    var coins by mutableStateOf(0)
    var gems by mutableStateOf(0)
    var combo by mutableStateOf(0)
    var maxCombo by mutableStateOf(0)
    var showNewBest by mutableStateOf(false)

    var draggingIndex by mutableStateOf<Int?>(null)
    var dragPreviewPos by mutableStateOf<Pair<Int,Int>?>(null)
    var isValidPreview by mutableStateOf(false)
    var lastCleared by mutableStateOf<ClearResult?>(null)

    var musicEnabled by mutableStateOf(true)
    var sfxEnabled by mutableStateOf(true)
    var vibrationEnabled by mutableStateOf(true)

    init {
        viewModelScope.launch {
            val saved = saveManager.load()
            bestScore = saved.bestScore
            coins = saved.coins
            gems = saved.gems
            musicEnabled = saved.musicEnabled
            sfxEnabled = saved.sfxEnabled
            vibrationEnabled = saved.vibrationEnabled
        }
    }

    fun startGame() {
        board = BoardData()
        score = 0
        combo = 0
        maxCombo = 0
        showNewBest = false
        lastCleared = null
        tray = PieceProvider.getThreePieces()
        gameState = GameState.Playing
    }

    fun pause() { gameState = GameState.Paused }
    fun resume() { gameState = GameState.Playing }
    fun goToMenu() {
        saveProgress()
        gameState = GameState.MainMenu
    }

    fun toggleMusic() { musicEnabled =!musicEnabled; saveProgress() }
    fun toggleSfx() { sfxEnabled =!sfxEnabled; saveProgress() }
    fun toggleVibration() { vibrationEnabled =!vibrationEnabled; saveProgress() }

    fun onDragStart(index: Int) {
        draggingIndex = index
        gameState = GameState.Dragging
    }

    fun onDragMove(x: Int, y: Int) {
        dragPreviewPos = x to y
        val piece = tray.getOrNull(draggingIndex?: -1)
        isValidPreview = if (piece!= null) PlacementValidator.canPlace(board, piece, x, y) else false
    }

    fun onDragEnd(x: Int?, y: Int?) {
        val idx = draggingIndex
        val piece = if (idx!= null) tray.getOrNull(idx) else null
        if (idx!= null && piece!= null && x!= null && y!= null && PlacementValidator.canPlace(board, piece, x, y)) {
            // Place piece
            val newBoard = board.clone()
            val color = getColorForPiece(piece)
            for (c in piece.cells) {
                newBoard.set(x + c.x, y + c.y, BoardCell(occupied = true, blockId = piece.id, color = color))
            }
            board = newBoard
            score += piece.cellCount * GameBalance.scorePerCell

            // Check clear
            val clearResult = checkClear(newBoard)
            if (clearResult.hasClear) {
                combo++
                maxCombo = maxOf(maxCombo, combo)
                val lines = clearResult.totalLines
                score += GameBalance.getScoreForLines(lines)
                score = (score * GameBalance.getComboMultiplier(combo)).toInt()
                coins += lines * GameBalance.coinsPerLine + (if (combo>1) GameBalance.coinsPerCombo else 0)

                // Clear cells
                val clearedBoard = newBoard.clone()
                for ((cx, cy) in clearResult.cellsToClear) {
                    clearedBoard.set(cx, cy, BoardCell())
                }
                board = clearedBoard
                lastCleared = clearResult
            } else {
                combo = 0
                lastCleared = null
            }

            // Update tray
            val newTray = tray.toMutableList()
            newTray[idx] = null
            if (newTray.all { it == null }) {
                newTray.clear()
                newTray.addAll(PieceProvider.getThreePieces())
            }
            tray = newTray

            if (score > bestScore) {
                bestScore = score
                showNewBest = true
            }

            // Game over check
            if (isGameOver()) {
                gameState = GameState.GameOver
                saveProgress()
            } else {
                gameState = GameState.Playing
            }
        } else {
            gameState = GameState.Playing
        }
        draggingIndex = null
        dragPreviewPos = null
        isValidPreview = false
    }

    private fun checkClear(b: BoardData): ClearResult {
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()
        for (y in 0 until BoardData.SIZE) {
            if ((0 until BoardData.SIZE).all { x -> b.get(x, y).occupied }) rowsToClear.add(y)
        }
        for (x in 0 until BoardData.SIZE) {
            if ((0 until BoardData.SIZE).all { y -> b.get(x, y).occupied }) colsToClear.add(x)
        }
        val cells = mutableSetOf<Pair<Int,Int>>()
        for (y in rowsToClear) for (x in 0 until BoardData.SIZE) cells.add(x to y)
        for (x in colsToClear) for (y in 0 until BoardData.SIZE) cells.add(x to y)
        return ClearResult(rowsToClear, colsToClear, cells)
    }

    private fun isGameOver(): Boolean {
        for (piece in tray) {
            if (piece == null) continue
            for (y in 0 until BoardData.SIZE) {
                for (x in 0 until BoardData.SIZE) {
                    if (PlacementValidator.canPlace(board, piece, x, y)) return false
                }
            }
        }
        return true
    }

    fun getColorForPiece(piece: PieceDefinition): Color {
        return FitBlockColors.PieceColors[piece.colorIndex % FitBlockColors.PieceColors.size]
    }

    private fun saveProgress() {
        viewModelScope.launch {
            saveManager.save(SaveData(bestScore = bestScore, coins = coins, gems = gems, musicEnabled = musicEnabled, sfxEnabled = sfxEnabled, vibrationEnabled = vibrationEnabled))
        }
    }
}
