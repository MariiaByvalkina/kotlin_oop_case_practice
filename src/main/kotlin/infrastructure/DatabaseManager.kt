package infrastructure

import logic.*
import models.*

data class PlayerRecord(val playerId: String, var wins: Int = 0, var gamesPlayed: Int = 0)

class DatabaseManager {
    private val storage = mutableMapOf<String, PlayerRecord>()

    fun getPlayerStats(playerId: String) : PlayerRecord {
        return storage.getOrPut(playerId){PlayerRecord(playerId)}
    }

    fun saveSession(session: GameSession) {
        val result = session.getResult()
        session.players.forEach { player ->
            val record = getPlayerStats(player.id)
            record.gamesPlayed++
        }

        if (result is GameResult.Winner) {
            val winnerRecord = getPlayerStats(result.player.id)
            winnerRecord.wins++
        }
    }

    fun getGlobalRanking() : List<PlayerRecord> {
        return storage.values.sortedByDescending{it.wins}
    }
}