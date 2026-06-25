package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.EliteChampion
import com.example.network.GeminiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface MatchmakingState {
    object Idle : MatchmakingState
    data class Searching(val elapsedSeconds: Int, val expectedPing: Int) : MatchmakingState
    data class Found(val opponent: OpponentProfile, val isElite: Boolean = false) : MatchmakingState
}

data class OpponentProfile(
    val name: String,
    val rankRating: Int,
    val avatar: String,
    val combatClass: String,
    val wins: Int,
    val losses: Int,
    val ping: Int,
    val specialAttackName: String = "Plasma Barrage",
    val specialAttackPower: Int = 22,
    val battleCry: String = "You're going offline!",
    val hp: Int = 100
)

data class CombatantState(
    val maxHp: Int = 100,
    val hp: Int = 100,
    val energy: Int = 0,
    val maxEnergy: Int = 100,
    val shield: Int = 0,
    val maxShield: Int = 50,
    val isEvading: Boolean = false
)

data class FloatingText(
    val id: Long,
    val text: String,
    val isDamage: Boolean,
    val isPlayer: Boolean, // True if floating over Player, false if Opponent
    val colorHex: String,
    val alpha: Float = 1f
)

sealed interface ScreenState {
    object MainDashboard : ScreenState
    object Matchmaking : ScreenState
    object BattleArena : ScreenState
    object Leaderboard : ScreenState
    object MatchHistoryView : ScreenState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "GameViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val repository = GameRepository(database.playerDao())
    private val geminiService = GeminiService()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.MainDashboard)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    // Room DB bindings
    val playerProfile: StateFlow<PlayerProfile?> = repository.playerProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val matchHistory: StateFlow<List<MatchHistory>> = repository.matchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboardEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI match finding state
    private val _matchmakingState = MutableStateFlow<MatchmakingState>(MatchmakingState.Idle)
    val matchmakingState: StateFlow<MatchmakingState> = _matchmakingState.asStateFlow()

    // Combat Arena active states
    val playerCombat = MutableStateFlow(CombatantState())
    val opponentCombat = MutableStateFlow(CombatantState())
    val activeOpponent = MutableStateFlow<OpponentProfile?>(null)

    // Cooldown states (in milliseconds remaining)
    val playerStrikeCooldown = MutableStateFlow(0L)
    val playerShieldCooldown = MutableStateFlow(0L)
    val playerDodgeCooldown = MutableStateFlow(0L)

    // Log messages
    val battleLogs = MutableStateFlow<List<String>>(emptyList())
    val floatingTexts = MutableStateFlow<List<FloatingText>>(emptyList())

    // Mid-match Trash Talk or Banter
    val opponentBanter = MutableStateFlow("")

    // Match end summary
    val matchWinner = MutableStateFlow<String?>(null) // "PLAYER", "OPPONENT", "DRAW"
    val showMatchEndDialog = MutableStateFlow(false)
    val finalRatingChange = MutableStateFlow(0)
    val coachingFeedback = MutableStateFlow("")
    val isCoachingLoading = MutableStateFlow(false)

    // Background jobs
    private var matchmakingJob: Job? = null
    private var combatEngineJob: Job? = null
    private var banterJob: Job? = null

    // Chat states
    private val _globalMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val globalMessages: StateFlow<List<ChatMessage>> = _globalMessages.asStateFlow()

    private val _teamMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val teamMessages: StateFlow<List<ChatMessage>> = _teamMessages.asStateFlow()

    private val _mutedPlayers = MutableStateFlow<Set<String>>(emptySet())
    val mutedPlayers: StateFlow<Set<String>> = _mutedPlayers.asStateFlow()

    private var globalChatSimulationJob: Job? = null
    private var teammateChatSimulationJob: Job? = null

    init {
        // Create initial profile if not exists
        viewModelScope.launch {
            repository.getOrCreateProfile("NetRunner_X")
        }
        initializeGlobalMessages()
        startGlobalChatSimulation()
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    // Set user profile details (Customizer)
    fun customizeProfile(name: String, avatar: String, combatClass: String) {
        viewModelScope.launch {
            val current = playerProfile.value
            if (current != null) {
                repository.updateProfile(
                    current.copy(
                        name = name.ifBlank { current.name },
                        chosenAvatar = avatar,
                        chosenClass = combatClass
                    )
                )
            }
        }
    }

    // MATCHMAKING ACTION
    fun startMatchmaking(isEliteBoss: Boolean = false) {
        navigateTo(ScreenState.Matchmaking)
        _matchmakingState.value = MatchmakingState.Searching(0, Random.nextInt(18, 45))
        
        matchmakingJob?.cancel()
        matchmakingJob = viewModelScope.launch {
            var elapsed = 0
            while (elapsed < 3) {
                delay(1000)
                elapsed++
                _matchmakingState.value = MatchmakingState.Searching(elapsed, Random.nextInt(18, 45))
            }

            val playerRating = playerProfile.value?.rankRating ?: 1000
            
            if (isEliteBoss) {
                // Fetch daily boss from Gemini Service!
                val boss = geminiService.generateEliteChampion(playerRating)
                val opponent = OpponentProfile(
                    name = boss.name,
                    rankRating = playerRating + 150,
                    avatar = "avatar_elite_boss",
                    combatClass = boss.title,
                    wins = 350,
                    losses = 45,
                    ping = 12,
                    specialAttackName = boss.specialAttackName,
                    specialAttackPower = boss.specialAttackPower,
                    battleCry = boss.battleCry
                )
                _matchmakingState.value = MatchmakingState.Found(opponent, isElite = true)
            } else {
                // Generate standard cyberpunk opponent
                val names = listOf("Cypher_Blade", "Aegis_X", "Glitch_Phantom", "Byte_Slayer", "Quantum_Ranger", "Neuro_Wrecker", "Overclock_Viper")
                val opponentName = names.random() + "_" + Random.nextInt(10, 99)
                val diff = Random.nextInt(-90, 90)
                val oppRating = (playerRating + diff).coerceAtLeast(100)
                val avatars = listOf("avatar_space_ranger", "avatar_cyborg_tank", "avatar_cyber_fist", "avatar_neon_cyber")
                val classes = listOf("Stryker", "Aegis", "Chrono")
                
                val opponent = OpponentProfile(
                    name = opponentName,
                    rankRating = oppRating,
                    avatar = avatars.random(),
                    combatClass = classes.random(),
                    wins = Random.nextInt(10, 80),
                    losses = Random.nextInt(10, 80),
                    ping = Random.nextInt(20, 55),
                    specialAttackName = when(classes.random()) {
                        "Stryker" -> "Hyper-Fist Blitz"
                        "Aegis" -> "Phalanx Shockwave"
                        else -> "Chrono-Rift Slash"
                    },
                    specialAttackPower = Random.nextInt(20, 26)
                )
                _matchmakingState.value = MatchmakingState.Found(opponent, isElite = false)
            }
        }
    }

    fun cancelMatchmaking() {
        matchmakingJob?.cancel()
        _matchmakingState.value = MatchmakingState.Idle
        navigateTo(ScreenState.MainDashboard)
    }

    // ENTER ARENA - STARTS COMBAT
    fun enterArena(opponent: OpponentProfile, isElite: Boolean) {
        activeOpponent.value = opponent
        opponentBanter.value = opponent.battleCry
        
        // Reset Combatant healths
        playerCombat.value = CombatantState(
            maxHp = 100,
            hp = 100,
            energy = 0,
            shield = 0
        )
        opponentCombat.value = CombatantState(
            maxHp = opponent.hp,
            hp = opponent.hp,
            energy = 0,
            shield = 0
        )

        // Reset inputs
        playerStrikeCooldown.value = 0L
        playerShieldCooldown.value = 0L
        playerDodgeCooldown.value = 0L
        battleLogs.value = listOf("Combat initiated. Face ${opponent.name}!")
        floatingTexts.value = emptyList()
        matchWinner.value = null
        showMatchEndDialog.value = false
        coachingFeedback.value = ""

        navigateTo(ScreenState.BattleArena)
        
        // Reset Team messages for this match
        _teamMessages.value = listOf(
            ChatMessage(
                senderName = "SYSTEM",
                content = "Tactical Link Established. Teammates: VoltScythe, ApexSentinel",
                channel = ChatChannel.TEAM,
                isSystem = true
            ),
            ChatMessage(
                senderName = "VoltScythe",
                senderAvatar = "avatar_neon_cyber",
                content = "Alright gladiators, let's take down ${opponent.name}! Focus fire!",
                channel = ChatChannel.TEAM
            ),
            ChatMessage(
                senderName = "ApexSentinel",
                senderAvatar = "avatar_cyborg_tank",
                content = "Affirmative. I'll monitor their defense lines. Let's get that ELO!",
                channel = ChatChannel.TEAM
            )
        )
        startTeammateChatSimulation(opponent)

        // Start Combat Engine Loop
        startCombatEngine(opponent, isElite)
    }

    // COMBAT ACTIONS (PLAYER INPUT)
    fun playerStrike() {
        if (playerStrikeCooldown.value > 0 || matchWinner.value != null) return
        playerStrikeCooldown.value = 1000L // 1.0s cooldown

        val dmg = Random.nextInt(8, 13)
        dealDamageToOpponent(dmg, isCritical = Random.nextFloat() < 0.15f)
        
        // Charge player Energy
        playerCombat.update { it.copy(energy = (it.energy + 15).coerceAtMost(100)) }
    }

    fun playerShield() {
        if (playerShieldCooldown.value > 0 || matchWinner.value != null) return
        playerShieldCooldown.value = 2000L // 2s cooldown

        playerCombat.update {
            it.copy(shield = (it.shield + 25).coerceAtMost(50))
        }
        addFloatingText("SHIELD +25", isDamage = false, isPlayer = true, "#00E5FF")
        addBattleLog("You activated Barrier Core.")
    }

    fun playerDodge() {
        if (playerDodgeCooldown.value > 0 || matchWinner.value != null) return
        playerDodgeCooldown.value = 2500L // 2.5s cooldown

        playerCombat.update { it.copy(isEvading = true) }
        addFloatingText("EVADE ON", isDamage = false, isPlayer = true, "#E040FB")
        addBattleLog("You initiated tactical dash.")

        // Evade stays active for 600ms
        viewModelScope.launch {
            delay(600)
            playerCombat.update { it.copy(isEvading = false) }
        }
    }

    fun playerOverload() {
        val current = playerCombat.value
        if (current.energy < 100 || matchWinner.value != null) return

        // Consume energy
        playerCombat.update { it.copy(energy = 0) }

        val isCrit = Random.nextFloat() < 0.25f
        val baseDmg = Random.nextInt(25, 36)
        val dmg = if (isCrit) (baseDmg * 1.5f).toInt() else baseDmg
        
        dealDamageToOpponent(dmg, isCritical = isCrit, isUltimate = true)
        addBattleLog("You unleashed ULTRA OVERLOAD!")
    }

    // COMBAT ENGINE TIMED LOOP
    private fun startCombatEngine(opponent: OpponentProfile, isElite: Boolean) {
        combatEngineJob?.cancel()
        combatEngineJob = viewModelScope.launch {
            val tickRateMs = 100L
            var timeElapsedMs = 0L

            // Opponent AI action state variables
            var opponentStrikeCooldown = 0L
            var opponentShieldCooldown = 0L
            var opponentUltimateCooldown = 0L
            var opponentEvadingDuration = 0L

            while (matchWinner.value == null) {
                delay(tickRateMs)
                timeElapsedMs += tickRateMs

                // 1. Tick player cooldowns
                playerStrikeCooldown.update { (it - tickRateMs).coerceAtLeast(0) }
                playerShieldCooldown.update { (it - tickRateMs).coerceAtLeast(0) }
                playerDodgeCooldown.update { (it - tickRateMs).coerceAtLeast(0) }

                // 2. Shield Decay
                if (timeElapsedMs % 1000 == 0L) {
                    playerCombat.update { it.copy(shield = (it.shield - 5).coerceAtLeast(0)) }
                    opponentCombat.update { it.copy(shield = (it.shield - 5).coerceAtLeast(0)) }
                }

                // 3. Tick opponent AI state
                opponentStrikeCooldown = (opponentStrikeCooldown - tickRateMs).coerceAtLeast(0)
                opponentShieldCooldown = (opponentShieldCooldown - tickRateMs).coerceAtLeast(0)
                opponentUltimateCooldown = (opponentUltimateCooldown - tickRateMs).coerceAtLeast(0)
                if (opponentEvadingDuration > 0) {
                    opponentEvadingDuration -= tickRateMs
                    if (opponentEvadingDuration <= 0) {
                        opponentCombat.update { it.copy(isEvading = false) }
                    }
                }

                // 4. Opponent AI decision block
                val pState = playerCombat.value
                val oState = opponentCombat.value

                // Simple clever PvP opponent state machine
                if (oState.hp > 0 && pState.hp > 0) {
                    when {
                        // AI Ultimate - triggers if energy is 100%
                        oState.energy >= 100 && opponentUltimateCooldown <= 0 -> {
                            opponentCombat.update { it.copy(energy = 0) }
                            opponentUltimateCooldown = 4000L

                            // Ultimate damage
                            val dmg = opponent.specialAttackPower
                            dealDamageToPlayer(dmg, isCritical = false, isUltimate = true, name = opponent.specialAttackName)
                        }

                        // AI Shield - triggers if HP is low or Player has high energy
                        pState.energy >= 80 && oState.shield < 10 && opponentShieldCooldown <= 0 -> {
                            opponentShieldCooldown = 3000L
                            opponentCombat.update { it.copy(shield = (it.shield + 20).coerceAtMost(50)) }
                            addFloatingText("SHIELD +20", isDamage = false, isPlayer = false, "#00E5FF")
                            addBattleLog("${opponent.name} raised Nanotech Fortification.")
                        }

                        // AI Strike
                        opponentStrikeCooldown <= 0 -> {
                            opponentStrikeCooldown = if (isElite) 1200L else 1500L // Elite boss strikes faster!
                            val dmg = if (isElite) Random.nextInt(10, 16) else Random.nextInt(7, 12)
                            val isCrit = Random.nextFloat() < (if (isElite) 0.20f else 0.10f)
                            dealDamageToPlayer(dmg, isCritical = isCrit)
                            
                            // Charge AI energy
                            opponentCombat.update { it.copy(energy = (it.energy + 15).coerceAtMost(100)) }
                        }
                    }
                }
            }
        }

        // Periodic Gemini Opponent Banter (every 5-6 seconds)
        banterJob?.cancel()
        banterJob = viewModelScope.launch {
            while (matchWinner.value == null) {
                delay(6000)
                val pState = playerCombat.value
                val oState = opponentCombat.value
                val text = geminiService.generateTrashTalk(opponent.name, opponent.combatClass, pState.hp, oState.hp)
                opponentBanter.value = text
            }
        }
    }

    private fun dealDamageToOpponent(rawDmg: Int, isCritical: Boolean, isUltimate: Boolean = false) {
        val current = opponentCombat.value
        if (current.isEvading) {
            addFloatingText("EVADED!", isDamage = false, isPlayer = false, "#E040FB")
            addBattleLog("${activeOpponent.value?.name ?: "Opponent"} dodged your strike!")
            return
        }

        var dmg = rawDmg
        var isShielded = false
        if (current.shield > 0 && !isUltimate) { // Ultimates pierce shield
            isShielded = true
            val absorbed = (dmg * 0.7f).toInt().coerceAtMost(current.shield)
            opponentCombat.update { it.copy(shield = (it.shield - absorbed).coerceAtLeast(0)) }
            dmg -= absorbed
        }

        val finalDmg = dmg.coerceAtLeast(1)
        opponentCombat.update { it.copy(hp = (it.hp - finalDmg).coerceAtLeast(0)) }

        val col = if (isUltimate) "#FF9100" else if (isCritical) "#FF1744" else "#E0E0E0"
        val label = if (isUltimate) "ULTRA -$finalDmg" else if (isCritical) "CRIT -$finalDmg" else "-$finalDmg"
        addFloatingText(label, isDamage = true, isPlayer = false, col)

        if (isUltimate) {
            addBattleLog("Direct hit with Overlord blast! Dealt $finalDmg pierce damage.")
        } else if (isShielded) {
            addBattleLog("Opponent shield absorbed damage. Dealt $finalDmg.")
        } else {
            addBattleLog("Strike landed. Dealt $finalDmg.")
        }

        checkBattleEnded()
    }

    private fun dealDamageToPlayer(rawDmg: Int, isCritical: Boolean, isUltimate: Boolean = false, name: String = "") {
        val current = playerCombat.value
        if (current.isEvading) {
            addFloatingText("EVADED!", isDamage = false, isPlayer = true, "#E040FB")
            addBattleLog("You dodged incoming attack!")
            return
        }

        var dmg = rawDmg
        var isShielded = false
        if (current.shield > 0 && !isUltimate) {
            isShielded = true
            val absorbed = (dmg * 0.7f).toInt().coerceAtMost(current.shield)
            playerCombat.update { it.copy(shield = (it.shield - absorbed).coerceAtLeast(0)) }
            dmg -= absorbed
        }

        val finalDmg = dmg.coerceAtLeast(1)
        playerCombat.update { it.copy(hp = (it.hp - finalDmg).coerceAtLeast(0)) }

        val col = if (isUltimate) "#FF3D00" else if (isCritical) "#FF1744" else "#EEEEEE"
        val label = if (isUltimate) "OVERLOAD -$finalDmg" else if (isCritical) "CRIT -$finalDmg" else "-$finalDmg"
        addFloatingText(label, isDamage = true, isPlayer = true, col)

        if (isUltimate) {
            addBattleLog("${activeOpponent.value?.name} unleashed Ultimate: $name! Dealt $finalDmg pierce!")
        } else if (isShielded) {
            addBattleLog("Your Energy Shield absorbed part of strike. Sustained $finalDmg.")
        } else {
            addBattleLog("You took strike. Sustained $finalDmg.")
        }

        checkBattleEnded()
    }

    private fun checkBattleEnded() {
        val p = playerCombat.value
        val o = opponentCombat.value

        if (p.hp <= 0 && o.hp <= 0) {
            matchWinner.value = "DRAW"
            endMatch()
        } else if (p.hp <= 0) {
            matchWinner.value = "OPPONENT"
            endMatch()
        } else if (o.hp <= 0) {
            matchWinner.value = "PLAYER"
            endMatch()
        }
    }

    private fun endMatch() {
        combatEngineJob?.cancel()
        banterJob?.cancel()
        teammateChatSimulationJob?.cancel()

        val p = playerCombat.value
        val o = opponentCombat.value

        val winner = matchWinner.value ?: "DRAW"
        val opponent = activeOpponent.value ?: return

        // Calculate Elo change
        val ratingChange = when (winner) {
            "PLAYER" -> Random.nextInt(22, 28)
            "OPPONENT" -> -Random.nextInt(15, 20)
            else -> 0
        }
        finalRatingChange.value = ratingChange

        showMatchEndDialog.value = true

        // 1. Save match history record in Room
        viewModelScope.launch {
            val record = MatchHistory(
                opponentName = opponent.name,
                opponentAvatar = opponent.avatar,
                opponentClass = opponent.combatClass,
                isElite = opponent.avatar == "avatar_elite_boss",
                result = winner,
                ratingChange = ratingChange,
                durationSeconds = Random.nextInt(15, 45),
                playerDamageDealt = 100 - o.hp,
                opponentDamageDealt = 100 - p.hp
            )
            repository.addMatchRecord(record)

            // 2. Fetch Gemini Dynamic Combat Review
            fetchCoachingFeedback()
        }
    }

    private fun fetchCoachingFeedback() {
        isCoachingLoading.value = true
        viewModelScope.launch {
            val logs = battleLogs.value.takeLast(10)
            val feedback = geminiService.generateCombatCoaching(logs)
            coachingFeedback.value = feedback
            isCoachingLoading.value = false
        }
    }

    fun closeMatchEndDialog() {
        showMatchEndDialog.value = false
        navigateTo(ScreenState.MainDashboard)
    }

    // Floating combat numbers utils
    private fun addFloatingText(text: String, isDamage: Boolean, isPlayer: Boolean, colorHex: String) {
        val entry = FloatingText(
            id = System.nanoTime(),
            text = text,
            isDamage = isDamage,
            isPlayer = isPlayer,
            colorHex = colorHex
        )
        floatingTexts.update { it + entry }

        // Remove floating text after 1.2 seconds
        viewModelScope.launch {
            delay(1200)
            floatingTexts.update { list -> list.filter { it.id != entry.id } }
        }
    }

    private fun addBattleLog(msg: String) {
        battleLogs.update { it + msg }
    }

    // --- CHAT IMPLEMENTATION ---
    fun sendGlobalMessage(content: String) {
        val currentProfile = playerProfile.value ?: return
        val msg = ChatMessage(
            senderName = currentProfile.name,
            senderAvatar = currentProfile.chosenAvatar,
            content = content,
            channel = ChatChannel.GLOBAL
        )
        _globalMessages.update { (it + msg).takeLast(50) }
        triggerSimulatedGlobalResponse(content)
    }

    fun sendTeamMessage(content: String) {
        val currentProfile = playerProfile.value ?: return
        val msg = ChatMessage(
            senderName = currentProfile.name,
            senderAvatar = currentProfile.chosenAvatar,
            content = content,
            channel = ChatChannel.TEAM
        )
        _teamMessages.update { (it + msg).takeLast(30) }
        triggerSimulatedTeamResponse(content)
    }

    fun toggleMutePlayer(playerName: String) {
        _mutedPlayers.update { current ->
            if (current.contains(playerName)) {
                current - playerName
            } else {
                current + playerName
            }
        }
    }

    private fun initializeGlobalMessages() {
        val historical = listOf(
            ChatMessage(
                senderName = "GridRunner",
                senderAvatar = "avatar_neon_cyber",
                content = "What's the best combat class for dealing with co-op bosses?",
                channel = ChatChannel.GLOBAL,
                timestamp = System.currentTimeMillis() - 600000
            ),
            ChatMessage(
                senderName = "Aegis_X",
                senderAvatar = "avatar_cyborg_tank",
                content = "Definitely Aegis! The shield absorption holds off any boss strikes easily.",
                channel = ChatChannel.GLOBAL,
                timestamp = System.currentTimeMillis() - 500000
            ),
            ChatMessage(
                senderName = "QuantumRanger",
                senderAvatar = "avatar_space_ranger",
                content = "Stryker can burst them down much faster though. High-risk, high-reward.",
                channel = ChatChannel.GLOBAL,
                timestamp = System.currentTimeMillis() - 400000
            ),
            ChatMessage(
                senderName = "BitSlayer",
                senderAvatar = "avatar_cyber_fist",
                content = "Has anyone beaten the Gemini Elite Coach today? ELO requirements are tough.",
                channel = ChatChannel.GLOBAL,
                timestamp = System.currentTimeMillis() - 300000
            )
        )
        _globalMessages.value = historical
    }

    private fun startGlobalChatSimulation() {
        globalChatSimulationJob?.cancel()
        globalChatSimulationJob = viewModelScope.launch {
            val usernames = listOf("VoltScythe", "ApexSentinel", "Neuro_Wrecker", "Byte_Slayer", "ShadowEdge", "PixelWarrior", "ProxyGamer", "CyberDiva", "Glitch_Phantom", "CodeViper")
            val avatars = listOf("avatar_neon_cyber", "avatar_cyborg_tank", "avatar_cyber_fist", "avatar_space_ranger")
            val messages = listOf(
                "Stryker class is definitely the best for ELO climbing.",
                "Anyone managed to beat Apex-V yet? That boss is brutal!",
                "Chrono evasion cooldown is so fast, love it.",
                "Just hit 1400 ELO, let's go!",
                "Can we customize avatar borders? Mine looks default.",
                "Aegis shield is a lifesaver against Stryker ult.",
                "Who wants to queue for a 2v2?",
                "Is the leaderboard refreshing every hour?",
                "That last match was intense, down to 5 HP!",
                "Chrono Overload is the coolest looking special attack.",
                "Elite Boss yields double XP, highly recommend farming it.",
                "NetRunner_X is a pretty cool name.",
                "Need a guild to join, any top 10 recruiting?",
                "Aegis Phalanx shockwave is hyper-shielding.",
                "Lobby latency is super low today, 20ms flat."
            )
            while (true) {
                delay(Random.nextLong(10000, 18000))
                val sender = usernames.random()
                if (!_mutedPlayers.value.contains(sender)) {
                    val msg = ChatMessage(
                        senderName = sender,
                        senderAvatar = avatars.random(),
                        content = messages.random(),
                        channel = ChatChannel.GLOBAL
                    )
                    _globalMessages.update { (it + msg).takeLast(50) }
                }
            }
        }
    }

    private fun triggerSimulatedGlobalResponse(userMsg: String) {
        viewModelScope.launch {
            delay(Random.nextLong(1500, 3500))
            val repliers = listOf("ProxyGamer", "Neuro_Wrecker", "CodeViper", "CyberDiva")
            val sender = repliers.random()
            if (!_mutedPlayers.value.contains(sender)) {
                val content = when {
                    userMsg.contains("stryker", ignoreCase = true) || userMsg.contains("class", ignoreCase = true) -> {
                        listOf(
                            "Agreed! Stryker's damage multiplier is insane.",
                            "Stryker is nice, but I prefer Aegis' defense.",
                            "Yeah, Stryker dominates the leaderboard."
                        ).random()
                    }
                    userMsg.contains("boss", ignoreCase = true) || userMsg.contains("elite", ignoreCase = true) || userMsg.contains("gemini", ignoreCase = true) -> {
                        listOf(
                            "The Gemini reviews are actually super helpful to improve.",
                            "Yes! The elite boss AI strikes like crazy.",
                            "Farming the elite coach is the meta right now."
                        ).random()
                    }
                    userMsg.contains("elo", ignoreCase = true) || userMsg.contains("rank", ignoreCase = true) -> {
                        listOf(
                            "Elo climbing is getting competitive.",
                            "What is your current rating? I'm trying to cross 1200.",
                            "Nice! Keep pushing the ladder."
                        ).random()
                    }
                    else -> {
                        listOf(
                            "Well said! Cyber battle onwards.",
                            "Interesting point, netrunner.",
                            "Let's go! Matchmaking is super fast.",
                            "Anyone up for a dual strike?"
                        ).random()
                    }
                }
                val msg = ChatMessage(
                    senderName = sender,
                    content = content,
                    channel = ChatChannel.GLOBAL
                )
                _globalMessages.update { (it + msg).takeLast(50) }
            }
        }
    }

    private fun startTeammateChatSimulation(opponent: OpponentProfile) {
        teammateChatSimulationJob?.cancel()
        teammateChatSimulationJob = viewModelScope.launch {
            val teammates = listOf(
                Pair("VoltScythe", "avatar_neon_cyber"),
                Pair("ApexSentinel", "avatar_cyborg_tank")
            )
            while (matchWinner.value == null) {
                delay(Random.nextLong(6000, 10000))
                if (matchWinner.value != null) break
                
                val pState = playerCombat.value
                val oState = opponentCombat.value
                val activeTeammate = teammates.random()
                val sender = activeTeammate.first
                
                if (!_mutedPlayers.value.contains(sender)) {
                    val content = when {
                        oState.hp < 30 -> {
                            listOf(
                                "Opponent is extremely low! Finish them!",
                                "Drop the ultimate now, we've almost won!",
                                "Keep striking, victory is right there!"
                            ).random()
                        }
                        pState.hp < 35 -> {
                            listOf(
                                "Watch your health! Put up a shield!",
                                "Dodge the incoming strikes!",
                                "Careful! Use Evade to avoid critical hits."
                            ).random()
                        }
                        pState.shield > 30 -> {
                            listOf(
                                "Excellent shield coverage!",
                                "Nice barrier, absorb their strikes!",
                                "Now is our chance, strike while shielded!"
                            ).random()
                        }
                        else -> {
                            listOf(
                                "We are coordinating well, stay focused.",
                                "Keep your eye on the energy charge meter.",
                                "Solid ping, latency is perfect.",
                                "Our combat configuration is optimal.",
                                "Aether arena dominance is ours!",
                                "Watch out for their ultimate charge!"
                            ).random()
                        }
                    }
                    
                    val msg = ChatMessage(
                        senderName = sender,
                        senderAvatar = activeTeammate.second,
                        content = content,
                        channel = ChatChannel.TEAM
                    )
                    _teamMessages.update { (it + msg).takeLast(30) }
                }
            }
        }
    }

    private fun triggerSimulatedTeamResponse(userMsg: String) {
        viewModelScope.launch {
            delay(Random.nextLong(1200, 2500))
            if (matchWinner.value != null) return@launch
            val teammate = listOf(
                Pair("VoltScythe", "avatar_neon_cyber"),
                Pair("ApexSentinel", "avatar_cyborg_tank")
            ).random()
            
            val sender = teammate.first
            if (!_mutedPlayers.value.contains(sender)) {
                val content = when {
                    userMsg.contains("shield", ignoreCase = true) || userMsg.contains("defense", ignoreCase = true) -> {
                        listOf(
                            "Shielding up! Defense active.",
                            "Got you covered, barrier is solid.",
                            "Yeah, defense is key right here."
                        ).random()
                    }
                    userMsg.contains("strike", ignoreCase = true) || userMsg.contains("attack", ignoreCase = true) || userMsg.contains("hit", ignoreCase = true) -> {
                        listOf(
                            "Let's focus fire! Strike!",
                            "Lining up my plasma bursts!",
                            "Maximize DPS, go go go!"
                        ).random()
                    }
                    userMsg.contains("ult", ignoreCase = true) || userMsg.contains("ultimate", ignoreCase = true) || userMsg.contains("overload", ignoreCase = true) -> {
                        listOf(
                            "Ready! Launch it on my mark.",
                            "Overload charging up!",
                            "Unleash the storm!"
                        ).random()
                    }
                    else -> {
                        listOf(
                            "Copy that!",
                            "Roger! Proceeding with tactical plan.",
                            "Got it! Let's win this match."
                        ).random()
                    }
                }
                
                val msg = ChatMessage(
                    senderName = sender,
                    senderAvatar = teammate.second,
                    content = content,
                    channel = ChatChannel.TEAM
                )
                _teamMessages.update { (it + msg).takeLast(30) }
            }
        }
    }
}
