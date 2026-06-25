package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfile)

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    fun getMatchHistory(): Flow<List<MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchHistory(match: MatchHistory)

    @Query("SELECT * FROM leaderboard_entry ORDER BY rankRating DESC")
    fun getLeaderboardEntries(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntry>)

    @Query("DELETE FROM leaderboard_entry")
    suspend fun clearLeaderboard()

    @Transaction
    suspend fun refreshLeaderboard(entries: List<LeaderboardEntry>) {
        clearLeaderboard()
        insertLeaderboardEntries(entries)
    }
}
