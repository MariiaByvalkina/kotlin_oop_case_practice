package infrastructure

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File

class DatabaseManagerTest {
    @Test
    fun testDatabaseCreation() {
        val testDbName = "test_create.db"
        val db = DatabaseManager(testDbName)

        val file = File(testDbName)
        assertTrue(file.exists())
        file.delete()
    }

    @Test
    fun testRankingEmpty() {
        val db = DatabaseManager("test_rank.db")
        val ranking = db.getGlobalRanking()

        assertEquals(0, ranking.size)

        File("test_rank.db").delete()
    }
}
