package logic

import models.*

enum class GameMode {
    CLASSIC,
    TRANSFERABLE
}

class GameSession(
    private val deck: Deck,
    private val table: Table,
    val players: List<Player>,
    private val mode: GameMode,
    private val trumpSuit: Suit
) {

    private val turnHistory = mutableListOf<String>()

    var attackerIdx = 0
        private set

    private var defenderIdx = 1

    fun getCurrentAttacker(): Player {
        return players[attackerIdx]
    }

    fun getCurrentDefender(): Player {
        return players[defenderIdx]
    }

    private fun isCurrentPlayer(player: Player): Boolean {
        return player == getCurrentAttacker() || player == getCurrentDefender()
    }

    fun executeMove(player: Player, card: Card) {

        if (!isCurrentPlayer(player)) {
            throw IllegalArgumentException("Сейчас ход другого игрока")
        }

        if (!validateAction(player, card)) {
            throw IllegalArgumentException("Ход нарушает правила")
        }

        val isAttacker = player == getCurrentAttacker()

        if (isAttacker) {
            table.addAttack(card)
        } else {

            val isTransfer = mode == GameMode.TRANSFERABLE &&
                    table.canTransfer(card, players[(defenderIdx + 1) % players.size].hand.size)

            if (isTransfer) {
                transferAttack(card)
            } else {

                val attackSlot = table.slots.firstOrNull {
                    !it.isBeaten() && card.beats(it.attackCard, trumpSuit)
                }

                if (attackSlot == null) {
                    throw IllegalArgumentException("Нельзя отбить карту")
                }

                attackSlot.defenseCard = card
            }
        }

        player.hand.remove(card)

        turnHistory.add(
            "${player.name} сыграл ${card.rank} ${card.suit}"
        )
    }

    private fun transferAttack(card: Card) {

        table.addAttack(card)

        attackerIdx = (attackerIdx + 1) % players.size
        defenderIdx = (attackerIdx + 1) % players.size
    }

    fun nextTurn() {
        attackerIdx = defenderIdx
        defenderIdx = (attackerIdx + 1) % players.size
    }

    fun finishRound() {
        table.clear()
        nextTurn()
        refillCards()
    }

    fun takeCards() {

        val defender = getCurrentDefender()

        defender.hand.addAll(table.getAllCards())
        table.clear()

        attackerIdx = (defenderIdx + 1) % players.size
        defenderIdx = (attackerIdx + 1) % players.size
        refillCards()
    }

    private fun refillCards() {

        players.forEach { player ->

            while (player.hand.size < 6 && deck.remaining() > 0) {

                val card = deck.draw() ?: break
                player.hand.add(card)
            }
        }
    }

    private fun validateAction(player: Player, card: Card): Boolean {

        if (card !in player.hand) {
            return false
        }

        val isAttacker = player == getCurrentAttacker()

        return if (isAttacker) {

            table.canAdd(card,getCurrentDefender().hand.size)

        } else {
            table.slots.any {
                !it.isBeaten() && card.beats(it.attackCard, trumpSuit)
            }
        }
    }

    fun getResult(): GameResult {
        val activePlayers = players.filter {
            !(it.hand.isEmpty() && deck.remaining() == 0)
        }

        return when {
            activePlayers.size == 1 -> GameResult.Winner(activePlayers.first())
            activePlayers.isEmpty() -> GameResult.Draw
            else -> GameResult.InProgress
        }
    }
}