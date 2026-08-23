
package com.fitblock.game.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitblock.game.board.BoardData
import com.fitblock.game.board.BoardCell
import com.fitblock.game.board.PlacementValidator
import com.fitblock.game.core.*
import com.fitblock.game.data.SaveManager
import com.fitblock.game.data.SaveData
import com.fitblock.game.data.SoundManager
import com.fitblock.game.ui.theme.FitBlockColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val saveManager = SaveManager(application)
    private val soundManager = SoundManager(application)

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

    // DRAG STATE - THIS FIXES YOUR ERROR
    var draggingIndex by mutableStateOf<Int?>(null)
    var draggedPiece by mutableStateOf<PieceDefinition?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragBoardPos by mutableStateOf<Pair<Int,Int>?>(null)
    var isValidPreview by mutableStateOf(false)
    var lastCleared by mutableStateOf<ClearResult?>(null)
    var lastPlacedCells by mutableStateOf<Set<Pair<Int,Int>>>(emptySet())
    var showSettings by mutableStateOf(false)

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
        lastPlacedCells = emptySet()
        tray = com.fitblock.game.pieces.PieceProvider.getThreePieces()
        gameState = GameState.Playing
    }

    fun pause() { gameState = GameState.Paused }
    fun resume() { gameState = GameState.Playing }
    fun goToMenu() { saveProgress(); gameState = GameState.MainMenu }
    fun toggleMusic() { musicEnabled = !musicEnabled; saveProgress() }
    fun toggleSfx() { sfxEnabled = !sfxEnabled; saveProgress() }
    fun toggleVibration() { vibrationEnabled = !vibrationEnabled; saveProgress() }

    fun startDrag(index: Int, offset: Offset) {
        draggingIndex = index
        draggedPiece = tray.getOrNull(index)
        dragOffset = offset
        gameState = GameState.Dragging
        soundManager.playClick(sfxEnabled)
    }

    fun updateDrag(offset: Offset, boardPos: Pair<Int,Int>?) {
        dragOffset = offset
        dragBoardPos = boardPos
        val piece = draggedPiece
        isValidPreview = if(piece!=null && boardPos!=null) PlacementValidator.canPlace(board, piece, boardPos.first, boardPos.second) else false
    }

    fun endDrag() {
        val idx = draggingIndex
        val piece = draggedPiece
        val pos = dragBoardPos
        if(idx!=null && piece!=null && pos!=null && PlacementValidator.canPlace(board, piece, pos.first, pos.second)) {
            placePiece(piece, pos.first, pos.second, idx)
        }
        draggingIndex = null
        draggedPiece = null
        dragBoardPos = null
        isValidPreview = false
        if(gameState == GameState.Dragging) gameState = GameState.Playing
    }

    fun quickPlace(index: Int) {
        val piece = tray.getOrNull(index) ?: return
        for(y in 0 until BoardData.SIZE) {
            for(x in 0 until BoardData.SIZE) {
                if(PlacementValidator.canPlace(board, piece, x, y)) {
                    placePiece(piece, x, y, index)
                    return
                }
            }
        }
    }

    private fun placePiece(piece: PieceDefinition, x: Int, y: Int, idx: Int) {
        val newBoard = board.clone()
        val color = getColorForPiece(piece)
        val placed = mutableSetOf<Pair<Int,Int>>()
        for(c in piece.cells) {
            newBoard.set(x+c.x, y+c.y, BoardCell(true, piece.id, color))
            placed.add(x+c.x to y+c.y)
        }
        board = newBoard
        lastPlacedCells = placed
        score += piece.cellCount * 10
        soundManager.playPlace(sfxEnabled)

        viewModelScope.launch {
            delay(120)
            val rows = mutableListOf<Int>()
            val cols = mutableListOf<Int>()
            for(yy in 0 until BoardData.SIZE) if((0 until BoardData.SIZE).all { xx -> newBoard.get(xx,yy).occupied }) rows.add(yy)
            for(xx in 0 until BoardData.SIZE) if((0 until BoardData.SIZE).all { yy -> newBoard.get(xx,yy).occupied }) cols.add(xx)
            val cells = mutableSetOf<Pair<Int,Int>>()
            for(yy in rows) for(xx in 0 until BoardData.SIZE) cells.add(xx to yy)
            for(xx in cols) for(yy in 0 until BoardData.SIZE) cells.add(xx to yy)
            if(cells.isNotEmpty()) {
                combo++
                maxCombo = maxOf(maxCombo, combo)
                score += GameBalance.getScoreForLines(rows.size+cols.size)
                score = (score * GameBalance.getComboMultiplier(combo)).toInt()
                coins += (rows.size+cols.size) * 2
                lastCleared = ClearResult(rows, cols, cells)
                soundManager.playClear(rows.size+cols.size, sfxEnabled)
                delay(300)
                val cleared = newBoard.clone()
                for((cx,cy) in cells) cleared.set(cx,cy, BoardCell())
                board = cleared
                lastCleared = null
            } else {
                combo = 0
                lastCleared = null
            }
            lastPlacedCells = emptySet()
            val newTray = tray.toMutableList()
            newTray[idx]=null
            if(newTray.all { it==null }) {
                newTray.clear()
                newTray.addAll(com.fitblock.game.pieces.PieceProvider.getThreePieces())
            }
            tray = newTray
            if(score>bestScore) { bestScore=score; showNewBest=true }
            if(isGameOver()) { gameState = GameState.GameOver; soundManager.playGameOver(sfxEnabled); saveProgress() }
        }
    }

    private fun isGameOver(): Boolean {
        for(p in tray) {
            if(p==null) continue
            for(y in 0 until BoardData.SIZE) for(x in 0 until BoardData.SIZE) if(PlacementValidator.canPlace(board,p,x,y)) return false
        }
        return true
    }

    fun getColorForPiece(piece: PieceDefinition): Color = FitBlockColors.PieceColors[piece.colorIndex % FitBlockColors.PieceColors.size]

    private fun saveProgress() {
        viewModelScope.launch {
            saveManager.save(SaveData(bestScore=bestScore, coins=coins, gems=gems, musicEnabled=musicEnabled, sfxEnabled=sfxEnabled, vibrationEnabled=vibrationEnabled))
        }
    }
}
