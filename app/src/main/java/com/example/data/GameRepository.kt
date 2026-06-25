package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val playerDao: PlayerDao) {

    val playerProfile: Flow<PlayerProfile?> = playerDao.getPlayerProfile()
    val matchHistory: Flow<List<MatchHistory>> = playerDao.getMatchHistory()
    val leaderboardEntries: Flow<List<LeaderboardEntry>> = playerDao.getLeaderboardEntries()

    suspend fun getOrCreateProfile(defaultName: String): PlayerProfile {
        val existing = playerProfile.firstOrNull() ?: playerDao.getPlayerProfile().firstOrNull()
        if (existing != null) {
            return existing
        }
        val newProfile = PlayerProfile(
            name = defaultName,
            rankRating = 1000,
            wins = 0,
            losses = 0,
            level = 1,
            xp = 0,
            chosenAvatar = "avatar_neon_cyber",
            chosenClass = "Stryker"
        )
        playerDao.insertOrUpdateProfile(newProfile)
        
        // Also initialize leaderboard
        initializeLeaderboard()
        return newProfile
    }

    suspend fun updateProfile(profile: PlayerProfile) {
        playerDao.insertOrUpdateProfile(profile)
    }

    suspend fun addMatchRecord(match: MatchHistory) {
        playerDao.insertMatchHistory(match)
        
        // Update player stats
        val currentProfile = playerDao.getPlayerProfile().firstOrNull()
        if (currentProfile != null) {
            val xpGained = if (match.result == "WIN") 150 else 50
            var newXp = currentProfile.xp + xpGained
            var newLevel = currentProfile.level
            val xpNeeded = 100 + (newLevel * 50)
            if (newXp >= xpNeeded) {
                newXp -= xpNeeded
                newLevel += 1
            }

            val newRating = (currentProfile.rankRating + match.ratingChange).coerceAtLeast(100)
            val updatedProfile = currentProfile.copy(
                rankRating = newRating,
                wins = if (match.result == "WIN") currentProfile.wins + 1 else currentProfile.wins,
                losses = if (match.result == "LOSS") currentProfile.losses + 1 else currentProfile.losses,
                level = newLevel,
                xp = newXp
            )
            playerDao.insertOrUpdateProfile(updatedProfile)

            // Dynamic Leaderboard standing update
            updateLeaderboardWithPlayer(updatedProfile)
        }
    }

    private suspend fun initializeLeaderboard() {
        val defaultLeaderboard = listOf(
            LeaderboardEntry("Nova_Prime", 1950, 1, "Chrono", "avatar_space_ranger", 145, 82, "CHAMPION"),
            LeaderboardEntry("Viper_Shield", 1820, 2, "Aegis", "avatar_cyborg_tank", 124, 76, "ELITE"),
            LeaderboardEntry("Saber_Fist", 1710, 3, "Stryker", "avatar_cyber_fist", 98, 54, "ELITE"),
            LeaderboardEntry("Ghost_In_Aether", 1590, 4, "Chrono", "avatar_neon_cyber", 87, 49, "ELITE"),
            LeaderboardEntry("Aegis_Vanguard", 1480, 5, "Aegis", "avatar_cyborg_tank", 74, 45, "CHALLENGER"),
            LeaderboardEntry("Xenon_Rider", 1350, 6, "Stryker", "avatar_neon_cyber", 61, 38, "CHALLENGER"),
            LeaderboardEntry("Quantum_Wraith", 1210, 7, "Chrono", "avatar_space_ranger", 45, 29, "CHALLENGER"),
            LeaderboardEntry("Neural_Spectre", 1120, 8, "Stryker", "avatar_cyber_fist", 32, 21, "CHALLENGER"),
            LeaderboardEntry("Vector_Slayer", 990, 9, "Stryker", "avatar_neon_cyber", 22, 19, "CONTENDER"),
            LeaderboardEntry("Solar_Warden", 850, 10, "Aegis", "avatar_cyborg_tank", 15, 24, "RECRUIT")
        )
        playerDao.insertLeaderboardEntries(defaultLeaderboard)
    }

    private suspend fun updateLeaderboardWithPlayer(player: PlayerProfile) {
        val currentEntries = playerDao.getLeaderboardEntries().firstOrNull() ?: emptyList()
        val list = currentEntries.toMutableList()
        
        // Remove old entry of player if present
        list.removeAll { it.name == player.name }
        
        // Add new entry of player
        val status = when {
            player.rankRating >= 1800 -> "CHAMPION"
            player.rankRating >= 1500 -> "ELITE"
            player.rankRating >= 1100 -> "CHALLENGER"
            player.rankRating >= 900 -> "CONTENDER"
            else -> "RECRUIT"
        }
        
        list.add(
            LeaderboardEntry(
                name = player.name,
                rankRating = player.rankRating,
                rank = 0, // Will recalculate
                chosenClass = player.chosenClass,
                chosenAvatar = player.chosenAvatar,
                wins = player.wins,
                losses = player.losses,
                statusTag = status
            )
        )
        
        // Sort and recalculate rank
        list.sortByDescending { it.rankRating }
        val updatedList = list.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
        
        playerDao.refreshLeaderboard(updatedList)
    }
}
