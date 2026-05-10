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
    fun `table canAdd should allow card with rank already on table`() {
        val table = Table()
        table.addAttack(Card(Suit.HEARTS, Rank.SIX))

        val validCard = Card(Suit.SPADES, Rank.SIX)
        val invalidCard = Card(Suit.DIAMONDS, Rank.ACE)

        assertTrue(table.canAdd(validCard))
        assertFalse(table.canAdd(invalidCard))
    }
}
