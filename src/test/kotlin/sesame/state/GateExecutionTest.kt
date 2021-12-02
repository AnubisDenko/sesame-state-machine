package sesame.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sesame.domain.FlexibleGateResponse
import sesame.domain.TestEvent
import sesame.domain.TestStateObject
import kotlin.math.exp

class GateExecutionTest {
    private val testEvent = TestEvent("orderPlaced")
    private val initialState = "NEW" // TODO currently the state inside the stateObject is mutable, which it shouldn't be. need to fix later
    private val testStateObject = TestStateObject(initialState)

    @BeforeEach
    fun resetStateMachine(){
        StateMachineFactory.clearAllStateMachines()
        FlexibleGateResponse.reset()
    }

    @Test
    fun `if a gate check is negative the transition will be blocked and we will receive the original state as target state`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysBlockGate"))

        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals(initialState, result.stateObject.value.state)
    }

    @Test
    fun `if a gate check is positive the transition will proceed`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysPassGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals("OR", result.stateObject.value.state)
    }

    @Test
    fun `blocks if one of the gates configured is failing`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysPassGate","sesame.domain.AlwaysBlockGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals(initialState, result.stateObject.value.state)
    }

    @Test
    fun `passes if all gates are successful`(){
        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.AlwaysPassGate","sesame.domain.AlwaysPassGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        assertEquals("OR", result.stateObject.value.state)
    }

    @Test
    fun `Gates can return a description of why they blocked the StateObject transition`(){
        val expectedErrorMessage = "I was setup to fail without reason"
        FlexibleGateResponse.setupNextResponse(false, expectedErrorMessage)

        val stateMachine = StateMachineFactory.createStateMachine(setupTemplate("sesame.domain.FlexibleGate"))
        val result = stateMachine.processEvent(testEvent, testStateObject)
        with(result){
            assertEquals(initialState, stateObject.value.state)
            assertEquals(1, messages.size)
            assertEquals(expectedErrorMessage, messages.first())
        }
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
