package logic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import models.*

class GameIntegrationTest {

    @Test
    fun `integration test full game session initialization`() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Тест Игрок 1")
        val p2 = Player("2", "Тест Игрок 2")
        val players = listOf(p1, p2)
        val trump = deck.draw()!!

        val session = GameSession(deck, table, players, GameMode.CLASSIC, trump)

        assertEquals(GameResult.InProgress, session.getResult())
        assertEquals(2, session.players.size)
        assertEquals(0, session.attackerIdx)
    }

    @Test
    fun testFullSessionAndTableIntegration() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Атакующий")
        val p2 = Player("2", "Защищающийся")
        val players = listOf(p1, p2)

        val trump = Card(Suit.DIAMONDS, Rank.ACE)
        val attackCard = Card(Suit.HEARTS, Rank.SEVEN)
        p1.hand.add(attackCard)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, trump)
        session.executeMove(p1, attackCard)

        assertFalse(p1.hand.contains(attackCard))
        assertTrue(table.slots.any { it.attackCard == attackCard })
        assertEquals(GameResult.InProgress, session.getResult())
    }
}
