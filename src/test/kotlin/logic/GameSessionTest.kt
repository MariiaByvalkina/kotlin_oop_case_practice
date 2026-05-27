package logic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import models.*

class GameSessionTest {

    @Test
    fun testInitialStateAndGetters() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.HEARTS)

        assertEquals(p1, session.getCurrentAttacker())
        assertEquals(p2, session.getCurrentDefender())
        assertEquals(GameResult.InProgress, session.getResult())
    }

    @Test
    fun testMoveRemovesCard() {
        val player = Player("1", "Test")
        val card = Card(Suit.CLUBS, Rank.TEN)
        player.hand.add(card)

        val opponent = Player("1", "opponent")
        val playersList = listOf(player, opponent)

        val session = GameSession(Deck(), Table(), playersList, GameMode.CLASSIC, card.suit)
        session.executeMove(player, card)

        assertFalse(player.hand.contains(card))
    }

    @Test
    fun testExecuteMoveAttackAndNormalDefense() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.SPADES)

        val attackCard = Card(Suit.HEARTS, Rank.SIX)
        val defenseCard = Card(Suit.HEARTS, Rank.TEN)

        p1.hand.add(attackCard)
        p2.hand.add(defenseCard)

        session.executeMove(p1, attackCard)
        assertEquals(1, table.slots.size)
        assertFalse(p1.hand.contains(attackCard))

        session.executeMove(p2, defenseCard)
        assertTrue(table.slots.first().isBeaten())
        assertFalse(p2.hand.contains(defenseCard))
    }

    @Test
    fun testExecuteMoveIllegalTurnOrderAndRules() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.SPADES)

        val card = Card(Suit.HEARTS, Rank.SEVEN)
        p2.hand.add(card)

        assertThrows(IllegalArgumentException::class.java) {
            session.executeMove(p2, card)
        }

        p2.hand.clear()
        p1.hand.add(card)
        session.executeMove(p1, card)

        val weakCard = Card(Suit.HEARTS, Rank.SIX)
        p2.hand.add(weakCard)
        assertThrows(IllegalArgumentException::class.java) {
            session.executeMove(p2, weakCard)
        }
    }

    @Test
    fun testExecuteMoveTransferableMode() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val p3 = Player("3", "Игрок 3")
        val players = listOf(p1, p2, p3)

        p1.hand.add(Card(Suit.HEARTS, Rank.EIGHT))
        p2.hand.add(Card(Suit.HEARTS, Rank.EIGHT))

        p3.hand.add(Card(Suit.CLUBS, Rank.ACE))
        p3.hand.add(Card(Suit.CLUBS, Rank.KING))
        p3.hand.add(Card(Suit.CLUBS, Rank.QUEEN))

        val session = GameSession(deck, table, players, GameMode.TRANSFERABLE, Suit.DIAMONDS)

        val card1 = p1.hand.first()
        session.executeMove(p1, card1)

        val card2 = p2.hand.first()
        session.executeMove(p2, card2)

        assertEquals(2, table.slots.size)
        assertEquals(p3, session.getCurrentDefender())
    }

    @Test
    fun testNextTurnAndFinishRoundAndRefill() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.HEARTS)

        val card = Card(Suit.CLUBS, Rank.ACE)
        p1.hand.add(card)
        session.executeMove(p1, card)

        session.finishRound()
        assertTrue(table.slots.isEmpty())
        assertEquals(p2, session.getCurrentAttacker())
        assertEquals(6, p1.hand.size)
    }

    @Test
    fun testTakeCardsMethod() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.HEARTS)

        val card = Card(Suit.CLUBS, Rank.NINE)
        p1.hand.add(card)
        session.executeMove(p1, card)

        session.takeCards()
        assertTrue(table.slots.isEmpty())
        assertTrue(p2.hand.contains(card))
        assertEquals(p1, session.getCurrentAttacker())
    }

    @Test
    fun testGameResultStates() {
        val deck = Deck()
        val table = Table()
        val p1 = Player("1", "Игрок 1")
        val p2 = Player("2", "Игрок 2")
        val players = listOf(p1, p2)

        while (deck.remaining() > 0) {
            deck.draw()
        }

        val session = GameSession(deck, table, players, GameMode.CLASSIC, Suit.HEARTS)
        assertEquals(GameResult.Draw, session.getResult())

        p1.hand.add(Card(Suit.CLUBS, Rank.ACE))
        val winnerResult = session.getResult()
        assertTrue(winnerResult is GameResult.Winner)
        assertEquals(p1, (winnerResult as GameResult.Winner).player)
    }
}
