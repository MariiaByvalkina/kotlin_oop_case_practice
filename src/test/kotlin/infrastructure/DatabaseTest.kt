package infrastructure

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class DatabaseTest {
    @Test
    fun testGetStats() {
        val db = DatabaseManager()
        val stats = db.getPlayerStats("user1")
        assertEquals("user1", stats.playerId)
    }
}
