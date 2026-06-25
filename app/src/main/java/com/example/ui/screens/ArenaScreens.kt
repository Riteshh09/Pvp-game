package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.data.ChatMessage
import com.example.data.ChatChannel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.LeaderboardEntry
import com.example.data.MatchHistory
import com.example.data.PlayerProfile
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// --- AETHER DESIGN THEME SYSTEM ---
object ArenaColors {
    val DarkCanvas = Color(0xFF0D0E12)
    val SurfaceCard = Color(0xFF161922)
    val SurfaceHeader = Color(0xFF1E2230)
    
    val NeonCyan = Color(0xFF00E5FF)
    val NeonCrimson = Color(0xFFFF1744)
    val NeonPurple = Color(0xFFD500F9)
    val NeonGold = Color(0xFFFFD600)
    val NeonGreen = Color(0xFF00E676)
    
    val GrayMuted = Color(0xFF8C95A5)
    
    val GradientMain = Brush.linearGradient(
        colors = listOf(Color(0xFF1E2230), Color(0xFF0D0E12))
    )
    val GradientCyber = Brush.horizontalGradient(
        colors = listOf(NeonPurple, NeonCyan)
    )
}

// Custom Vector Avatars for visual richness
@Composable
fun AvatarIcon(
    key: String,
    size: Dp,
    modifier: Modifier = Modifier,
    borderColor: Color = ArenaColors.NeonCyan
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ArenaColors.SurfaceHeader)
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.toPx()
            val h = size.toPx()
            when (key) {
                "avatar_neon_cyber" -> {
                    // Futuristic cyber circuit icon
                    drawCircle(ArenaColors.NeonPurple.copy(alpha = 0.2f), radius = w / 2.5f)
                    drawLine(
                        color = ArenaColors.NeonCyan,
                        start = Offset(w * 0.2f, h * 0.5f),
                        end = Offset(w * 0.8f, h * 0.5f),
                        strokeWidth = 4f
                    )
                    drawCircle(ArenaColors.NeonCyan, radius = 8f, center = Offset(w * 0.35f, h * 0.5f))
                    drawCircle(ArenaColors.NeonPurple, radius = 8f, center = Offset(w * 0.65f, h * 0.5f))
                    drawLine(
                        color = ArenaColors.NeonCyan,
                        start = Offset(w * 0.5f, h * 0.25f),
                        end = Offset(w * 0.5f, h * 0.75f),
                        strokeWidth = 4f
                    )
                }
                "avatar_cyborg_tank" -> {
                    // Shield/Armor crest
                    drawCircle(ArenaColors.NeonCyan.copy(alpha = 0.2f), radius = w / 2.5f)
                    drawLine(
                        color = ArenaColors.NeonCyan,
                        start = Offset(w * 0.5f, h * 0.2f),
                        end = Offset(w * 0.25f, h * 0.45f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = ArenaColors.NeonCyan,
                        start = Offset(w * 0.25f, h * 0.45f),
                        end = Offset(w * 0.5f, h * 0.8f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = ArenaColors.NeonCyan,
                        start = Offset(w * 0.5f, h * 0.8f),
                        end = Offset(w * 0.75f, h * 0.45f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = ArenaColors.NeonCyan,
                        start = Offset(w * 0.75f, h * 0.45f),
                        end = Offset(w * 0.5f, h * 0.2f),
                        strokeWidth = 4f
                    )
                }
                "avatar_cyber_fist" -> {
                    // Heavy attack/sword crest
                    drawCircle(ArenaColors.NeonCrimson.copy(alpha = 0.2f), radius = w / 2.5f)
                    drawLine(
                        color = ArenaColors.NeonCrimson,
                        start = Offset(w * 0.3f, h * 0.7f),
                        end = Offset(w * 0.7f, h * 0.3f),
                        strokeWidth = 5f
                    )
                    drawLine(
                        color = ArenaColors.NeonCrimson,
                        start = Offset(w * 0.25f, h * 0.5f),
                        end = Offset(w * 0.5f, h * 0.75f),
                        strokeWidth = 4f
                    )
                }
                "avatar_space_ranger" -> {
                    // Solar crest
                    drawCircle(ArenaColors.NeonGold.copy(alpha = 0.2f), radius = w / 2.5f)
                    drawCircle(ArenaColors.NeonGold, radius = w / 6f)
                    for (i in 0..7) {
                        val angle = i * Math.PI / 4
                        val startX = (w / 2) + (w / 6) * Math.cos(angle).toFloat()
                        val startY = (h / 2) + (h / 6) * Math.sin(angle).toFloat()
                        val endX = (w / 2) + (w / 3.2f) * Math.cos(angle).toFloat()
                        val endY = (h / 2) + (h / 3.2f) * Math.sin(angle).toFloat()
                        drawLine(ArenaColors.NeonGold, Offset(startX, startY), Offset(endX, endY), strokeWidth = 3f)
                    }
                }
                "avatar_elite_boss" -> {
                    // Red-horned devil boss face
                    drawCircle(ArenaColors.NeonCrimson.copy(alpha = 0.3f), radius = w / 2.2f)
                    // Draw horns
                    drawLine(ArenaColors.NeonCrimson, Offset(w * 0.3f, h * 0.4f), Offset(w * 0.2f, h * 0.15f), strokeWidth = 5f)
                    drawLine(ArenaColors.NeonCrimson, Offset(w * 0.7f, h * 0.4f), Offset(w * 0.8f, h * 0.15f), strokeWidth = 5f)
                    // Eyes
                    drawCircle(ArenaColors.NeonGold, radius = 6f, center = Offset(w * 0.35f, h * 0.45f))
                    drawCircle(ArenaColors.NeonGold, radius = 6f, center = Offset(w * 0.65f, h * 0.45f))
                    // Angry mouth
                    drawLine(ArenaColors.NeonCrimson, Offset(w * 0.4f, h * 0.65f), Offset(w * 0.6f, h * 0.65f), strokeWidth = 4f)
                }
                else -> {
                    // Default gear icon
                    drawCircle(ArenaColors.NeonCyan, radius = w / 4f, style = Stroke(width = 4f))
                }
            }
        }
    }
}

