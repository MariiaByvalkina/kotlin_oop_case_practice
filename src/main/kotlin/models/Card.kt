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
    val rank: Rank,
    var isTrump: Boolean = false
) {
    fun beats(other: Card) : Boolean {
        if (this.suit == other.suit) {
            return this.rank.value > other.rank.value
        }
        return this.isTrump && !other.isTrump
    }
}