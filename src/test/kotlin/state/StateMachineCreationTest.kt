package state

import domain.TestEvent
import domain.TestStateObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StateMachineCreationTest {
    private val sampleStateModel = """
        { 
            NEW: { orderPlaced: "OR" },
            OR: { 
                accept: "ACCEPTED",
                reject: "REJECTED"    
            }
        }
        
    """.trimIndent()


    @Test
    fun `can create a statemachine with a given name`(){
        val name = "MyStateMachine"
        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel, name)
        assertEquals(name, stateMachine.name)
    }

    @Test
    fun `doesn't create two state machines with same name but instead returns the previously created one`(){
        val name = "MyStateMachine"
        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel, name)
        val stateMachineAgain = StateMachineFactory.createStateMachine(sampleStateModel, name)

        assertTrue(stateMachine == stateMachineAgain) // reference comparison to see if same instance.
        assertEquals(stateMachine.name, stateMachineAgain.name)
    }

    @Test
    fun `transitions from NEW to OR when orderPlaced event occurs on domain object`(){
        val testStateObject = TestStateObject("NEW")
        val event = TestEvent("orderPlaced")

        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel)

        val processedEvent = stateMachine.processEvent(event, testStateObject)
        assertEquals("OR", processedEvent.state.state)
    }
}