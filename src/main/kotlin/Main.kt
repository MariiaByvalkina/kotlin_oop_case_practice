import models.*
import logic.*
import infrastructure.*

fun main() {
    val db = DatabaseManager()
    val table = Table()
    val deck = Deck()
    deck.shuffle()

    val p1 = Player("1", "Игрок первый")
    val p2 = Player("2", "Игрок второй")
    val players = listOf(p1, p2)

    val trump = deck.draw() ?: throw IllegalStateException("Колода пуста")
    trump.isTrump = true

    fun giveCards(player: Player) {
        while (player.hand.size < 6 && deck.remaining() > 0) {
            val card = deck.draw()
            if (card is Card) {
                if (card.suit == trump.suit) {
                    card.isTrump = true
                }
                player.hand.add(card)
            } else {
                break
            }
        }
    }

    players.forEach { giveCards(it) }

    val session = GameSession(deck, table, players, GameMode.CLASSIC, trump)

    println("Игра началась! Козырь: ${trump.rank} ${trump.suit}")

    while (session.getResult() == GameResult.InProgress) {
        val currentPlayer = players[session.attackerIdx]

        println("\nКарты ${currentPlayer.name}: ${currentPlayer.hand}")
        print("Индекс (или 'выйти'): ")

        val input = readlnOrNull() ?: ""
        if (input.lowercase() == "выйти") break

        val index = input.toIntOrNull()
        if (index != null && index in currentPlayer.hand.indices) {
            try {
                val selectedCard = currentPlayer.hand[index]
                session.executeMove(currentPlayer, selectedCard)
                println("Ход выполнен!")

                session.attackerIdx = (session.attackerIdx + 1) % players.size

                players.forEach { giveCards(it) }
            } catch (e: Exception) {
                println("Ошибка: ${e.message}")
            }
        } else {
            println("Некорректный ввод. Введите число от 0 до ${currentPlayer.hand.size - 1}")
        }
    }


    db.saveSession(session)
    println("Партия завершена.")
}

