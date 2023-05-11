package sbp.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContractHandlerTest {

    @Test
    fun `buildContractResponseFromFacade milestone state - in`() {
        // given
        val currentStates = listOf("reached", "odd one out", "closed")

        // when
        val actual = currentStates
            .filter { it in setOf("reached", "closed") }
            .size

        // then
        assertEquals(2, actual)
    }

    @Test
    fun `buildContractResponseFromFacade milestone state - this or that`() {
        // given
        val currentStates = listOf("reached", "odd one out", "closed")

        // when
        val actual = currentStates
            .filter { it == "reached" || it == "closed" }
            .size

        // then
        assertEquals(2, actual)
    }
}
