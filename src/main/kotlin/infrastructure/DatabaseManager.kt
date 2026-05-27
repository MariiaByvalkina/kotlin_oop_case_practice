package infrastructure

import logic.*
import models.*
import java.sql.DriverManager
import java.time.LocalDateTime

data class GameHistoryRecord(val id: Int, val date: String, val result: String, val players: String)

class DatabaseManager(dbPath: String = "durak_main.db") {
    private val url = "jdbc:sqlite:$dbPath"

    init {
        val connection = DriverManager.getConnection(url)
        val statement = connection.createStatement()

        statement.execute("CREATE TABLE IF NOT EXISTS player_stats (id TEXT PRIMARY KEY, wins INTEGER DEFAULT 0, games_played INTEGER DEFAULT 0)")
        statement.execute("CREATE TABLE IF NOT EXISTS game_history (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, result_text TEXT, players_list TEXT)")

        statement.close()
        connection.close()
    }

    fun saveSession(session: GameSession) {
        val connection = DriverManager.getConnection(url)

        try {
            val gameResult = session.getResult()

            for (player in session.players) {
                var isWinner = false
                if (gameResult is GameResult.Winner) {
                    // Исправлено: используем правильное свойство
                    if (gameResult.player.id == player.id) {
                        isWinner = true
                    }
                }

                val checkStatement = connection.prepareStatement("SELECT * FROM player_stats WHERE id = ?")
                checkStatement.setString(1, player.id)
                val resultSet = checkStatement.executeQuery()

                if (resultSet.next()) {
                    val currentWins = resultSet.getInt("wins")
                    val currentGames = resultSet.getInt("games_played")

                    val newWins = if (isWinner) currentWins + 1 else currentWins

                    val updateStatement = connection.prepareStatement("UPDATE player_stats SET wins = ?, games_played = ? WHERE id = ?")
                    updateStatement.setInt(1, newWins)
                    updateStatement.setInt(2, currentGames + 1)
                    updateStatement.setString(3, player.id)
                    updateStatement.executeUpdate()
                    updateStatement.close()
                } else {
                    val initialWins = if (isWinner) 1 else 0
                    val insertStatement = connection.prepareStatement("INSERT INTO player_stats VALUES (?, ?, ?)")
                    insertStatement.setString(1, player.id)
                    insertStatement.setInt(2, initialWins)
                    insertStatement.setInt(3, 1)
                    insertStatement.executeUpdate()
                    insertStatement.close()
                }
                resultSet.close()
                checkStatement.close()
            }

            val playerNamesString = session.players.joinToString(", ") { it.name }

            val resultText = gameResult.toString()
            val currentDate = LocalDateTime.now().toString()

            val historyStatement = connection.prepareStatement("INSERT INTO game_history (date, result_text, players_list) VALUES (?, ?, ?)")
            historyStatement.setString(1, currentDate)
            historyStatement.setString(2, resultText)
            historyStatement.setString(3, playerNamesString)
            historyStatement.executeUpdate()
            historyStatement.close()

        } finally {
            connection.close()
        }
    }

    fun getHistory(): List<GameHistoryRecord> {
        val historyList = mutableListOf<GameHistoryRecord>()
        val connection = DriverManager.getConnection(url)

        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM game_history ORDER BY id DESC")

            while (resultSet.next()) {
                historyList.add(
                    GameHistoryRecord(
                        resultSet.getInt("id"),
                        resultSet.getString("date"),
                        resultSet.getString("result_text"),
                        resultSet.getString("players_list")
                    )
                )
            }
            resultSet.close()
            statement.close()
        } finally {
            connection.close()
        }
        return historyList
    }

    fun getGlobalRanking(): List<PlayerRecord> {
        val rankingList = mutableListOf<PlayerRecord>()
        val connection = DriverManager.getConnection(url)

        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM player_stats ORDER BY wins DESC")

            while (resultSet.next()) {
                val playerId = resultSet.getString("id")
                val winsCount = resultSet.getInt("wins")
                val gamesCount = resultSet.getInt("games_played")
                rankingList.add(PlayerRecord(playerId, winsCount, gamesCount))
            }
            resultSet.close()
            statement.close()
        } finally {
            connection.close()
        }
        return rankingList
    }
}

data class PlayerRecord(val playerId: String, val wins: Int, val gamesPlayed: Int)