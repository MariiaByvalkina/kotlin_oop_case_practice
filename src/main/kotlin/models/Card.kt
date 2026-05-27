package models

enum class Suit(val symbol: String) {
    HEARTS("RED"),
    DIAMONDS("RED"),
    CLUBS("BLACK"),
    SPADES("BLACK")
}

enum class Rank(val value: Int) {
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14)
}

data class Card(
    val suit: Suit,
    val rank: Rank
) {

    fun isTrump(trumpSuit: Suit): Boolean {
        return suit == trumpSuit
    }

    fun beats(other: Card, trumpSuit: Suit): Boolean {
        if (suit == other.suit) {
            return rank.value > other.rank.value
        }
        return isTrump(trumpSuit) && !other.isTrump(trumpSuit)
    }

    override fun toString(): String {
        return "$rank $suit"
    }
}