// --- PROFILE CUSTOMIZER DIALOG ---
@Composable
fun ProfileCustomizerDialog(
    currentProfile: PlayerProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, avatar: String, combatClass: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentProfile.name) }
    var selectedAvatar by remember { mutableStateOf(currentProfile.chosenAvatar) }
    var selectedClass by remember { mutableStateOf(currentProfile.chosenClass) }

    val avatars = listOf("avatar_neon_cyber", "avatar_cyborg_tank", "avatar_cyber_fist", "avatar_space_ranger")
    val classes = listOf("Stryker", "Aegis", "Chrono")
    val classDescs = mapOf(
        "Stryker" to "Offensive Specialist: Hyper strike speed & amplified regular strike capacity.",
        "Aegis" to "Defensive Bastion: Multi-layered shield and heavy strike resistance.",
        "Chrono" to "Quantum Manipulator: Speed evasion, hyper ultimate charging."
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
            border = BorderStroke(1.5.dp, ArenaColors.NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RECONFIGURE GLADIATOR",
                    color = ArenaColors.NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Name Input
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 15) nameInput = it },
                    label = { Text("Gladiator Alias", color = ArenaColors.GrayMuted) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaColors.NeonCyan,
                        unfocusedBorderColor = ArenaColors.GrayMuted,
                        focusedLabelColor = ArenaColors.NeonCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("name_input"),
                    singleLine = true
                )

                // Avatar Selection
                Text(
                    text = "SELECT CHASSIS HOLOGRAPH",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    avatars.forEach { avatar ->
                        val isSelected = selectedAvatar == avatar
                        AvatarIcon(
                            key = avatar,
                            size = 56.dp,
                            borderColor = if (isSelected) ArenaColors.NeonCyan else Color.Transparent,
                            modifier = Modifier
                                .clickable { selectedAvatar = avatar }
                                .scale(if (isSelected) 1.1f else 0.95f)
                        )
                    }
                }

                // Class Selection
                Text(
                    text = "COMBAT CLASS INJECTOR",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    classes.forEach { compClass ->
                        val isSelected = selectedClass == compClass
                        Button(
                            onClick = { selectedClass = compClass },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) ArenaColors.NeonPurple else ArenaColors.SurfaceHeader,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ArenaColors.NeonPurple else ArenaColors.GrayMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(compClass, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Class description
                Text(
                    text = classDescs[selectedClass] ?: "",
                    color = ArenaColors.GrayMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(36.dp)
                )

                // Save buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, ArenaColors.GrayMuted),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = { onSave(nameInput, selectedAvatar, selectedClass) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.NeonCyan, contentColor = ArenaColors.DarkCanvas),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_profile_button")
                    ) {
                        Text("SAVE DATA", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// --- MAIN DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    onCustomizeProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Profile Header
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
            border = BorderStroke(1.dp, ArenaColors.SurfaceHeader)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarIcon(key = profile.chosenAvatar, size = 56.dp)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onCustomizeProfileClick,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = ArenaColors.NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "Class: ${profile.chosenClass} | Lv.${profile.level}",
                        color = ArenaColors.NeonCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // XP progress bar
                    val xpNeeded = 100 + (profile.level * 50)
                    val xpProgress = profile.xp.toFloat() / xpNeeded
                    LinearProgressIndicator(
                        progress = xpProgress,
                        color = ArenaColors.NeonPurple,
                        trackColor = ArenaColors.SurfaceHeader,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("ELO RATING", color = ArenaColors.GrayMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        "${profile.rankRating}",
                        color = ArenaColors.NeonGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Hero Combat Banner Card (Interactive!)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, ArenaColors.NeonPurple.copy(alpha = 0.5f))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // Direct loading of our custom generated drawable banner
                    Image(
                        painter = painterResource(id = R.drawable.img_pvp_arena_banner),
                        contentDescription = "PvP Battle Arena Header Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Vignette Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, ArenaColors.DarkCanvas.copy(alpha = 0.85f))
                                )
                            )
                    )
                    
                    // Floating Badge
                    Surface(
                        color = ArenaColors.NeonCrimson,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "LIVE PVP ARENA",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            "AETHER ARENA: NEON CONFLICT",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Challenge cybernetic contenders in quick PvP combat rounds.",
                            color = ArenaColors.GrayMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Match Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Match Normal PVP
                    Button(
                        onClick = { viewModel.startMatchmaking(isEliteBoss = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.NeonCyan, contentColor = ArenaColors.DarkCanvas),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pvp_match_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play PvP")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FIND COMP", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    // Match Elite boss
                    Button(
                        onClick = { viewModel.startMatchmaking(isEliteBoss = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.NeonPurple, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("elite_boss_button")
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Elite Boss")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ELITE CHAMP", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Mini statistics & History shortcut
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                border = BorderStroke(1.dp, ArenaColors.SurfaceHeader),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(ScreenState.MatchHistoryView) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("WAR STATISTICS", color = ArenaColors.GrayMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "W: ${profile.wins} / L: ${profile.losses}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("View logs & combat review", color = ArenaColors.NeonCyan, fontSize = 9.sp)
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                border = BorderStroke(1.dp, ArenaColors.SurfaceHeader),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(ScreenState.Leaderboard) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("GLOBAL STANDINGS", color = ArenaColors.GrayMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "Leaderboard", tint = ArenaColors.NeonGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LEADERBOARD", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Compete for top 10 positions", color = ArenaColors.NeonCyan, fontSize = 9.sp)
                }
            }
        }

        // Global Chat Sector
        GlobalChatCard(viewModel = viewModel)
    }
}

@Composable
fun GlobalChatCard(viewModel: GameViewModel) {
    val messages by viewModel.globalMessages.collectAsState()
    val mutedPlayers by viewModel.mutedPlayers.collectAsState()
    var showFullConsole by remember { mutableStateOf(false) }

    val activeMessages = messages.filter { !mutedPlayers.contains(it.senderName) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
        border = BorderStroke(1.dp, ArenaColors.NeonCyan.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ArenaColors.NeonGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GLOBAL SECTOR CHAT",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "ONLINE",
                    color = ArenaColors.NeonGreen,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last 2 messages preview
            if (activeMessages.isEmpty()) {
                Text(
                    text = "Sector comms offline. Send a signal.",
                    color = ArenaColors.GrayMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    activeMessages.takeLast(2).forEach { msg ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${msg.senderName}: ",
                                color = ArenaColors.NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = msg.content,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showFullConsole = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArenaColors.SurfaceHeader,
                    contentColor = ArenaColors.NeonCyan
                ),
                border = BorderStroke(1.dp, ArenaColors.NeonCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text(
                    "OPEN SECTOR CONSOLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    if (showFullConsole) {
        ChatConsoleDialog(
            title = "GLOBAL SECTOR CONSOLE",
            messages = messages,
            mutedPlayers = mutedPlayers,
            onSendMessage = { viewModel.sendGlobalMessage(it) },
            onToggleMute = { viewModel.toggleMutePlayer(it) },
            onDismiss = { showFullConsole = false }
        )
    }
}

@Composable
fun ChatConsoleDialog(
    title: String,
    messages: List<ChatMessage>,
    mutedPlayers: Set<String>,
    onSendMessage: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    val activeMessages = messages.filter { !mutedPlayers.contains(it.senderName) }

    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
            border = BorderStroke(1.5.dp, ArenaColors.NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = ArenaColors.NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = ArenaColors.SurfaceHeader,
                    thickness = 1.dp
                )

                // Muted list bar
                if (mutedPlayers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MUTED: ", color = ArenaColors.NeonCrimson, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(4.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mutedPlayers.forEach { mutedName ->
                                Surface(
                                    color = ArenaColors.SurfaceHeader,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, ArenaColors.NeonCrimson.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { onToggleMute(mutedName) }
                                ) {
                                    Text(
                                        text = "$mutedName ✕",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Message list
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeMessages) { msg ->
                        if (msg.isSystem) {
                            Surface(
                                color = ArenaColors.SurfaceHeader,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg.content,
                                    color = ArenaColors.NeonGold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                AvatarIcon(key = msg.senderAvatar, size = 28.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = msg.senderName,
                                            color = if (msg.senderName == "VoltScythe" || msg.senderName == "ApexSentinel") ArenaColors.NeonPurple else ArenaColors.NeonCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val timeString = android.text.format.DateFormat.format("hh:mm a", msg.timestamp).toString()
                                        Text(
                                            text = timeString,
                                            color = ArenaColors.GrayMuted,
                                            fontSize = 8.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "[MUTE]",
                                            color = ArenaColors.NeonCrimson,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.clickable { onToggleMute(msg.senderName) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.content,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = ArenaColors.SurfaceHeader,
                    thickness = 1.dp
                )

                // Input bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { if (it.length <= 100) textInput = it },
                        placeholder = { Text("Enter signal...", color = ArenaColors.GrayMuted, fontSize = 12.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArenaColors.NeonCyan,
                            unfocusedBorderColor = ArenaColors.SurfaceHeader,
                            focusedLabelColor = ArenaColors.NeonCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.NeonCyan, contentColor = ArenaColors.DarkCanvas),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("SEND", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// --- MATCHMAKING SCREEN ---
@Composable
fun MatchmakingScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.matchmakingState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition()
    
    // Custom radar-sweep alpha animation
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaColors.DarkCanvas)
            .padding(24.dp)
            .testTag("matchmaking_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val currState = state) {
            is MatchmakingState.Searching -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .drawBehind {
                            drawCircle(
                                color = ArenaColors.NeonCyan.copy(alpha = 0.05f),
                                radius = size.minDimension / 2f
                            )
                            drawCircle(
                                color = ArenaColors.NeonCyan.copy(alpha = pulseAlpha * 0.15f),
                                radius = size.minDimension / 3f,
                                style = Stroke(width = 4f)
                            )
                            drawCircle(
                                color = ArenaColors.NeonCyan,
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 2f)
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Searching",
                        tint = ArenaColors.NeonCyan,
                        modifier = Modifier
                            .size(54.dp)
                            .rotateAnimation()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "SEARCHING NEURAL MATCHES",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Lobby Ping: ${currState.expectedPing}ms  |  Elapsed: ${currState.elapsedSeconds}s",
                    color = ArenaColors.NeonCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedButton(
                    onClick = { viewModel.cancelMatchmaking() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaColors.NeonCrimson),
                    border = BorderStroke(1.dp, ArenaColors.NeonCrimson),
                    modifier = Modifier.testTag("cancel_match_button")
                ) {
                    Text("ABORT QUEUE", fontFamily = FontFamily.Monospace)
                }
            }

            is MatchmakingState.Found -> {
                Text(
                    text = "OPPONENT TARGET ENGAGED!",
                    color = ArenaColors.NeonGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Render Opponent details card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                    border = BorderStroke(1.5.dp, if (currState.isElite) ArenaColors.NeonPurple else ArenaColors.NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarIcon(
                            key = currState.opponent.avatar,
                            size = 80.dp,
                            borderColor = if (currState.isElite) ArenaColors.NeonPurple else ArenaColors.NeonCyan
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currState.isElite) {
                            Surface(
                                color = ArenaColors.NeonPurple,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "ELITE BOSS CHAMPION",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = currState.opponent.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = currState.opponent.combatClass,
                            color = ArenaColors.NeonCyan,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ELO RATING", color = ArenaColors.GrayMuted, fontSize = 9.sp)
                                Text("${currState.opponent.rankRating}", color = ArenaColors.NeonGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("W / L RECORD", color = ArenaColors.GrayMuted, fontSize = 9.sp)
                                Text("${currState.opponent.wins} - ${currState.opponent.losses}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LATENCY", color = ArenaColors.GrayMuted, fontSize = 9.sp)
                                Text("${currState.opponent.ping}ms", color = ArenaColors.NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ENTER BUTTON
                Button(
                    onClick = { viewModel.enterArena(currState.opponent, currState.isElite) },
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.NeonGreen, contentColor = ArenaColors.DarkCanvas),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("enter_arena_button")
                ) {
                    Text("ENTER ARENA CHAMBER", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                }
            }

            else -> {}
        }
    }
}

@Composable
fun Modifier.rotateAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing)
        )
    )
    return this.drawBehind {
        drawContext.transform.rotate(angle, center)
    }
}


// --- REAL-TIME BATTLE ARENA SCREEN ---
@Composable
fun BattleArenaScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile
) {
    val opponent = viewModel.activeOpponent.collectAsState().value ?: return
    val pCombat by viewModel.playerCombat.collectAsState()
    val oCombat by viewModel.opponentCombat.collectAsState()

    val pStrikeCd by viewModel.playerStrikeCooldown.collectAsState()
    val pShieldCd by viewModel.playerShieldCooldown.collectAsState()
    val pDodgeCd by viewModel.playerDodgeCooldown.collectAsState()

    val logs by viewModel.battleLogs.collectAsState()
    val floatingTexts by viewModel.floatingTexts.collectAsState()
    val opponentBanter by viewModel.opponentBanter.collectAsState()
    val winner by viewModel.matchWinner.collectAsState()

    var showTeamChat by remember { mutableStateOf(false) }
    val teamMessages by viewModel.teamMessages.collectAsState()
    val mutedPlayers by viewModel.mutedPlayers.collectAsState()

    // Screen Shake state triggers on damage
    var shakeOffset by remember { mutableStateOf(0f) }
    LaunchedEffect(pCombat.hp) {
        if (pCombat.hp < 100) {
            shakeOffset = 10f
            delay(50)
            shakeOffset = -10f
            delay(50)
            shakeOffset = 5f
            delay(50)
            shakeOffset = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaColors.DarkCanvas)
            .padding(12.dp)
            .offset(x = shakeOffset.dp)
            .testTag("battle_arena_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // 1. OPPONENT FIGHTER PROFILE CARD (Top)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                border = BorderStroke(1.dp, ArenaColors.NeonCrimson)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarIcon(key = opponent.avatar, size = 36.dp, borderColor = ArenaColors.NeonCrimson)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(opponent.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Class: ${opponent.combatClass}", color = ArenaColors.NeonCrimson, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text("${oCombat.hp} / ${opponent.hp} HP", color = ArenaColors.NeonCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    // HP and Shield visual bars
                    CustomHealthBar(hp = oCombat.hp, maxHp = opponent.hp, shield = oCombat.shield, barColor = ArenaColors.NeonCrimson)
                }
            }

            // 2. MIDDLE COMBAT STAGE (Real-time visualizations)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceHeader),
                border = BorderStroke(1.dp, ArenaColors.SurfaceCard),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Grid background decoration
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val space = 30.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height))
                            x += space
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(Color.White.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y))
                            y += space
                        }
                    }

                    // Opponent fighter representation
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (oCombat.isEvading) ArenaColors.NeonPurple else ArenaColors.NeonCrimson.copy(alpha = 0.2f))
                                .border(
                                    2.dp,
                                    if (oCombat.isEvading) ArenaColors.NeonPurple else ArenaColors.NeonCrimson,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (oCombat.isEvading) Icons.Default.PlayArrow else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (oCombat.isEvading) "EVADING..." else "STRIKING POSITION",
                            color = if (oCombat.isEvading) ArenaColors.NeonPurple else ArenaColors.NeonCrimson,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Opponent Banter Speech Bubble (Cyber Trash Talk)
                    if (opponentBanter.isNotEmpty()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ArenaColors.NeonPurple.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = ArenaColors.NeonPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = opponentBanter,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }

                    // Player Fighter representation
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (pCombat.isEvading) "EVADING..." else "ACTIVE COMBATANT",
                            color = if (pCombat.isEvading) ArenaColors.NeonPurple else ArenaColors.NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (pCombat.isEvading) ArenaColors.NeonPurple else ArenaColors.NeonCyan.copy(alpha = 0.2f))
                                .border(
                                    2.dp,
                                    if (pCombat.isEvading) ArenaColors.NeonPurple else ArenaColors.NeonCyan,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Real-Time Floating Damage Text elements
                    floatingTexts.forEach { entry ->
                        val alignment = if (entry.isPlayer) Alignment.CenterStart else Alignment.CenterEnd
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (entry.isPlayer) 32.dp else 12.dp),
                            contentAlignment = alignment
                        ) {
                            Text(
                                text = entry.text,
                                color = Color(android.graphics.Color.parseColor(entry.colorHex)),
                                fontSize = if (entry.isDamage) 22.sp else 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }

            // 3. PLAYER FIGHTER PROFILE STATUS CARD (Bottom Header)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                border = BorderStroke(1.dp, ArenaColors.NeonCyan)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarIcon(key = profile.chosenAvatar, size = 36.dp, borderColor = ArenaColors.NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${profile.name} (YOU)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Class: ${profile.chosenClass}", color = ArenaColors.NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text("${pCombat.hp} / 100 HP", color = ArenaColors.NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Player Health & Shield bar
                    CustomHealthBar(hp = pCombat.hp, maxHp = 100, shield = pCombat.shield, barColor = ArenaColors.NeonCyan)

                    Spacer(modifier = Modifier.height(6.dp))

                    // Energy Charge progress bar (for Overlord Ultimate)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ULTIMATE CHARGE:", color = ArenaColors.GrayMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = pCombat.energy.toFloat() / 100f,
                            color = if (pCombat.energy >= 100) ArenaColors.NeonGold else ArenaColors.NeonPurple,
                            trackColor = ArenaColors.SurfaceHeader,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${pCombat.energy}%", color = if (pCombat.energy >= 100) ArenaColors.NeonGold else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // 4. PLAYER ACTION CONTROLS ENGINE DECK
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                border = BorderStroke(1.dp, ArenaColors.SurfaceHeader)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ACTION STRIKE
                    CombatButton(
                        label = "STRIKE",
                        subLabel = if (pStrikeCd > 0) "${pStrikeCd / 100}s" else "READY",
                        color = ArenaColors.NeonCrimson,
                        cooldownPercentage = pStrikeCd.toFloat() / 1000f,
                        isEnabled = pStrikeCd <= 0 && winner == null,
                        onClick = { viewModel.playerStrike() },
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f).testTag("strike_button")
                    )

                    // ACTION SHIELD
                    CombatButton(
                        label = "SHIELD",
                        subLabel = if (pShieldCd > 0) "${pShieldCd / 100}s" else "READY",
                        color = ArenaColors.NeonCyan,
                        cooldownPercentage = pShieldCd.toFloat() / 2000f,
                        isEnabled = pShieldCd <= 0 && winner == null,
                        onClick = { viewModel.playerShield() },
                        icon = Icons.Default.Info,
                        modifier = Modifier.weight(1f).testTag("shield_button")
                    )

                    // ACTION EVADE
                    CombatButton(
                        label = "EVADE",
                        subLabel = if (pDodgeCd > 0) "${pDodgeCd / 100}s" else "READY",
                        color = ArenaColors.NeonPurple,
                        cooldownPercentage = pDodgeCd.toFloat() / 2500f,
                        isEnabled = pDodgeCd <= 0 && winner == null,
                        onClick = { viewModel.playerDodge() },
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.weight(1f).testTag("evade_button")
                    )

                    // OVERLORD ULTIMATE CHARGE BUTTON
                    val isUltReady = pCombat.energy >= 100 && winner == null
                    Button(
                        onClick = { viewModel.playerOverload() },
                        enabled = isUltReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArenaColors.NeonGold,
                            disabledContainerColor = ArenaColors.SurfaceHeader,
                            contentColor = ArenaColors.DarkCanvas,
                            disabledContentColor = ArenaColors.GrayMuted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp)
                            .testTag("ult_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("OVERLOAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(if (isUltReady) "RELEASE" else "LOCKED", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // 5. COMBAT TERMINAL MESSAGE LOGGER
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val lazyListState = rememberScrollState()
                LaunchedEffect(logs.size) {
                    lazyListState.animateScrollTo(lazyListState.maxValue)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .verticalScroll(lazyListState)
                ) {
                    logs.forEach { log ->
                        Text(
                            text = "> $log",
                            color = ArenaColors.NeonCyan,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }

        // Teammate Chat Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 160.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { showTeamChat = true },
                containerColor = ArenaColors.NeonPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Team Chat",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Unread / Active badge
            if (teamMessages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(ArenaColors.NeonCyan)
                        .align(Alignment.TopEnd)
                        .border(1.5.dp, ArenaColors.DarkCanvas, CircleShape)
                )
            }
        }

        if (showTeamChat) {
            ChatConsoleDialog(
                title = "TACTICAL TEAM COMMS",
                messages = teamMessages,
                mutedPlayers = mutedPlayers,
                onSendMessage = { viewModel.sendTeamMessage(it) },
                onToggleMute = { viewModel.toggleMutePlayer(it) },
                onDismiss = { showTeamChat = false }
            )
        }

        // 6. FULL-SCREEN GAME OVER COMBAT SUMMARY DIALOG
        val showEndDialog by viewModel.showMatchEndDialog.collectAsState()
        if (showEndDialog) {
            MatchEndDialog(
                viewModel = viewModel,
                onClose = { viewModel.closeMatchEndDialog() }
            )
        }
    }
}

// Custom health & Shield visual container
@Composable
fun CustomHealthBar(
    hp: Int,
    maxHp: Int,
    shield: Int,
    barColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(ArenaColors.SurfaceHeader)
    ) {
        // HP level
        val hpProgress = (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(hpProgress)
                .background(barColor)
        )
        // Shield level
        if (shield > 0) {
            val shieldProgress = (shield.toFloat() / 50f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(shieldProgress)
                    .background(ArenaColors.NeonCyan.copy(alpha = 0.5f))
                    .border(1.dp, ArenaColors.NeonCyan, RoundedCornerShape(4.dp))
            )
        }
    }
}

// Custom Grid Combat Buttons
@Composable
fun CombatButton(
    label: String,
    subLabel: String,
    color: Color,
    cooldownPercentage: Float,
    isEnabled: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(enabled = isEnabled, onClick = onClick),
        color = if (isEnabled) ArenaColors.SurfaceHeader else ArenaColors.SurfaceHeader.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isEnabled) color.copy(alpha = 0.7f) else ArenaColors.SurfaceCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cooldown overlay sweep
            if (cooldownPercentage > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .fillMaxWidth(cooldownPercentage)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = if (isEnabled) color else ArenaColors.GrayMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(label, color = if (isEnabled) Color.White else ArenaColors.GrayMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(subLabel, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// --- FULLSCREEN MATCH OVER DIALOG WITH GEMINI AI COACHING ---
@Composable
fun MatchEndDialog(
    viewModel: GameViewModel,
    onClose: () -> Unit
) {
    val winner by viewModel.matchWinner.collectAsState()
    val ratingChange by viewModel.finalRatingChange.collectAsState()
    val coaching by viewModel.coachingFeedback.collectAsState()
    val isCoachingLoading by viewModel.isCoachingLoading.collectAsState()

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
            border = BorderStroke(1.5.dp, ArenaColors.NeonGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("match_end_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bannerTitle = when (winner) {
                    "PLAYER" -> "VICTORY ATTAINED"
                    "OPPONENT" -> "CHASSIS TERMINATED"
                    else -> "ARENA DRAW"
                }
                val bannerColor = when (winner) {
                    "PLAYER" -> ArenaColors.NeonGreen
                    "OPPONENT" -> ArenaColors.NeonCrimson
                    else -> ArenaColors.NeonCyan
                }

                Text(
                    text = bannerTitle,
                    color = bannerColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "RANK ADJUSTMENT: ",
                        color = ArenaColors.GrayMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (ratingChange >= 0) "+$ratingChange ELO" else "$ratingChange ELO",
                        color = if (ratingChange >= 0) ArenaColors.NeonGreen else ArenaColors.NeonCrimson,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AI Coaching Segment (Gemini Powered!)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceHeader),
                    border = BorderStroke(1.dp, ArenaColors.NeonPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ArenaColors.NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GEMINI COMBAT REVIEW",
                                color = ArenaColors.NeonPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isCoachingLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ArenaColors.NeonPurple, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Text(
                                text = coaching,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.NeonCyan, contentColor = ArenaColors.DarkCanvas),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("close_match_dialog_button")
                ) {
                    Text("RETURN TO DOCK", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// --- GLOBAL LEADERBOARD VIEW ---
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile
) {
    val entries by viewModel.leaderboard.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaColors.DarkCanvas)
            .padding(16.dp)
            .testTag("leaderboard_screen")
    ) {
        // Leaderboard header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenState.MainDashboard) }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "ARENA SECTOR STANDINGS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    scope.launch {
                        isRefreshing = true
                        delay(1200)
                        isRefreshing = false
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = ArenaColors.NeonCyan,
                    modifier = Modifier.rotateAnimationIf(isRefreshing)
                )
            }
        }

        if (isRefreshing) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArenaColors.NeonCyan)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    val isPlayer = entry.name == profile.name
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayer) ArenaColors.SurfaceHeader else ArenaColors.SurfaceCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPlayer) ArenaColors.NeonCyan else ArenaColors.SurfaceHeader
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank number / icon
                            val rankColor = when (entry.rank) {
                                1 -> ArenaColors.NeonGold
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color.White
                            }
                            Text(
                                text = "#${entry.rank}",
                                color = rankColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(36.dp)
                            )

                            // Avatar icon representation
                            AvatarIcon(key = entry.chosenAvatar, size = 32.dp, borderColor = rankColor)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.name,
                                    color = if (isPlayer) ArenaColors.NeonCyan else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${entry.chosenClass} | W: ${entry.wins} L: ${entry.losses}",
                                    color = ArenaColors.GrayMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = when (entry.statusTag) {
                                        "CHAMPION" -> ArenaColors.NeonPurple.copy(alpha = 0.2f)
                                        "ELITE" -> ArenaColors.NeonCrimson.copy(alpha = 0.2f)
                                        else -> ArenaColors.SurfaceHeader
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when (entry.statusTag) {
                                            "CHAMPION" -> ArenaColors.NeonPurple
                                            "ELITE" -> ArenaColors.NeonCrimson
                                            else -> ArenaColors.GrayMuted
                                        }
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = entry.statusTag,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    "${entry.rankRating}",
                                    color = ArenaColors.NeonGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.rotateAnimationIf(trigger: Boolean): Modifier {
    return if (trigger) this.rotateAnimation() else this
}


// --- HISTORIC MATCH LOGS VIEW ---
@Composable
fun MatchHistoryScreen(
    viewModel: GameViewModel
) {
    val records by viewModel.matchHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaColors.DarkCanvas)
            .padding(16.dp)
            .testTag("match_history_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenState.MainDashboard) }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "GLADIATOR DEPLOYMENT LOGS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (records.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Empty", tint = ArenaColors.GrayMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No combat matches deployed yet.", color = ArenaColors.GrayMuted, fontSize = 14.sp)
                    Text("Enter the arena to record initial battle logs.", color = ArenaColors.NeonCyan, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { record ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ArenaColors.SurfaceCard),
                        border = BorderStroke(
                            1.dp,
                            if (record.result == "WIN") ArenaColors.NeonGreen.copy(alpha = 0.5f) else ArenaColors.NeonCrimson.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarIcon(key = record.opponentAvatar, size = 32.dp, borderColor = ArenaColors.GrayMuted)
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(record.opponentName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        if (record.isElite) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = ArenaColors.NeonPurple, shape = RoundedCornerShape(3.dp)) {
                                                Text("ELITE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text("Class: ${record.opponentClass}  |  Duration: ${record.durationSeconds}s", color = ArenaColors.GrayMuted, fontSize = 10.sp)
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    val labelColor = when (record.result) {
                                        "WIN" -> ArenaColors.NeonGreen
                                        "LOSS" -> ArenaColors.NeonCrimson
                                        else -> ArenaColors.NeonCyan
                                    }
                                    Text(
                                        text = record.result,
                                        color = labelColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    val ratingChangeLabel = if (record.ratingChange >= 0) "+${record.ratingChange}" else "${record.ratingChange}"
                                    Text(
                                        text = "$ratingChangeLabel Elo",
                                        color = labelColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = ArenaColors.SurfaceHeader, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Dealt: ${record.playerDamageDealt} DMG",
                                    color = ArenaColors.NeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    "Sustained: ${record.opponentDamageDealt} DMG",
                                    color = ArenaColors.NeonCrimson,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
