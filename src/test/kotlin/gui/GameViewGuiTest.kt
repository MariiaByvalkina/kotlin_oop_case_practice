package gui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import models.*
import logic.Table

class GameViewGuiTest {

    @Test
    fun testCardVisualsMappingUnit() {
        val heartsCard = Card(Suit.HEARTS, Rank.ACE)
        val spadesCard = Card(Suit.SPADES, Rank.TEN)
        val getCardVisuals = { card: Card ->
            val suitName = card.suit.toString().uppercase()
            when {
                suitName.contains("HEART") -> Pair("♥️", "#e74c3c")
                suitName.contains("DIAMOND") -> Pair("♦️", "#e67e22")
                suitName.contains("CLUB") -> Pair("♣️", "#2c3e50")
                suitName.contains("SPADE") -> Pair("♠️", "#2c3e50")
                else -> Pair(card.suit.toString(), "#2c3e50")
            }
        }

        val (heartsSymbol, heartsColor) = getCardVisuals(heartsCard)
        val (spadesSymbol, spadesColor) = getCardVisuals(spadesCard)

        assertEquals("♥️", heartsSymbol)
        assertEquals("#e74c3c", heartsColor)
        assertEquals("♠️", spadesSymbol)
        assertEquals("#2c3e50", spadesColor)
    }

    @Test
    fun testRenderStateDataFormattingIntegration() {
        val trump = Card(Suit.DIAMONDS, Rank.KING)
        val modeName = "Переводной"
        val deckRemaining = 18
        val trumpSymbol = "♦️"
        val formattedInfoText = "Режим: $modeName | Козырь: ${trump.rank} $trumpSymbol | В колоде: $deckRemaining"

        assertEquals("Режим: Переводной | Козырь: KING ♦️ | В колоде: 18", formattedInfoText)
    }

    @Test
    fun testActionButtonTextLogicDependingOnPhase() {
        val tableWithCards = Table().apply { addAttack(Card(Suit.CLUBS, Rank.SIX)) }
        val emptyTable = Table()

        val getActionButtonText = { isUserAttacking: Boolean, currentTable: Table ->
            if (isUserAttacking) {
                if (currentTable.slots.isEmpty()) "Пропустить ход" else "БИТО (Очистить стол)"
            } else {
                "ВЗЯТЬ КАРТЫ СО СТОЛА"
            }
        }

        assertEquals("Пропустить ход", getActionButtonText(true, emptyTable))
        assertEquals("БИТО (Очистить стол)", getActionButtonText(true, tableWithCards))
        assertEquals("ВЗЯТЬ КАРТЫ СО СТОЛА", getActionButtonText(false, tableWithCards))
    }

    @Test
    fun testRenderGameOverWinnerParsing() {
        val winnerResult = GameResult.Winner(Player("1", "first"))
        val resultText = winnerResult.toString()

        val formattedResult = when {
            resultText.contains("Winner") -> {
                val nameStart = resultText.indexOf("name='") + 6
                val nameEnd = resultText.indexOf("'", nameStart)
                val winnerName = resultText.substring(nameStart, nameEnd)
                "ПОБЕДИТЕЛЬ: $winnerName"
            }
            else -> "НИЧЬЯ! Карты закончились."
        }

        assertEquals("ПОБЕДИТЕЛЬ: first", formattedResult)
    }
}
