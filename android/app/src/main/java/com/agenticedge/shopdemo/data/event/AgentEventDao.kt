package com.agenticedge.shopdemo.data.event

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AgentEventDao {

    @Insert
    suspend fun insert(event: AgentEvent)

    @Query("SELECT * FROM agent_events WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun eventsForSession(sessionId: String): List<AgentEvent>

    @Query("SELECT * FROM agent_events ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun recentEvents(limit: Int = 200): List<AgentEvent>

    @Query("SELECT COUNT(*) FROM agent_events WHERE sessionId = :sessionId AND type = :type")
    suspend fun countByType(sessionId: String, type: String): Int

    @Query("DELETE FROM agent_events")
    suspend fun clearAll()
}
