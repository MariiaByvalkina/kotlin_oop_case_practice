package gui

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import models.*
import logic.*
import infrastructure.*

class DurakGuiApp : Application() {
    private val db = DatabaseManager()
    private val table = Table()
    private val deck = Deck()
    private val players = mutableListOf<Player>()
    private lateinit var session: GameSession
    private lateinit var trump: Card

    private lateinit var primaryStage: Stage
    private lateinit var gameViewGui: GameViewGui
    private var roundCount = 1
    private var selectedGameMode = GameMode.CLASSIC

    private var isDefensePhase = false
    private var currentTurnPlayerIdx = 0

    override fun start(stage: Stage) {
        primaryStage = stage
        showMenuScreen()
    }

    private fun showMenuScreen() {
        val titleLabel = Label("ДУРАК — НАСТРОЙКА МУЛЬТИПЛЕЕРА").apply {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        }

        val modeLabel = Label("Режим игры:").apply { style = "-fx-font-weight: bold;" }
        val classicRadio = RadioButton("Классический").apply { isSelected = true }
        val transferRadio = RadioButton("Подкидной / Переводной")
        ToggleGroup().apply {
            classicRadio.toggleGroup = this
            transferRadio.toggleGroup = this
        }

        val countLabel = Label("Количество игроков (живые люди):").apply { style = "-fx-font-weight: bold;" }
        val countComboBox = ComboBox<Int>().apply {
            items.addAll(2, 3, 4)
            value = 2
        }

        val namesVBox = VBox(8.0).apply { alignment = Pos.CENTER }

        val updatePlayerFields = { count: Int ->
            namesVBox.children.clear()
            for (i in 1..count) {
                namesVBox.children.add(TextField("Игрок $i").apply { maxWidth = 200.0 })
            }
        }
        updatePlayerFields(2)
        countComboBox.setOnAction { updatePlayerFields(countComboBox.value) }

        val startBtn = Button("НАЧАТЬ ПАРТИЮ").apply {
            style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-min-width: 180px; -fx-min-height: 35px;"
        }

        startBtn.setOnAction {
            selectedGameMode = if (classicRadio.isSelected) GameMode.CLASSIC else GameMode.TRANSFERABLE
            players.clear()
            namesVBox.children.filterIsInstance<TextField>().forEachIndexed { idx, field ->
                players.add(Player((idx + 1).toString(), field.text))
            }
            initGameEngine()
            showGameScreen()
        }

        val menuLayout = VBox(15.0).apply {
            alignment = Pos.CENTER
            padding = Insets(25.0)
            style = "-fx-background-color: #f5f6fa;"
            children.addAll(titleLabel, modeLabel, classicRadio, transferRadio, countLabel, countComboBox, Label("Имена участников:"), namesVBox, startBtn)
        }

        primaryStage.title = "Дурак — Главное меню"
        primaryStage.scene = Scene(menuLayout, 450.0, 550.0)
        primaryStage.show()
    }

    private fun initGameEngine() {
        deck.shuffle()
        trump = deck.draw() ?: throw IllegalStateException("Колода пуста")
        trump.isTrump = true

        players.forEach { giveCards(it) }
        session = GameSession(deck, table, players, selectedGameMode, trump)
        currentTurnPlayerIdx = session.attackerIdx
        isDefensePhase = false
    }

    private fun showGameScreen() {
        gameViewGui = GameViewGui(
            onCardClicked = { card, player -> handleCardClick(card, player) },
            onActionClicked = { handleActionClick() },
            onFinishInteractionClicked = { handleFinishInteractionClick() }
        )

        val topPanel = VBox(10.0).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 0 0 1 0;"
            children.addAll(
                gameViewGui.collapsiblePlayersPane,
                gameViewGui.activePlayerLabel,
                gameViewGui.infoLabel,
                gameViewGui.statusLabel,
                gameViewGui.scoreLabel
            )
        }

        val centerPanel = VBox(15.0).apply {
            alignment = Pos.CENTER
            style = "-fx-background-color: #f1f2f6;"
            children.addAll(Label("Карты на столе:").apply { style = "-fx-font-weight: bold;" }, gameViewGui.tableCardsHBox)
        }

