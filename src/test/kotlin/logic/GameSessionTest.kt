package logic

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import models.*

class GameSessionTest {
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
}
