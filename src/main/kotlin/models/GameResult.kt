package models

sealed interface GameResult {

    object InProgress : GameResult
    object Draw : GameResult
    data class Winner(val player: Player) : GameResult
}