        val controlButtonsHBox = HBox(20.0).apply {
            alignment = Pos.CENTER
            children.addAll(gameViewGui.actionButton, gameViewGui.finishInteractionButton)
        }

        val bottomPanel = VBox(15.0).apply {
            alignment = Pos.CENTER
            padding = Insets(15.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 1 0 0 0;"
            children.addAll(Label("Ваши карты:").apply { style = "-fx-font-weight: bold;" }, gameViewGui.playerCardsHBox, controlButtonsHBox)
        }

        val mainLayout = BorderPane().apply {
            top = topPanel
            center = centerPanel
            bottom = bottomPanel
        }

        primaryStage.title = "Дурак — Игровое поле"
        primaryStage.scene = Scene(mainLayout, 800.0, 600.0)
        updateUi()
    }

    private fun handleCardClick(selectedCard: Card, player: Player) {
        try {
            val isTransferMove = selectedGameMode == GameMode.TRANSFERABLE &&
                    table.slots.isNotEmpty() &&
                    table.slots.all { !it.isBeaten() } &&
                    table.slots.all { it.attackCard.rank == selectedCard.rank }

            session.executeMove(player, selectedCard)

            if (isTransferMove) {
                isDefensePhase = false
                currentTurnPlayerIdx = session.attackerIdx
            }
            updateUi()
        } catch (e: Exception) {
            showError(e.message ?: "Ход невозможен")
        }
    }

    private fun handleFinishInteractionClick() {
        if (!isDefensePhase) {
            if (table.slots.isEmpty()) {
                showError("Вы должны выложить хотя бы одну карту для атаки!")
                return
            }
            isDefensePhase = true
            currentTurnPlayerIdx = (session.attackerIdx + 1) % players.size
        } else {
            val allBeaten = table.slots.all { it.isBeaten() }
            if (!allBeaten) {
                showError("Вы должны отбить все карты на столе перед завершением хода!")
                return
            }
            isDefensePhase = false
            currentTurnPlayerIdx = session.attackerIdx
        }
        showPassScreen(players[currentTurnPlayerIdx])
    }

    private fun handleActionClick() {
        val defenderIdx = (session.attackerIdx + 1) % players.size

        if (!isDefensePhase) {
            table.clear()
            session.attackerIdx = defenderIdx
            currentTurnPlayerIdx = session.attackerIdx
            isDefensePhase = false
            roundCount++
            players.forEach { giveCards(it) }
            showPassScreen(players[currentTurnPlayerIdx])
        } else {
            players[defenderIdx].hand.addAll(table.getAllCards())
            table.clear()
            session.attackerIdx = (defenderIdx + 1) % players.size
            currentTurnPlayerIdx = session.attackerIdx
            isDefensePhase = false
            roundCount++
            players.forEach { giveCards(it) }
            showPassScreen(players[currentTurnPlayerIdx])
        }
    }

    private fun showPassScreen(nextPlayer: Player) {
        val label = Label("ПЕРЕДАЙТЕ УПРАВЛЕНИЕ ИГРОКУ:\n\n${nextPlayer.name}").apply {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-alignment: center; -fx-text-fill: white;"
        }
        val btn = Button("Я ГОТОВ, ПОКАЗАТЬ КАРТЫ").apply {
            style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #3498db; -fx-text-fill: white; -fx-min-height: 40px;"
        }

        val passLayout = VBox(25.0).apply {
            alignment = Pos.CENTER
            padding = Insets(50.0)
            style = "-fx-background-color: #2c3e50;"
        }
        passLayout.children.addAll(label, btn)

        val oldScene = primaryStage.scene
        primaryStage.scene = Scene(passLayout, 800.0, 600.0)

        btn.setOnAction {
            primaryStage.scene = oldScene
            updateUi()
        }
    }

    private fun updateUi() {
        if (session.getResult() != GameResult.InProgress) {
            gameViewGui.renderGameOver(session.getResult())
            db.saveSession(session)
            return
        }

        val modeName = if (selectedGameMode == GameMode.CLASSIC) "Классика" else "Подкидной/Переводной"
        gameViewGui.renderState(
            trump, deck.remaining(), players[currentTurnPlayerIdx], table, players,
            roundCount, !isDefensePhase, modeName
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
