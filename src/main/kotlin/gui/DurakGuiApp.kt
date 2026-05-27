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
    private val guiTurnHistory = mutableListOf<String>()

    private val historyTextArea = TextArea().apply {
        isEditable = false
        isWrapText = true
        prefWidth = 220.0
        prefHeight = 400.0
        style = "-fx-font-family: 'Courier New'; -fx-font-size: 12px;"
    }

    private val onModelChanged = {
        updateUi()
    }

    override fun start(stage: Stage) {
        primaryStage = stage
        showMenuScreen()
    }

    fun showMenuScreen() {
        val titleLabel = Label("ДУРАК — НАСТРОЙКА").apply {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        }

        val modeLabel = Label("Режим игры:").apply { style = "-fx-font-weight: bold;" }
        val classicRadio = RadioButton("Классический").apply { isSelected = true }
        val transferRadio = RadioButton("Переводной")
        ToggleGroup().apply {
            classicRadio.toggleGroup = this
            transferRadio.toggleGroup = this
        }

        val countLabel = Label("Количество игроков:").apply { style = "-fx-font-weight: bold;" }
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

        val menuLayout = VBox(15.0, titleLabel, modeLabel, classicRadio, transferRadio, countLabel, countComboBox, Label("Имена участников:"), namesVBox, startBtn).apply {
            alignment = Pos.CENTER
            padding = Insets(25.0)
            style = "-fx-background-color: #f5f6fa;"
        }

        primaryStage.title = "Дурак — Главное меню"
        primaryStage.scene = Scene(menuLayout, 450.0, 550.0)
        primaryStage.show()
    }

    private fun initGameEngine() {
        deck.shuffle()
        trump = deck.draw() ?: throw IllegalStateException("Колода пуста")

        players.forEach { giveCards(it) }
        session = GameSession(deck, table, players, selectedGameMode, trump.suit)
        currentTurnPlayerIdx = session.attackerIdx
        isDefensePhase = false
        guiTurnHistory.clear()
        guiTurnHistory.add("Игра началась. Козырь: ${trump.rank} ${trump.suit}")
    }

    fun showGameScreen() {
        gameViewGui = GameViewGui(
            onCardClicked = { card, player -> handleCardClick(card, player) },
            onActionClicked = { handleActionClick() },
            onFinishInteractionClicked = { handleFinishInteractionClick() }
        )

        setupGameLayout()
        onModelChanged()
    }

    fun setupGameLayout() {
        val topPanel = VBox(10.0, gameViewGui.collapsiblePlayersPane, gameViewGui.activePlayerLabel, gameViewGui.infoLabel, gameViewGui.statusLabel, gameViewGui.scoreLabel).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 0 0 1 0;"
        }

        val centerPanel = VBox(15.0, Label("Карты на столе:").apply { style = "-fx-font-weight: bold;" }, gameViewGui.tableCardsHBox).apply {
            alignment = Pos.CENTER
            style = "-fx-background-color: #f1f2f6;"
        }

        val controlButtonsHBox = HBox(20.0, gameViewGui.actionButton, gameViewGui.finishInteractionButton).apply {
            alignment = Pos.CENTER
        }

        val bottomPanel = VBox(15.0, Label("Ваши карты:").apply { style = "-fx-font-weight: bold;" }, gameViewGui.playerCardsHBox, controlButtonsHBox).apply {
            alignment = Pos.CENTER
            padding = Insets(15.0)
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 1 0 0 0;"
        }

        val rightPanel = VBox(10.0, Label("История ходов:").apply { style = "-fx-font-weight: bold;" }, historyTextArea).apply {
            padding = Insets(10.0)
            alignment = Pos.CENTER
            style = "-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 0 0 0 1;"
        }

        val mainLayout = BorderPane().apply {
            top = topPanel
            center = centerPanel
            bottom = bottomPanel
            right = rightPanel
        }

        primaryStage.scene = Scene(mainLayout, 1030.0, 600.0)
    }

    fun updateUi() {
        if (session.getResult() != GameResult.InProgress) {
            gameViewGui.renderGameOver(session.getResult())
            db.saveSession(session)
        } else {
            val modeName = if (selectedGameMode == GameMode.CLASSIC) "Классика" else "Переводной"
            val activePlayer = players[currentTurnPlayerIdx]

            gameViewGui.renderState(
                trump = trump,
                deckRemaining = deck.remaining(),
                activePlayer = activePlayer,
                table = table,
                allPlayers = players,
                roundCount = roundCount,
                isUserAttacking = !isDefensePhase,
                modeName = modeName
            )
            historyTextArea.text = guiTurnHistory.joinToString("\n")
            historyTextArea.selectPositionCaret(historyTextArea.text.length)
        }
    }

    private fun handleCardClick(selectedCard: Card, player: Player) {
        try {
            val oldAttacker = session.getCurrentAttacker()
            session.executeMove(player, selectedCard)

            if (oldAttacker != session.getCurrentAttacker()) {
                isDefensePhase = false
                currentTurnPlayerIdx = session.attackerIdx
                guiTurnHistory.add("${player.name} ПЕРЕВЕЛ ход картой ${selectedCard.rank}")
            } else {
                val actionName = if (!isDefensePhase) "атаковал" else "отбил"
                guiTurnHistory.add("${player.name} $actionName: ${selectedCard.rank} ${selectedCard.suit}")
            }
            onModelChanged()
        } catch (e: Exception) {
            showError(e.message ?: "Ход невозможен")
        }
    }

    private fun handleFinishInteractionClick() {
        if (!isDefensePhase) {
            if (table.slots.isEmpty()) {
                showError("Нужно выложить хотя бы одну карту для атаки")
                return
            }
            isDefensePhase = true
            currentTurnPlayerIdx = players.indexOf(session.getCurrentDefender())
            guiTurnHistory.add("Атака зафиксирована. Очередь защиты.")
        } else {
            val allBeaten = table.slots.all { it.isBeaten() }
            if (!allBeaten) {
                showError("Нужно отбить все карты на столе")
                return
            }
            isDefensePhase = false
            currentTurnPlayerIdx = players.indexOf(session.getCurrentAttacker())
            guiTurnHistory.add("Карты отбиты успешно.")
        }
        showPassScreen(players[currentTurnPlayerIdx])
    }

    private fun handleActionClick() {
        if (!isDefensePhase) {
            guiTurnHistory.add("Раунд завершен: БИТО")
            session.finishRound()
            currentTurnPlayerIdx = session.attackerIdx
            isDefensePhase = false
            roundCount++
        } else {
            guiTurnHistory.add("${session.getCurrentDefender().name} ПРИНЯЛ карты со стола")
            session.takeCards()
            currentTurnPlayerIdx = session.attackerIdx
            isDefensePhase = false
            roundCount++
        }
        showPassScreen(players[currentTurnPlayerIdx])
    }

    private fun showPassScreen(nextPlayer: Player) {
        val label = Label("ПЕРЕДАЙТЕ УПРАВЛЕНИЕ ИГРОКУ:\n\n${nextPlayer.name}").apply {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-alignment: center; -fx-text-fill: white;"
        }
        val btn = Button("Я ГОТОВ, ПОКАЗАТЬ КАРТЫ").apply {
            style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #3498db; -fx-text-fill: white; -fx-min-height: 40px;"
        }

        val passLayout = VBox(25.0, label, btn).apply {
            alignment = Pos.CENTER
            padding = Insets(50.0)
            style = "-fx-background-color: #2c3e50;"
        }
        val oldScene = primaryStage.scene
        primaryStage.scene = Scene(passLayout, 1030.0, 600.0)
        btn.setOnAction {
            primaryStage.scene = oldScene
            onModelChanged()
        }
    }
    private fun giveCards(player: Player) {
        while (player.hand.size < 6 && deck.remaining() > 0) {
            val card = deck.draw() ?: break
            player.hand.add(card)
        }
    }
    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR, message)
        alert.showAndWait()
    }
}
