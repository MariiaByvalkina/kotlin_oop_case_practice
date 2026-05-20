package gui

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import models.*
import logic.*
import infrastructure.*

class DurakGuiApp : Application() {
    private val db = DatabaseManager()
    private val table = Table()
    private val deck = Deck()
    private lateinit var p1: Player
    private lateinit var p2: Player
    private lateinit var players: List<Player>
    private lateinit var session: GameSession
    private lateinit var trump: Card

    private lateinit var primaryStage: Stage
    private lateinit var gameViewGui: GameViewGui
    private var roundCount = 1
    private var selectedGameMode = GameMode.CLASSIC

    override fun start(stage: Stage) {
        primaryStage = stage
        showMenuScreen()
    }

    private fun showMenuScreen() {
        val titleLabel = Label("ДУРАК — НАСТРОЙКА ПАРТИИ").apply {
            style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        }

        val modeLabel = Label("Выберите режим игры:").apply { style = "-fx-font-size: 14px;" }
        val classicRadio = RadioButton("Классический").apply { isSelected = true }
        val transferRadio = RadioButton("Подкидной / Переводной")
        ToggleGroup().apply {
            classicRadio.toggleGroup = this
            transferRadio.toggleGroup = this
        }

        val nameLabel = Label("Ваше имя в реестре:").apply { style = "-fx-font-size: 14px;" }
        val nameField = TextField("Игрок первый").apply { maxWidth = 200.0 }

        val startBtn = Button("НАЧАТЬ ИГРУ").apply {
            style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-min-width: 150px; -fx-min-height: 35px;"
        }

        startBtn.setOnAction {
            selectedGameMode = if (classicRadio.isSelected) GameMode.CLASSIC else GameMode.TRANSFERABLE
            initGameEngine(nameField.text)
            showGameScreen()
        }

        val menuLayout = VBox(20.0).apply {
            alignment = Pos.CENTER
            padding = Insets(40.0)
            style = "-fx-background-color: #f5f6fa;"
            children.addAll(titleLabel, modeLabel, classicRadio, transferRadio, nameLabel, nameField, startBtn)
        }

        primaryStage.title = "Дурак — Главное меню"
        primaryStage.scene = Scene(menuLayout, 450.0, 400.0)
        primaryStage.show()
    }

    private fun initGameEngine(userName: String) {
        deck.shuffle()
        p1 = Player("1", userName)
        p2 = Player("2", "Игрок второй (ИИ)")
        players = listOf(p1, p2)

        trump = deck.draw() ?: throw IllegalStateException("Колода пуста")
        trump.isTrump = true

        players.forEach { giveCards(it) }
        session = GameSession(deck, table, players, selectedGameMode, trump)
    }

    private fun showGameScreen() {
        gameViewGui = GameViewGui(
            onCardClicked = { card, _ -> handleCardClick(card) },
            onActionClicked = { handleActionClick() }
        )

        val topPanel = VBox(10.0).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 0 0 1 0;"
            children.addAll(
                gameViewGui.collapsiblePlayersPane,
                gameViewGui.aiCardsLabel,
                gameViewGui.infoLabel,
                gameViewGui.statusLabel,
                gameViewGui.scoreLabel,
                gameViewGui.timerLabel
            )
        }

        val centerPanel = VBox(15.0).apply {
            alignment = Pos.CENTER
            style = "-fx-background-color: #f1f2f6;"
            children.addAll(Label("Карты на столе:").apply { style = "-fx-font-weight: bold;" }, gameViewGui.tableCardsHBox)
        }

        val bottomPanel = VBox(15.0).apply {
            alignment = Pos.CENTER
            padding = Insets(15.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 1 0 0 0;"
            children.addAll(Label("Ваши карты:").apply { style = "-fx-font-weight: bold;" }, gameViewGui.playerCardsHBox, gameViewGui.actionButton)
        }

        val mainLayout = BorderPane().apply {
            top = topPanel
            center = centerPanel
            bottom = bottomPanel
        }

        updateUi()

        primaryStage.title = "Дурак — Графический Интерфейс"
        primaryStage.scene = Scene(mainLayout, 800.0, 600.0)
        primaryStage.setOnCloseRequest { gameViewGui.stopTimer() }
    }

