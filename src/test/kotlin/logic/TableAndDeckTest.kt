package logic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import models.*

class TableAndDeckTest {
    @Test
    fun `deck draw should decrease remaining cards`() {
        val deck = Deck()
        val initialSize = deck.remaining()
        deck.draw()
        assertEquals(initialSize - 1, deck.remaining())
    }

    @Test
    fun `deck shuffle should preserve total card count`() {
        val deck = Deck()
        val initialSize = deck.remaining()
        deck.shuffle()
        assertEquals(initialSize, deck.remaining())
    }

    @Test
    fun `table canAdd should allow card with rank already on table`() {
        val table = Table()
        table.addAttack(Card(Suit.HEARTS, Rank.SIX))

        val validCard = Card(Suit.SPADES, Rank.SIX)
        val invalidCard = Card(Suit.DIAMONDS, Rank.ACE)

        assertTrue(table.canAdd(validCard, 6))
        assertFalse(table.canAdd(invalidCard, 6))
    }

    @Test
    fun `table defendCard should enforce dueling rules`() {
        val table = Table()
        val attack = Card(Suit.HEARTS, Rank.SEVEN)
        table.addAttack(attack)

        val validDefense = Card(Suit.HEARTS, Rank.TEN)
        val invalidDefense = Card(Suit.CLUBS, Rank.NINE)

        assertTrue(table.defendCard(attack, validDefense, Suit.SPADES))
        assertFalse(table.defendCard(attack, invalidDefense, Suit.SPADES))
    }

    @Test
    fun `table canTransfer should validate clean table condition`() {
        val table = Table()
        val attack = Card(Suit.HEARTS, Rank.EIGHT)

        assertFalse(table.canTransfer(attack, 6))

        table.addAttack(attack)
        assertTrue(table.canTransfer(Card(Suit.SPADES, Rank.EIGHT), 6))
    }
}
