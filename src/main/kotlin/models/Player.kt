package models

class Player(val id: String, val name: String) {
    val hand: MutableList<Card> = mutableListOf()

    override fun toString(): String {
        return "Player(name='$name', cardsInHand=${hand.size})"
    }
}