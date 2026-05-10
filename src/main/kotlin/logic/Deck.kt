package logic
import models.*

class Deck {
    private val cards = mutableListOf<Card>()

    init {
        generateDeck()
    }

    private fun generateDeck() {
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                cards.add(Card(suit, rank))
            }
        }
    }

    fun shuffle() {
        cards.shuffle()
    }

    fun draw() : Card? {
        if (cards.isNotEmpty()) {
            return cards.removeAt(0)
        } else {
            return null
        }
    }

    fun remaining() : Int {
        return cards.size
    }
}