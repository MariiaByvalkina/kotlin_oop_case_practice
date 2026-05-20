package logic
import models.*

enum class GameMode { CLASSIC, TRANSFERABLE }

class GameSession(
    private val deck: Deck,
    private val table: Table,
    val players: List<Player>,
    private val mode: GameMode,
    var trumpCard: Card
) {
    private val turnHistory = mutableListOf<String>()
    var attackerIdx: Int = 0

    fun executeMove(player: Player, card: Card) {
        if (!validateAction(player, card)) {
            throw IllegalArgumentException("Ход нарушает правила игры")
        }

        if (player == players[attackerIdx]) {
            table.slots.add(TableSlot(card))
        } else {
            val isTransferMove = mode == GameMode.TRANSFERABLE && table.slots.all { !it.isBeaten() } && table.slots.all { it.attackCard.rank == card.rank }

            if (isTransferMove) {
                table.slots.add(TableSlot(card))
                attackerIdx = (attackerIdx + 1) % players.size
            } else {
                val slot = table.slots.firstOrNull { !it.isBeaten() }
                slot?.defenseCard = card
            }
        }
        player.hand.remove(card)
        turnHistory.add("${player.name} сыграл ${card.rank} масти ${card.suit}")
    }

    private fun validateAction(player: Player, card: Card) : Boolean {
        if (card !in player.hand) {
            return false
        }

        val isAttacker = (player == players[attackerIdx])
        val defenderIdx = (attackerIdx + 1) % players.size
        val defender = players[defenderIdx]

        return if (isAttacker) {
            val fitsDefenderHand = table.slots.size < defender.hand.size

            table.canAdd(card, defender.hand.size) && table.slots.size < 6 && fitsDefenderHand
        } else {
            val isTransferPossible = mode == GameMode.TRANSFERABLE &&
                    table.slots.all { !it.isBeaten() } &&
                    table.slots.all { it.attackCard.rank == card.rank } &&
                    (table.slots.size + 1) <= players[(defenderIdx + 1) % players.size].hand.size

            val currentSlot = table.slots.firstOrNull { !it.isBeaten() }

            isTransferPossible || (currentSlot != null && card.beats(currentSlot.attackCard))
        }
    }

    fun getResult() : GameResult {
        val active = players.filter { it.hand.isNotEmpty() || deck.remaining() > 0 }

        return when {
            active.size == 1 -> GameResult.Winner(active.first())
            active.isEmpty() -> GameResult.Draw
            else -> GameResult.InProgress
        }
    }
}
