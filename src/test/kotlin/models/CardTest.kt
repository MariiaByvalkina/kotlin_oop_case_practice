package models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class CardTest {
    @Test
    fun testBeats() {
        val low = Card(Suit.HEARTS, Rank.SIX)
        val high = Card(Suit.HEARTS, Rank.ACE)
        assertTrue(high.beats(low))
    }
}
