package logic

import models.*

class TableSlot(val attackCard: Card, var defenseCard: Card? = null) {
    fun isBeaten() : Boolean {
        return defenseCard != null
    }
}

class Table {
    val slots = mutableListOf<TableSlot>()

    fun getAllCards() : List<Card> {
        return slots.flatMap{listOfNotNull(it.attackCard, it.defenseCard)}
    }


    fun canAdd(card: Card) : Boolean {
        if (slots.isEmpty()) {
            return true
        }
        val tableRanks = getAllCards().map{it.rank}.toSet()
        return card.rank in tableRanks
    }

    fun addAttack(card: Card) = slots.add(TableSlot(card))

    fun clear() = slots.clear()
}