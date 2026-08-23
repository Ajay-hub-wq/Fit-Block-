package com.fitblock.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitblock.game.board.BoardData
import com.fitblock.game.core.PieceDefinition
import com.fitblock.game.ui.GameViewModel
import com.fitblock.game.ui.theme.FitBlockColors

@Composable
fun FitBlockGameRoot(viewModel: GameViewModel) {
    Box(Modifier.fillMaxSize().background(FitBlockColors.BackgroundGradient)) {
        when(viewModel.gameState) {
            com.fitblock.game.core.GameState.MainMenu -> MainMenuScreen(viewModel)
            else -> GamePlayScreen(viewModel)
        }
        if(viewModel.draggedPiece!=null) {
            FloatingPiece(viewModel)
        }
    }
}

@Composable
fun FloatingPiece(vm: GameViewModel) {
    val piece = vm.draggedPiece?: return
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Box(Modifier.offset(x = with(LocalDensity.current){ vm.dragOffset.x.toDp() - 35.dp }, y = with(LocalDensity.current){ vm.dragOffset.y.toDp() - 35.dp })
           .scale(1.3f).background(Color.Black.copy(alpha=0.3f), RoundedCornerShape(12.dp)).padding(8.dp)
        ) {
            PieceMiniView(piece = piece, color = vm.getColorForPiece(piece), cellSize = 20.dp)
        }
    }
}

@Composable
fun PieceMiniView(piece: PieceDefinition, color: Color, modifier: Modifier = Modifier, cellSize: androidx.compose.ui.unit.Dp = 14.dp) {
    Column(modifier) {
        for(y in 0 until piece.height) {
            Row { for(x in 0 until piece.width) {
                val has = piece.cells.any { it.x==x && it.y==y }
                if(has) Box(Modifier.size(cellSize).padding(1.dp).background(color, RoundedCornerShape(4.dp)))
                else Box(Modifier.size(cellSize))
            }}
        }
    }
}