    private fun handleCardClick(selectedCard: Card) {
        try {
            val isUserAttacking = (session.attackerIdx == 0)

            session.executeMove(p1, selectedCard)

            if (isUserAttacking) {
                aiTryDefense()
            } else {
                if (table.slots.all { it.isBeaten() }) {
                    aiNextMoveOrPass()
                }
            }
            updateUi()
        } catch (e: Exception) {
            showError(e.message ?: "Ход невозможен")
        }
    }

    private fun handleActionClick() {
        val isUserAttacking = (session.attackerIdx == 0)

        if (isUserAttacking) {
            endTurnBito()
        } else {
            userTakeCards()
        }
        updateUi()
    }

    private fun endTurnBito() {
        table.clear()
        session.attackerIdx = 1
        roundCount++
        players.forEach { giveCards(it) }
        aiTryAttack()
    }

    private fun userTakeCards() {
        p1.hand.addAll(table.getAllCards())
        table.clear()
        roundCount++
        players.forEach { giveCards(it) }
        aiTryAttack()
    }

    private fun aiTryDefense() {
        val unbeatenSlot = table.slots.firstOrNull { !it.isBeaten() } ?: return
        val aiCard = p2.hand.filter { it.suit == unbeatenSlot.attackCard.suit && it.rank > unbeatenSlot.attackCard.rank }
            .minByOrNull { it.rank }
            ?: p2.hand.filter { it.isTrump && !unbeatenSlot.attackCard.isTrump }
                .minByOrNull { it.rank }

        if (aiCard != null) {
            try {
                session.executeMove(p2, aiCard)
            } catch (e: Exception) {
                aiTakeCards()
            }
        } else {
            aiTakeCards()
        }
    }

    private fun aiTakeCards() {
        p2.hand.addAll(table.getAllCards())
        table.clear()
        session.attackerIdx = 0
        roundCount++
        players.forEach { giveCards(it) }
    }

    private fun aiTryAttack() {
        if (session.attackerIdx == 1 && p2.hand.isNotEmpty()) {
            val card = p2.hand.minByOrNull { it.rank }
            if (card != null) {
                try {
                    session.executeMove(p2, card)
                } catch (e: Exception) {
                    endTurnBito()
                }
            }
        }
    }

    private fun aiNextMoveOrPass() {
        val existingRanks = table.slots.flatMap { listOf(it.attackCard.rank, it.defenseCard?.rank) }.filterNotNull().toSet()
        val cardToSub = p2.hand.firstOrNull { it.rank in existingRanks }

        if (cardToSub != null && selectedGameMode == GameMode.TRANSFERABLE) {
            try {
                session.executeMove(p2, cardToSub)
                updateUi()
            } catch (e: Exception) {
                session.attackerIdx = 0
                table.clear()
                roundCount++
                players.forEach { giveCards(it) }
            }
        } else {
            session.attackerIdx = 0
            table.clear()
            roundCount++
            players.forEach { giveCards(it) }
        }
    }

    private fun updateUi() {
        if (session.getResult() != GameResult.InProgress) {
            gameViewGui.renderGameOver(session.getResult())
            db.saveSession(session)
            return
        }

        val isUserAttacking = (session.attackerIdx == 0)
        val modeName = if (selectedGameMode == GameMode.CLASSIC) "Классика" else "Подкидной"

        gameViewGui.renderState(
            trump, deck.remaining(), players[session.attackerIdx], table, players,
            roundCount, isUserAttacking, modeName
        )
    }

    private fun giveCards(player: Player) {
        while (player.hand.size < 6 && deck.remaining() > 0) {
            val card = deck.draw()
            if (card is Card) {
                if (card.suit == trump.suit) card.isTrump = true
                player.hand.add(card)
            } else {
                break
            }
        }
    }

    private fun showError(message: String) {
        Alert(Alert.AlertType.ERROR, message).showAndWait()
    }
}
