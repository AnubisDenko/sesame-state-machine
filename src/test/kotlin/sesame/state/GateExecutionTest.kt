package sesame.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sesame.domain.TestEvent
import sesame.domain.TestStateObject

class GateExecutionTest {
    private val testEvent = TestEvent("orderPlaced")
    private val initialState = "NEW" // TODO currently the state inside the stateObject is mutable, which it shouldn't be. need to fix later
    private val testStateObject = TestStateObject(initialState)

    @BeforeEach
    fun resetStateMachine(){
        StateMachineFactory.clearAllStateMachines()
    }

    @Test
    fun `if a gate check is negative the transition will be blocked and we will receive the original state as target state`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysBlockGate"))

        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals(initialState, result.value.state)
    }

    @Test
    fun `if a gate check is positive the transition will proceed`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysPassGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals("OR", result.value.state)
    }

    @Test
    fun `blocks if one of the gates configured is failing`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysPassGate","sesame.domain.AlwaysBlockGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals(initialState, result.value.state)
    }

    @Test
    fun `passes if all gates are successful`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysPassGate","sesame.domain.AlwaysPassGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals("OR", result.value.state)
    }

    /**
     * to save some time and make it easy to setup multiple gates in a default configuration
     */
    private fun setupTemplate(vararg gateClassNames: String): String{
        val gates =  gateClassNames.map {
            """{
                "class": "$it",
                "name": "${it.split(".").last()}"
            }""".trimIndent()
        }
        return stateMachineTemplate.replace("%GATES%","$gates")
    }



    private val stateMachineTemplate = """
                {
                  "NEW": {
                    "orderPlaced": {
                      "nextState": "OR",
                      "gates": %GATES%
                    }
                  },
                  "OR": {}
                }
    """.trimIndent()
}