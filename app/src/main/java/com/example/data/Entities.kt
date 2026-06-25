package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1, // Only 1 local player profile
    val name: String,
    val rankRating: Int = 1000, // Elo rating starting at 1000
    val wins: Int = 0,
    val losses: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val chosenAvatar: String = "avatar_neon_cyber", // Key for avatar vector/drawing
    val chosenClass: String = "Stryker", // Stryker, Aegis, Chrono
    val registrationTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "match_history")
data class MatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val opponentName: String,
    val opponentAvatar: String,
    val opponentClass: String,
    val isElite: Boolean = false,
    val result: String, // "WIN", "LOSS", "DRAW"
    val ratingChange: Int,
    val durationSeconds: Int,
    val playerDamageDealt: Int,
    val opponentDamageDealt: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard_entry")
data class LeaderboardEntry(
    @PrimaryKey val name: String,
    val rankRating: Int,
    val rank: Int,
    val chosenClass: String,
    val chosenAvatar: String,
    val wins: Int,
    val losses: Int,
    val statusTag: String = "CHALLENGER" // e.g., "CHALLENGER", "CHAMPION", "ELITE"
)
