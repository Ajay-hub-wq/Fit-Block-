
package com.fitblock.game.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fitblock.game.board.BoardData
import com.fitblock.game.core.BoardCell
import com.fitblock.game.core.ClearResult
import com.fitblock.game.core.GameState
import com.fitblock.game.core.PieceDefinition
import com.fitblock.game.ui.GameViewModel
import com.fitblock.game.ui.theme.FitBlockColors
import kotlin.math.roundToInt

@Composable
fun FitBlockGameRoot(viewModel: GameViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(FitBlockColors.Background)) {
        when(viewModel.gameState) {
            GameState.MainMenu -> MainMenuScreen(viewModel)
            GameState.Playing, GameState.Dragging, GameState.Clearing, GameState.Paused -> GamePlayScreen(viewModel)
            GameState.GameOver, GameState.Result -> ResultScreen(viewModel)
            else -> MainMenuScreen(viewModel)
        }
        if (viewModel.gameState == GameState.Paused) {
            PauseOverlay(viewModel)
        }
    }
}

@Composable
fun MainMenuScreen(vm: GameViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.05f, animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOut), RepeatMode.Reverse), label="scale")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
        Spacer(Modifier.height(32.dp))
        // Logo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale).background(FitBlockColors.BoardSurface, RoundedCornerShape(24.dp)).padding(24.dp)) {
                Text("FIT BLOCK", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black, color = Color.White), letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("BEST ${vm.bestScore}", color = FitBlockColors.TextSecondary, style = MaterialTheme.typography.titleMedium)
        }

        // Board preview animation
        Box(modifier = Modifier.size(200.dp).background(FitBlockColors.BoardSurface, RoundedCornerShape(16.dp)).padding(8.dp)) {
            // simple 8x8 preview grid with some blocks
            Column { repeat(8) { y-> Row { repeat(8) { x-> Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp).background(if ((x+y)%3==0) FitBlockColors.PieceColors[(x+y)%7].copy(alpha=0.8f) else FitBlockColors.BoardCellEmpty, RoundedCornerShape(4.dp))) } } }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { vm.startGame() }, modifier = Modifier.fillMaxWidth(0.8f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = FitBlockColors.PieceColors[1])) {
                Text("PLAY", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { /* daily challenge */ }, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.EmojiEvents, null); Spacer(Modifier.width(8.dp)); Text("DAILY") }
                OutlinedButton(onClick = { /* settings */ }, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Settings, null); Spacer(Modifier.width(8.dp)); Text("SETTINGS") }
            }
            Text("Offline • No ads interrupt gameplay", color = FitBlockColors.TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun GamePlayScreen(vm: GameViewModel) {
    var boardSizePx by remember { mutableStateOf(0) }
    var boardTopLeft by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.fillMaxSize().background(FitBlockColors.Background).padding(horizontal = 12.dp, vertical = 8.dp).systemBarsPadding()) {
        // Top HUD
        TopHud(vm)
        Spacer(Modifier.height(12.dp))

        // Score
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${vm.score}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                if (vm.combo > 1) {
                    val comboColor = FitBlockColors.ComboColors[(vm.combo-2) % FitBlockColors.ComboColors.size]
                    Text("COMBO x${vm.combo}", color = comboColor, fontWeight = FontWeight.Bold, modifier = Modifier.background(comboColor.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(horizontal=8.dp, vertical=2.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // Board
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            val size = minOf(maxWidth, maxHeight).coerceAtMost(400.dp)
            Box(modifier = Modifier.size(size).onGloballyPositioned { coords -> boardSizePx = coords.size.width; boardTopLeft = coords.localToWindow(Offset.Zero) }.background(FitBlockColors.BoardSurface, RoundedCornerShape(20.dp)).padding(10.dp)) {
                BoardView(vm.board, vm.dragPreviewPos, vm.isValidPreview, vm.lastCleared, boardSizePx)
                // Drag overlay hit detection
                if (vm.gameState == GameState.Dragging) {
                    Box(Modifier.fillMaxSize()) // placeholder for board touch handling
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Piece Tray
        PieceTray(vm, boardSizePx, boardTopLeft)

        // Bottom controls
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.pause() }) { Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White) }
            Text("Coins: ${vm.coins} • Gems: ${vm.gems}", color = FitBlockColors.TextSecondary, fontSize = 12.sp)
            IconButton(onClick = { vm.toggleMusic() }) { Icon(if (vm.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff, contentDescription = "Music", tint = Color.White) }
        }
    }
}

@Composable
fun TopHud(vm: GameViewModel) {
    Row(Modifier.fillMaxWidth().background(FitBlockColors.BoardSurface.copy(alpha=0.6f), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(24.dp).background(FitBlockColors.Coin, CircleShape)); Spacer(Modifier.width(6.dp)); Text("${vm.coins}", color = Color.White, fontWeight = FontWeight.Bold) }
        Text("BEST ${vm.bestScore}", color = FitBlockColors.TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(24.dp).background(FitBlockColors.Gem, CircleShape)); Spacer(Modifier.width(6.dp)); Text("${vm.gems}", color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun BoardView(board: BoardData, previewPos: Pair<Int,Int>?, isValid: Boolean, lastCleared: ClearResult?, boardSizePx: Int) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        for (y in 0 until BoardData.SIZE) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                for (x in 0 until BoardData.SIZE) {
                    val cell = board.get(x,y)
                    val isPreview = previewPos?.let { it.first == x && it.second == y } ?: false // simplified single cell preview, actual will be multi
                    // For multi-cell preview we need to check if this board cell is covered by dragged piece - handled in tray logic via overlay
                    val isClearing = lastCleared?.cellsToClear?.contains(x to y) == true
                    val bg = when {
                        isClearing -> Color.White
                        cell.occupied -> cell.color
                        else -> FitBlockColors.BoardCellEmpty
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(2.5.dp).clip(RoundedCornerShape(8.dp)).background(bg).then(if (cell.occupied) Modifier.shadow(2.dp, RoundedCornerShape(8.dp)) else Modifier))
                }
            }
        }
    }
    // Ghost preview overlay
    if (previewPos != null) {
        // This is simplified - full ghost would draw all cells of dragged piece
        // For production we calculate ghost cells in ViewModel
    }
}

@Composable
fun PieceTray(vm: GameViewModel, boardSizePx: Int, boardTopLeft: Offset) {
    var trayWidth by remember { mutableStateOf(0) }
    Row(modifier = Modifier.fillMaxWidth().height(110.dp).background(FitBlockColors.BoardSurface.copy(alpha=0.7f), RoundedCornerShape(16.dp)).padding(8.dp).onGloballyPositioned { trayWidth = it.size.width }, horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        vm.tray.forEachIndexed { index, piece ->
            if (piece == null) {
                Box(Modifier.weight(1f).fillMaxHeight()) // empty slot
            } else {
                val isDragging = vm.draggingIndex == index
                val scale by animateFloatAsState(if (isDragging) 1.15f else 1f, label="scale")
                Box(modifier = Modifier.weight(1f).fillMaxHeight().graphicsLayer(scaleX = scale, scaleY = scale).clip(RoundedCornerShape(12.dp)).background(if (isDragging) FitBlockColors.BoardCellHighlight else Color.Transparent).pointerInput(piece) {
                    detectDragGestures(
                        onDragStart = { vm.onDragStart(index) },
                        onDragEnd = {
                            // Convert last drag position to board coordinates
                            // Simplified: use preview pos stored
                            vm.onDragEnd(vm.dragPreviewPos?.first, vm.dragPreviewPos?.second)
                        },
                        onDragCancel = { vm.onDragEnd(null, null) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Calculate board position from touch
                            // We need to map window offset to board grid
                            // Approximate: if drag goes above tray by >100px, try to map
                            // For simplicity in this version we allow direct placement via tap + auto-find best pos
                            // Production version uses precise hit testing
                            // Here we simulate by checking if drag moved up significantly, we set preview to center
                            // TODO: improve with LocalDensity and boardTopLeft
                        }
                    )
                }.clickable {
                    // Tap to auto-place at first valid spot (accessibility fallback)
                    val board = vm.board
                    for (y in 0 until BoardData.SIZE) {
                        for (x in 0 until BoardData.SIZE) {
                            if (com.fitblock.game.board.PlacementValidator.canPlace(board, piece, x, y)) {
                                vm.onDragStart(index)
                                vm.onDragMove(x, y)
                                vm.onDragEnd(x, y)
                                return@clickable
                            }
                        }
                    }
                }, contentAlignment = Alignment.Center) {
                    PieceMiniView(piece, vm.getColorForPiece(piece), Modifier.size(70.dp))
                }
            }
        }
    }
    // For drag gesture we also need to track global pointer to board
    // Simplified drag handling: use Box with pointer
    // Production: use custom DragAndDrop with LocalConfiguration
}

@Composable
fun PieceMiniView(piece: PieceDefinition, color: Color, modifier: Modifier = Modifier) {
    // Draw piece cells in a small grid
    val cellSize = 14.dp
    BoxWithConstraints(modifier = modifier) {
        // center
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                for (y in 0 until piece.height) {
                    Row {
                        for (x in 0 until piece.width) {
                            val has = piece.cells.any { it.x == x && it.y == y }
                            if (has) Box(Modifier.size(cellSize).padding(1.dp).background(color, RoundedCornerShape(4.dp))) else Box(Modifier.size(cellSize).padding(1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(vm: GameViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(FitBlockColors.Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(24.dp).background(FitBlockColors.BoardSurface, RoundedCornerShape(24.dp)).padding(24.dp).fillMaxWidth(0.9f)) {
            Text("GAME OVER", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            if (vm.showNewBest) {
                Text("NEW BEST!", color = FitBlockColors.Coin, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.background(FitBlockColors.Coin.copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(8.dp))
            }
            Text("SCORE", color = FitBlockColors.TextSecondary, fontSize = 14.sp)
            Text("${vm.score}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
            Text("BEST ${vm.bestScore}", color = FitBlockColors.TextSecondary)
            if (vm.maxCombo > 1) Text("MAX COMBO x${vm.maxCombo}", color = FitBlockColors.ComboColors[0], fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Button(onClick = { vm.startGame() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = FitBlockColors.PieceColors[1])) {
                Text("RETRY", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            OutlinedButton(onClick = { vm.goToMenu() }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp)) { Text("HOME") }

            // Rewarded ad option
            TextButton(onClick = { /* show rewarded for second chance - implement */ }) { Icon(Icons.Default.PlayCircle, null); Spacer(Modifier.width(8.dp)); Text("WATCH AD + REVIVE (50 coins)") }

            Text("Coins: ${vm.coins}  Gems: ${vm.gems}", color = FitBlockColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun PauseOverlay(vm: GameViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.6f)), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.background(FitBlockColors.BoardSurface, RoundedCornerShape(20.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PAUSED", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { vm.resume() }, modifier = Modifier.fillMaxWidth()) { Text("RESUME") }
            OutlinedButton(onClick = { vm.startGame() }, modifier = Modifier.fillMaxWidth()) { Text("RESTART") }
            OutlinedButton(onClick = { vm.goToMenu() }, modifier = Modifier.fillMaxWidth()) { Text("HOME") }
            Divider(color = FitBlockColors.BoardCellEmpty)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Music", color = Color.White); Switch(checked = vm.musicEnabled, onCheckedChange = { vm.toggleMusic() })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SFX", color = Color.White); Switch(checked = vm.sfxEnabled, onCheckedChange = { vm.toggleSfx() })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Vibration", color = Color.White); Switch(checked = vm.vibrationEnabled, onCheckedChange = { vm.toggleVibration() })
            }
        }
    }
}
