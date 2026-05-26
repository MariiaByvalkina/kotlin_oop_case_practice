package models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CardTest {

    @Test
    fun testCardInitializationAndFields() {
        val card = Card(Suit.HEARTS, Rank.JACK)

        assertEquals(Suit.HEARTS, card.suit)
        assertEquals(Rank.JACK, card.rank)

        assertTrue(card.isTrump(Suit.HEARTS))
        assertFalse(card.isTrump(Suit.SPADES))
    }

    @Test
    fun testBeatsSameSuit() {
        val low = Card(Suit.HEARTS, Rank.SIX)
        val high = Card(Suit.HEARTS, Rank.ACE)
        assertTrue(high.beats(low, Suit.SPADES))
    }

    @Test
    fun testBeatsDifferentSuitNonTrump() {
        val card1 = Card(Suit.HEARTS, Rank.ACE)
        val card2 = Card(Suit.SPADES, Rank.KING)

        assertFalse(card1.beats(card2, Suit.DIAMONDS))
        assertFalse(card2.beats(card1, Suit.DIAMONDS))
    }

    @Test
    fun testBeatsWithTrump() {
        val trumpCard = Card(Suit.DIAMONDS, Rank.SIX)
        val normalCard = Card(Suit.HEARTS, Rank.ACE)

        assertTrue(trumpCard.beats(normalCard, Suit.DIAMONDS))
        assertFalse(normalCard.beats(trumpCard, Suit.DIAMONDS))
    }

    @Test
    fun testCardToString() {
        val card = Card(Suit.CLUBS, Rank.TEN)
        assertEquals("TEN CLUBS", card.toString())
    }

    @Test
    fun testPlayerToStringAndHand() {
        val player = Player("99", "first")
        assertTrue(player.hand.isEmpty())

        player.hand.add(Card(Suit.CLUBS, Rank.JACK))
        val output = player.toString()

        assertTrue(output.contains("first"))
        assertTrue(output.contains("cardsInHand=1"))
    }
}
