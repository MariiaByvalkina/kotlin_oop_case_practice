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
        val gameResult = session.getResult()

        for (player in session.players) {
            var isWinner = false
            if (gameResult is GameResult.Winner) {
                if (gameResult.player.id == player.id) {
                    isWinner = true
                }
            }

            val checkStatement = connection.createStatement()
            val resultSet = checkStatement.executeQuery("SELECT * FROM player_stats WHERE id = '${player.id}'")

            if (resultSet.next()) {
                val currentWins = resultSet.getInt("wins")
                val currentGames = resultSet.getInt("games_played")

                var newWins = currentWins
                if (isWinner) {
                    newWins = currentWins + 1
                }

                val updateStatement = connection.createStatement()
                updateStatement.executeUpdate("UPDATE player_stats SET wins = $newWins, games_played = ${currentGames + 1} WHERE id = '${player.id}'")
                updateStatement.close()
            } else {
                val insertStatement = connection.createStatement()
                val initialWins = if (isWinner) 1 else 0
                insertStatement.executeUpdate("INSERT INTO player_stats VALUES ('${player.id}', $initialWins, 1)")
                insertStatement.close()
            }
            resultSet.close()
            checkStatement.close()
        }

        var playerNamesString = ""
        for (i in 0 until session.players.size) {
            playerNamesString += session.players[i].name
            if (i < session.players.size - 1) {
                playerNamesString += ", "
            }
        }

        val historyStatement = connection.createStatement()
        val currentDate = LocalDateTime.now().toString()
        historyStatement.executeUpdate("INSERT INTO game_history (date, result_text, players_list) VALUES ('$currentDate', '$gameResult', '$playerNamesString')")
        historyStatement.close()

        connection.close()
    }

    fun getHistory(): List<GameHistoryRecord> {
        val historyList = mutableListOf<GameHistoryRecord>()
        val connection = DriverManager.getConnection(url)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM game_history")

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
        connection.close()
        return historyList
    }

    fun getGlobalRanking(): List<PlayerRecord> {
        val rankingList = mutableListOf<PlayerRecord>()
        val connection = DriverManager.getConnection(url)
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
        connection.close()
        return rankingList
    }
}
