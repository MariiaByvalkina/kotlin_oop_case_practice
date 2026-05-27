package models

class Player(val id: String, val name: String) {
    val hand: MutableList<Card> = mutableListOf()

    override fun toString(): String {
        return "Player(name='$name', cardsInHand=${hand.size})"
    }
}

data class PlayerRecord(
    val playerId: String,
    var wins: Int = 0,
    var gamesPlayed: Int = 0
)