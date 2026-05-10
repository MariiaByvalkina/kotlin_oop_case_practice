package logic
import models.*

enum class GameMode {CLASSIC, TRANSFERABLE}

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
            throw IllegalArgumentException()
        }

        if (player == players[attackerIdx]) {
            table.slots.add(TableSlot(card))
        } else {
            val slot = table.slots.firstOrNull{!it.isBeaten()}
            slot?.defenseCard = card
        }
        player.hand.remove(card)
        turnHistory.add("${player.name} player ${card.rank} of ${card.suit}")
    }

    private fun validateAction(player: Player, card: Card) : Boolean {
        if (card !in player.hand) {
            return false
        }

        val isAttacker = (player == players[attackerIdx])

        return if (isAttacker) {
            table.canAdd(card) && table.slots.size < 6
        } else {
            val currentSlot = table.slots.firstOrNull{!it.isBeaten()}
            currentSlot != null && card.beats(currentSlot.attackCard)
        }
    }

    fun getResult() : GameResult {
        val active = players.filter{it.hand.isNotEmpty() || deck.remaining() > 0}

        return when {
            active.size == 1 -> GameResult.Winner(active.first())
            active.isEmpty() -> GameResult.Draw
            else -> GameResult.InProgress
        }
    }
}