@Composable
fun GamePlayScreen(vm: GameViewModel) {
    var boardBounds by remember { mutableStateOf(Rect.Zero) }
    var boardCellSize by remember { mutableStateOf(0f) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            TopHud(vm)
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${vm.score}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                if(vm.combo>1) {
                    val infinite by rememberInfiniteTransition(label="combo").animateFloat(1f,1.15f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label="comboScale")
                    Text("COMBO x${vm.combo}", color = FitBlockColors.ComboColors[(vm.combo-2)%3], fontWeight = FontWeight.Bold, modifier = Modifier.scale(infinite).background(FitBlockColors.ComboColors[(vm.combo-2)%3].copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(horizontal=10.dp, vertical=2.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Box(Modifier
                   .aspectRatio(1f).fillMaxWidth(0.92f)
                   .onGloballyPositioned {
                        val b = it.boundsInWindow()
                        boardBounds = b
                        boardCellSize = b.width / BoardData.SIZE
                    }
                   .background(FitBlockColors.BoardSurface, RoundedCornerShape(20.dp))
                   .border(2.dp, Color.White.copy(alpha=0.1f), RoundedCornerShape(20.dp))
                   .padding(10.dp)
                ) {
                    BoardViewWithPreview(vm, boardBounds, boardCellSize)
                }
            }
            Spacer(Modifier.height(12.dp))
            // REAL SWIPE TRAY
            Row(Modifier.fillMaxWidth().height(110.dp).background(FitBlockColors.BoardSurface.copy(alpha=0.8f), RoundedCornerShape(20.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                vm.tray.forEachIndexed { index, piece ->
                    if(piece==null) Box(Modifier.weight(1f))
                    else {
                        val isDragging = vm.draggingIndex==index
                        Box(Modifier.weight(1f).fillMaxHeight()
                           .clip(RoundedCornerShape(12.dp))
                           .background(if(isDragging) Color.White.copy(alpha=0.1f) else Color.Transparent)
                           .pointerInput(index) {
                                detectDragGestures(
                                    onDragStart = { offset -> vm.startDrag(index, Offset(boardBounds.left + 50f, boardBounds.top - 100f)) },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val pos = change.position
                                        // convert to window pos
                                        val windowPos = Offset(boardBounds.left + pos.x, 300f + pos.y)
                                        // calc board pos
                                        var bPos: Pair<Int,Int>? = null
                                        if(boardBounds.contains(Offset(change.position.x + 0f, change.position.y + 400f))) {
                                            // will be updated via board bounds check in VM
                                        }
                                        // estimate board pos from drag
                                        val boardX = ((windowPos.x - boardBounds.left) / boardCellSize).toInt() - 1
                                        val boardY = ((windowPos.y - boardBounds.top) / boardCellSize).toInt() - 1
                                        if(boardX in 0 until BoardData.SIZE && boardY in 0 until BoardData.SIZE) bPos = boardX to boardY
                                        vm.updateDrag(windowPos, bPos)
                                    },
                                    onDragEnd = { vm.endDrag() },
                                    onDragCancel = { vm.endDrag() }
                                )
                            }
                           .clickable { vm.quickPlace(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            PieceMiniView(piece=piece, color=vm.getColorForPiece(piece), modifier = Modifier.scale(if(isDragging) 0.5f else 1f), cellSize = 16.dp)
                        }
                    }
                }
            }
        }
        if(vm.gameState == com.fitblock.game.core.GameState.Paused) PauseOverlay(vm)
        if(vm.gameState == com.fitblock.game.core.GameState.GameOver) ResultScreen(vm)
        if(vm.showSettings) SettingsDialog(vm)
    }
}

@Composable
fun BoardViewWithPreview(vm: GameViewModel, boardBounds: Rect, cellSize: Float) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        for(y in 0 until BoardData.SIZE) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                for(x in 0 until BoardData.SIZE) {
                    val cell = vm.board.get(x,y)
                    val isPreview = vm.dragBoardPos?.let { (px,py) -> vm.draggedPiece?.cells?.any { it.x+px==x && it.y+py==y }?: false }?: false
                    val isValid = vm.isValidPreview
                    val isPlacedAnim = vm.lastPlacedCells.contains(x to y)
                    val isClearing = vm.lastCleared?.cellsToClear?.contains(x to y) == true
                    val scale by animateFloatAsState(if(isPlacedAnim) 1.2f else if(isClearing) 0f else 1f, tween(150), label="cell_$x$y")

                    val bg = when {
                        isPreview && isValid -> FitBlockColors.BoardCellHighlightValid
                        isPreview &&!isValid -> FitBlockColors.BoardCellHighlightInvalid
                        isClearing -> Color.White
                        cell.occupied -> cell.color
                        else -> FitBlockColors.BoardCellEmpty
                    }
                    Box(Modifier.weight(1f).fillMaxHeight().padding(2.5.dp).scale(scale).clip(RoundedCornerShape(8.dp)).background(bg))
                }
            }
        }
    }
}

@Composable
fun TopHud(vm: GameViewModel) {
    Row(Modifier.fillMaxWidth().background(FitBlockColors.BoardSurface.copy(alpha=0.6f), RoundedCornerShape(16.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(22.dp).background(FitBlockColors.Coin, CircleShape)); Spacer(Modifier.width(6.dp)); Text("${vm.coins}", color = Color.White, fontWeight = FontWeight.Bold) }
        Text("BEST ${vm.bestScore}", color = FitBlockColors.TextSecondary, fontSize=12.sp)
        Row {
            IconButton(onClick = { vm.showSettings = true }) { Icon(Icons.Default.Settings, contentDescription="Settings", tint=Color.White) }
            IconButton(onClick = { vm.pause() }) { Icon(Icons.Default.Pause, contentDescription="Pause", tint=Color.White) }
        }
    }
}

@Composable
fun MainMenuScreen(vm: GameViewModel) {
    val infinite = rememberInfiniteTransition(label="logo")
    val scale by infinite.animateFloat(1f,1.08f, infiniteRepeatable(tween(1000, easing=FastOutSlowInEasing), RepeatMode.Reverse), label="logoScale")
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
        Spacer(Modifier.height(40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.scale(scale).background(FitBlockColors.BoardSurface, RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(24.dp)).padding(28.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FIT", color=Color.White, fontSize=42.sp, fontWeight=FontWeight.Black, letterSpacing=3.sp)
                    Text("BLOCK", color=FitBlockColors.PieceColors[0], fontSize=42.sp, fontWeight=FontWeight.Black, letterSpacing=3.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("BEST ${vm.bestScore}", color=FitBlockColors.TextSecondary)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.startGame() }, Modifier.fillMaxWidth(0.85f).height(60.dp), shape=RoundedCornerShape(18.dp), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7C5CFF))) {
                Text("PLAY", fontSize=22.sp, fontWeight=FontWeight.Bold)
            }
            OutlinedButton(onClick = { vm.showSettings = true }, Modifier.fillMaxWidth(0.85f)) { Icon(Icons.Default.Settings, null); Spacer(Modifier.width(8.dp)); Text("SETTINGS") }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun SettingsDialog(vm: GameViewModel) {
    AlertDialog(onDismissRequest = { vm.showSettings=false }, title={ Text("Settings", fontWeight=FontWeight.Bold) },
        text={
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){ Text("Music"); Switch(checked=vm.musicEnabled, onCheckedChange={ vm.toggleMusic() }) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){ Text("SFX"); Switch(checked=vm.sfxEnabled, onCheckedChange={ vm.toggleSfx() }) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){ Text("Vibration"); Switch(checked=vm.vibrationEnabled, onCheckedChange={ vm.toggleVibration() }) }
            }
        },
        confirmButton={ TextButton(onClick={ vm.showSettings=false }){ Text("CLOSE") } }
    )
}

@Composable
fun ResultScreen(vm: GameViewModel) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.7f)), contentAlignment=Alignment.Center) {
        val scale by animateFloatAsState(0f, 1f, animationSpec=spring(dampingRatio=0.6f, stiffness=300f), label="result")
        Column(Modifier.scale(scale).background(FitBlockColors.BoardSurface, RoundedCornerShape(28.dp)).padding(28.dp).fillMaxWidth(0.85f), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text("GAME OVER", color=Color.White, fontSize=28.sp, fontWeight=FontWeight.Black)
            Text("${vm.score}", color=Color.White, fontSize=52.sp, fontWeight=FontWeight.Black)
            if(vm.showNewBest) Text("NEW BEST!", color=FitBlockColors.Coin, fontWeight=FontWeight.Bold)
            Button(onClick={ vm.startGame() }, Modifier.fillMaxWidth().height(56.dp), shape=RoundedCornerShape(16.dp)){ Text("RETRY", fontWeight=FontWeight.Bold) }
            OutlinedButton(onClick={ vm.goToMenu() }, Modifier.fillMaxWidth()){ Text("HOME") }
        }
    }
}

@Composable
fun PauseOverlay(vm: GameViewModel) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment=Alignment.Center) {
        Column(Modifier.background(FitBlockColors.BoardSurface, RoundedCornerShape(24.dp)).padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("PAUSED", color=Color.White, fontSize=24.sp, fontWeight=FontWeight.Bold)
            Button(onClick={ vm.resume() }, Modifier.fillMaxWidth()){ Text("RESUME") }
            OutlinedButton(onClick={ vm.goToMenu() }, Modifier.fillMaxWidth()){ Text("HOME") }
        }
    }
}
