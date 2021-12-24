package sesame.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sesame.state.exceptions.IncorrectConfigException

internal class StateMachineConfigTest{
    @Test
    fun `only allows one initial state per configuration`(){
        assertThrows<IncorrectConfigException> { StateMachineConfig<Any>(twoInitialStates) }
    }

    @Test
    fun `can extract initial state from config`(){
        val config = StateMachineConfig<Any>(oneInitialStates)
        assertEquals("NEW", config.initialState.state)
    }

    @Test
    fun `works if no initial state exists in config`(){
        assertNotNull(StateMachineConfig<Any>(noInitialState))
    }

    @Test
    fun `throws error if initialized with inconsistent configuration`(){
        val missingStateConfiguration = """
            {
              "NEW": {
                "orderPlaced": {
                  "nextState": "OR"
                }
              }
            }
        """.trimIndent()

        assertThrows<IncorrectConfigException> { StateMachineConfig<Any>(missingStateConfiguration)}
    }
}

private val twoInitialStates = """{
  "NEW": {
    "initialState": true
  },
  "ORDER_RECEIVED": {
    "initialState": true
  }
}
""".trimIndent()

private val oneInitialStates = """
      {"NEW": {
        "initialState": true
      }}
""".trimIndent()

private val noInitialState = """
{"NEW": {
    "action": {
      "nextState": "FINAL"
    }},
  "FINAL": {}}
""".trimIndent()