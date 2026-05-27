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


    fun canAdd(card: Card, defenderHandSize: Int) : Boolean {
        if (slots.isEmpty()) {
            return true
        }

        if (slots.size >= 6 || slots.size >= defenderHandSize) {
            return false
        }

        val tableRanks = getAllCards().map{it.rank}.toSet()
        return card.rank in tableRanks
    }

    fun defendCard(attackCard: Card, defenseCard: Card, trumpSuit: Suit): Boolean {
        val slot = slots.firstOrNull { it.attackCard == attackCard && !it.isBeaten() } ?: return false

        val isSameSuit = attackCard.suit == defenseCard.suit
        val isDefenseHigher = defenseCard.rank > attackCard.rank
        val isDefenseTrump = defenseCard.suit == trumpSuit
        val isAttackTrump = attackCard.suit == trumpSuit

        val isValidBeat = (isSameSuit && isDefenseHigher) || (isDefenseTrump && !isAttackTrump)

        return if (isValidBeat) {
            slot.defenseCard = defenseCard
            true
        } else {
            false
        }
    }

    fun canTransfer(card: Card, nextPlayerHandSize: Int): Boolean {
        if (slots.isEmpty()) return false

        if (slots.any { it.isBeaten() }) return false

        val matchesRank = slots.all { it.attackCard.rank == card.rank }
        val doesFitHand = (slots.size + 1) <= nextPlayerHandSize

        return matchesRank && doesFitHand
    }


    fun addAttack(card: Card) = slots.add(TableSlot(card))

    fun clear() = slots.clear()
}