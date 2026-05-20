package gui

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Duration
import models.Card
import models.GameResult
import models.Player
import logic.Table

class GameViewGui(
    private val onCardClicked: (Card, Player) -> Unit,
    private val onActionClicked: () -> Unit
) {
    val tableCardsHBox = HBox(15.0).apply { alignment = Pos.CENTER }
    val playerCardsHBox = HBox(10.0).apply { alignment = Pos.CENTER }
    val statusLabel = Label().apply { style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;" }
    val infoLabel = Label().apply { style = "-fx-font-size: 13px;" }
    val scoreLabel = Label().apply { style = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #34495e;" }
    val timerLabel = Label("Время хода: 00:00").apply { style = "-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;" }
    val aiCardsLabel = Label().apply { style = "-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;" }

    val actionButton = Button().apply {
        style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 220px; -fx-min-height: 40px; -fx-background-radius: 5;"
    }

    private val playersVBox = VBox(5.0).apply { padding = Insets(5.0) }
    val collapsiblePlayersPane = TitledPane("Реестр игроков и Статистика", playersVBox).apply {
        isExpanded = false
        style = "-fx-font-size: 12px;"
    }

    private var timeSeconds = 0
    private var timeline: Timeline? = null

    init {
        actionButton.setOnAction { onActionClicked() }
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

    fun resetTimer() {
        timeline?.stop()
        timeSeconds = 0
        timerLabel.text = "Время хода: 00:00"
        timeline = Timeline(KeyFrame(Duration.seconds(1.0), {
            timeSeconds++
            timerLabel.text = String.format("Время хода: %02d:%02d", timeSeconds / 60, timeSeconds % 60)
        })).apply {
            cycleCount = Timeline.INDEFINITE
            play()
        }
    }

    fun stopTimer() {
        timeline?.stop()
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
        val user = allPlayers[0]
        val ai = allPlayers[1]

        val (trumpSymbol, trumpColor) = getCardVisuals(trump)
        infoLabel.text = "Режим: $modeName | Козырь: ${trump.rank} $trumpSymbol | В колоде: $deckRemaining"
        infoLabel.style = "-fx-font-size: 13px; -fx-text-fill: $trumpColor; -fx-font-weight: bold;"

        if (isUserAttacking) {
            statusLabel.text = "ТВОЙ ХОД: Выбери карту из руки, чтобы атаковать"
            actionButton.text = if (table.slots.isEmpty()) "Нечем ходить (Пас)" else "БИТО (Завершить ход)"
            actionButton.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 220px; -fx-min-height: 40px; -fx-background-color: #2ecc71; -fx-text-fill: white;"
        } else {
            statusLabel.text = "ЗАЩИТА: Нажми на карту в руке, чтобы побить карту на столе"
            actionButton.text = "ВЗЯТЬ КАРТЫ СО СТОЛА"
            actionButton.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 220px; -fx-min-height: 40px; -fx-background-color: #e74c3c; -fx-text-fill: white;"
        }

        scoreLabel.text = "Раунд: $roundCount | Карт у соперника: ${ai.hand.size}"
        aiCardsLabel.text = "Соперник: ${ai.name} (" + ai.hand.joinToString { "🎴" } + ")"

        resetTimer()

        playersVBox.children.clear()
        allPlayers.forEach { player ->
            val statsText = "${player.name} [ID: ${player.id}] — Карт: ${player.hand.size}"
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
        user.hand.forEach { card ->
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
                setOnAction { onCardClicked(card, user) }
            }
            playerCardsHBox.children.add(cardBtn)
        }
    }

    fun renderGameOver(result: GameResult) {
        stopTimer()

        // Преобразуем объект результата в понятный для человека текст
        val resultText = result.toString()
        val formattedResult = when {
            resultText.contains("Winner") && resultText.contains("Игрок первый") -> "ВЫ ВЫИГРАЛИ! 🎉"
            resultText.contains("Winner") && resultText.contains("второй") -> "ИИ ВЫИГРАЛ! Оппонент оказался сильнее."
            resultText.contains("Draw") || resultText.contains("Ничья") -> "НИЧЬЯ! Карты закончились."
            else -> "Игра окончена!"
        }

        statusLabel.text = formattedResult
        statusLabel.style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;"

        infoLabel.text = "Партия успешно сохранена в локальный реестр."
        scoreLabel.text = ""
        timerLabel.text = ""
        aiCardsLabel.text = ""

        playerCardsHBox.children.clear()
        tableCardsHBox.children.clear()
        actionButton.isDisable = true
        actionButton.text = "Игра завершена"
        actionButton.style = "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 14px;"
    }
}
