package gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import models.Card
import models.GameResult
import models.Player
import logic.Table

class GameViewGui(
    private val onCardClicked: (Card, Player) -> Unit,
    private val onActionClicked: () -> Unit,
    private val onFinishInteractionClicked: () -> Unit
) {
    val tableCardsHBox = HBox(15.0).apply { alignment = Pos.CENTER }
    val playerCardsHBox = HBox(10.0).apply { alignment = Pos.CENTER }
    val statusLabel = Label().apply { style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;" }
    val infoLabel = Label().apply { style = "-fx-font-size: 13px;" }
    val scoreLabel = Label().apply { style = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #34495e;" }
    val activePlayerLabel = Label().apply { style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #8e44ad;" }

    val actionButton = Button().apply {
        style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 40px; -fx-background-radius: 5;"
    }

    val finishInteractionButton = Button("ГОТОВО (Передать ход)").apply {
        style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 40px; -fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 5;"
    }

    private val playersVBox = VBox(5.0).apply { padding = Insets(5.0) }
    val collapsiblePlayersPane = TitledPane("Реестр игроков и Статистика", playersVBox).apply {
        isExpanded = false
        style = "-fx-font-size: 12px;"
    }

    init {
        actionButton.setOnAction { onActionClicked() }
        finishInteractionButton.setOnAction { onFinishInteractionClicked() }
    }

    private fun getCardVisuals(card: Card): Pair<String, String> {
        val suitName = card.suit.toString().uppercase()
        return when {
            suitName.contains("HEART") -> Pair("♥️", "#e74c3c")
            suitName.contains("DIAMOND") -> Pair("♦️", "#e67e22")
            suitName.contains("CLUB") -> Pair("♣️", "#2c3e50")
            suitName.contains("SPADE") -> Pair("♠️", "#2c3e50")
            else -> Pair(card.suit.toString(), "#2c3e50")
        }
    }

    fun renderState(
        trump: Card,
        deckRemaining: Int,
        activePlayer: Player,
        table: Table,
        allPlayers: List<Player>,
        roundCount: Int,
        isUserAttacking: Boolean,
        modeName: String
    ) {
        val (trumpSymbol, trumpColor) = getCardVisuals(trump)
        infoLabel.text = "Режим: $modeName | Козырь: ${trump.rank} $trumpSymbol | В колоде: $deckRemaining"
        infoLabel.style = "-fx-font-size: 13px; -fx-text-fill: $trumpColor; -fx-font-weight: bold;"

        activePlayerLabel.text = "ТЕКУЩИЙ ИГРОК: ${activePlayer.name}"

        if (isUserAttacking) {
            statusLabel.text = "АТАКА: Выложи одну или несколько карт одного достоинства, затем нажми 'ГОТОВО'"
            actionButton.text = if (table.slots.isEmpty()) "Пропустить ход" else "БИТО (Очистить стол)"
            actionButton.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 40px; -fx-background-color: #2ecc71; -fx-text-fill: white;"
        } else {
            statusLabel.text = "ЗАЩИТА: Отбей карты на столе, затем нажми 'ГОТОВО' для подтверждения"
            actionButton.text = "ВЗЯТЬ КАРТЫ СО СТОЛА"
            actionButton.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 40px; -fx-background-color: #e74c3c; -fx-text-fill: white;"
        }

        scoreLabel.text = "Раунд: $roundCount"

        playersVBox.children.clear()
        allPlayers.forEach { player ->
            val statsText = "${player.name} [ID: ${player.id}] — Карт на руках: ${player.hand.size}"
            playersVBox.children.add(Label(statsText).apply { style = "-fx-padding: 2px 0;" })
        }

        tableCardsHBox.children.clear()
        table.slots.forEach { slot ->
            val (atkSymbol, atkColor) = getCardVisuals(slot.attackCard)
            var cardText = "${slot.attackCard.rank}$atkSymbol"
            var btnStyle = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: $atkColor; -fx-background-color: #ffffff; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-min-width: 70px; -fx-min-height: 90px;"

            if (slot.defenseCard != null) {
                val (defSymbol, defColor) = getCardVisuals(slot.defenseCard!!)
                cardText += "\n⚔️\n${slot.defenseCard!!.rank}$defSymbol"
                btnStyle += "-fx-border-color: #2ecc71; -fx-border-width: 2;"
            } else {
                btnStyle += "-fx-border-color: #e74c3c; -fx-border-width: 1.5; -fx-background-color: #fff5f5;"
            }

            val btn = Button(cardText).apply {
                isDisable = true
                style = btnStyle
                setStyle(style + "-fx-opacity: 1.0; -fx-text-alignment: center;")
            }
            tableCardsHBox.children.add(btn)
        }

        playerCardsHBox.children.clear()
        activePlayer.hand.forEach { card ->
            val (suitSymbol, suitColor) = getCardVisuals(card)
            val btnText = "${card.rank}\n$suitSymbol${if (card.isTrump) "\n★" else ""}"

            var cardStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: $suitColor; -fx-min-width: 70px; -fx-min-height: 95px; -fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-radius: 5; -fx-text-alignment: center;"
            if (card.isTrump) {
                cardStyle += "-fx-background-color: #fff9db; -fx-border-color: #f1c40f; -fx-border-width: 2;"
            }

            val cardBtn = Button(btnText).apply {
                style = cardStyle
                setOnMouseEntered { this.style = cardStyle + "-fx-background-color: #f1f2f6; -fx-cursor: hand;" }
                setOnMouseExited { this.style = cardStyle }
                setOnAction { onCardClicked(card, activePlayer) }
            }
            playerCardsHBox.children.add(cardBtn)
        }
    }

    fun renderGameOver(result: GameResult) {
        val resultText = result.toString()
        statusLabel.text = "ПАРТИЯ ЗАВЕРШЕНА!"
        val formattedResult = when {
            resultText.contains("Winner") -> {
                val nameStart = resultText.indexOf("name='") + 6
                val nameEnd = resultText.indexOf("'", nameStart)
                val winnerName = resultText.substring(nameStart, nameEnd)
                "ПОБЕДИТЕЛЬ: $winnerName! 🎉"
            }
            else -> "НИЧЬЯ! Карты закончились."
        }
        statusLabel.text = formattedResult
        statusLabel.style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;"
        infoLabel.text = "Партия успешно сохранена в локальный реестр."
        scoreLabel.text = ""
        activePlayerLabel.text = ""
        playerCardsHBox.children.clear()
        tableCardsHBox.children.clear()
        actionButton.isDisable = true
        finishInteractionButton.isDisable = true
        actionButton.text = "Игра окончена"
        actionButton.style = "-fx-background-color: #7f8c8d; -fx-text-fill: white;"
    }
}
