package logic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
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

        val session = GameSession(deck, table, players, GameMode.CLASSIC, trump.suit)

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

        val session = GameSession(deck, table, players, GameMode.CLASSIC, trump.suit)
        session.executeMove(p1, attackCard)

        assertFalse(p1.hand.contains(attackCard))
        assertTrue(table.slots.any { it.attackCard == attackCard })
        assertEquals(GameResult.InProgress, session.getResult())
    }

    @Test
    fun testIntegrationDeckAndTableWithPlayerHand() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")

        val initialDeckSize = deck.remaining()
        val card1 = deck.draw()!!
        val card2 = deck.draw()!!

        p1.hand.add(card1)
        p2.hand.add(card2)

        assertEquals(initialDeckSize - 2, deck.remaining())
        assertEquals(1, p1.hand.size)

        table.addAttack(card1)
        p1.hand.remove(card1)

        assertEquals(1, table.slots.size)
        assertTrue(p1.hand.isEmpty())

        val isBeaten = table.defendCard(card1, card2, Suit.DIAMONDS)
        if (isBeaten) {
            p2.hand.remove(card2)
            assertTrue(table.slots.first().isBeaten())
        }
    }

    @Test
    fun testIntegrationTableValidationInSession() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Атакующий")
        val p2 = Player("2", "Защитник")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.SPADES)

        val card = Card(Suit.HEARTS, Rank.NINE)
        p1.hand.add(card)

        session.executeMove(p1, card)
        assertEquals(1, table.slots.size)

        val invalidCard = Card(Suit.CLUBS, Rank.ACE)
        p1.hand.add(invalidCard)

        assertThrows(IllegalArgumentException::class.java) {
            session.executeMove(p1, invalidCard)
        }
    }

    @Test
    fun testSystemFullClassicRoundSimulation() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Атакующий")
        val p2 = Player("2", "Защитник")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.CLUBS)

        val card1 = Card(Suit.HEARTS, Rank.SIX)
        val card2 = Card(Suit.HEARTS, Rank.TEN)
        p1.hand.add(card1)
        p2.hand.add(card2)

        session.executeMove(p1, card1)
        assertEquals(1, table.slots.size)
        assertFalse(p1.hand.contains(card1))

        session.executeMove(p2, card2)
        assertTrue(table.slots.first().isBeaten())
        assertFalse(p2.hand.contains(card2))

        session.finishRound()
        assertTrue(table.slots.isEmpty())
        assertEquals(p2, session.getCurrentAttacker())
        assertTrue(p1.hand.size >= 6)
    }

    @Test
    fun testSystemDrawGameSimulation() {
        val deck = Deck()
        while (deck.remaining() > 0) {
            deck.draw()
        }

        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.SPADES)
        assertEquals(GameResult.Draw, session.getResult())
    }
}