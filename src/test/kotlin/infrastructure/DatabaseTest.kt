package infrastructure

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import models.*
import logic.*
import java.io.File

class DatabaseManagerTest {

    private val tempDbPath = "durak_test_temp.db"
    private lateinit var db: DatabaseManager

    @BeforeEach
    fun setUp() {
        File(tempDbPath).delete()
        db = DatabaseManager(tempDbPath)
    }

    @AfterEach
    fun tearDown() {
        File(tempDbPath).delete()
    }

    @Test
    fun testDatabaseCreation() {
        val testDbName = "test_create.db"
        File(testDbName).delete()

        DatabaseManager(testDbName)
        val file = File(testDbName)

        assertTrue(file.exists())
        file.delete()
    }

    @Test
    fun testRankingEmpty() {
        val testDbName = "test_rank.db"
        File(testDbName).delete()

        val uniqueDb = DatabaseManager(testDbName)
        val ranking = uniqueDb.getGlobalRanking()

        assertEquals(0, ranking.size)
        File(testDbName).delete()
    }

    @Test
    fun testRegressionStatsAccumulation() {
        val testFile = "regression_pass_${System.currentTimeMillis()}.db"
        val regressionDb = DatabaseManager(testFile)

        try {
            val p1 = Player("player_1", "Игрок 1")
            val p2 = Player("player_2", "Игрок 2")

            val deck1 = Deck()
            val table1 = Table()
            val players1 = mutableListOf(p1, p2)

            p1.hand.clear()
            p2.hand.clear()
            p1.hand.add(Card(Suit.HEARTS, Rank.ACE))

            while (deck1.remaining() > 0) {
                deck1.draw()
            }

            val session1 = GameSession(deck1, table1, players1, GameMode.CLASSIC, Suit.HEARTS)
            regressionDb.saveSession(session1)

            val deck2 = Deck()
            val table2 = Table()
            val players2 = mutableListOf(p1, p2)

            p1.hand.clear()
            p2.hand.clear()
            p1.hand.add(Card(Suit.SPADES, Rank.KING))

            while (deck2.remaining() > 0) {
                deck2.draw()
            }

            val session2 = GameSession(deck2, table2, players2, GameMode.CLASSIC, Suit.SPADES)
            regressionDb.saveSession(session2)

            val ranking = regressionDb.getGlobalRanking()

            assertEquals(2, ranking.size)

            val player1Stats = ranking.find { it.playerId == "player_1" }
            assertNotNull(player1Stats)
            assertTrue(player1Stats!!.gamesPlayed >= 2)
            assertTrue(player1Stats.wins >= 2)

        } finally {
            File(testFile).delete()
        }
    }

    @Test
    fun testIntegrationEngineWithDatabase() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Атакующий")
        val p2 = Player("2", "Защитник")
        val players = mutableListOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.DIAMONDS)
        val card1 = Card(Suit.HEARTS, Rank.SIX)
        p1.hand.add(card1)

        try {
            session.executeMove(p1, card1)
        } catch (e: Exception) {
        }

        db.saveSession(session)

        val history = db.getHistory()
        assertEquals(1, history.size)
        assertTrue(history.first().players.contains("Атакующий"))
    }

    @Test
    fun testSystemDrawGameSessionSavedToDatabase() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = mutableListOf(p1, p2)

        while (deck.remaining() > 0) {
            deck.draw()
        }

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.CLUBS)
        db.saveSession(session)

        val ranking = db.getGlobalRanking()
        assertEquals(2, ranking.size)
        assertEquals(0, ranking[0].wins)
        assertEquals(0, ranking[1].wins)
    }
}