package state

import domain.TestEvent
import domain.TestStateObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StateMachineCreationTest {
    companion object {
        private val DUMMY_EVENT = TestEvent("Whatever")
    }

    private val sampleStateModel = """
        { 
            NEW: { orderPlaced: "OR" },
            OR: { 
                accept: "ACCEPTED",
                reject: "REJECTED"    
            },
            ACCEPTED: {},
            REJECTED: {}
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
        assertEquals("OR", processedEvent.value.state)
    }

    @Test
    fun `Engine throws an error if a State Object is inserted with an unknown state`() {
        val testStateObject = TestStateObject("UNKNOWN")
        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel)

        assertThrows<UnknownStateException> { stateMachine.processEvent(DUMMY_EVENT, testStateObject) }
    }

    @Test
    fun `Engine throws an error if an Event is given that is unknown`(){
        val testStateObject = TestStateObject("OR")
        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel)

        assertThrows<UnknownEventException> { stateMachine.processEvent(TestEvent("UNKNOWN"), testStateObject) }
    }

    @Test
    fun `throws error if initialized with inconsistent configuration`(){
        val missingStateConfiguration = """
        { 
            NEW: { orderPlaced: "OR" }            
        }""".trimIndent()

        assertThrows<IncorrectConfigException> { StateMachineFactory.createStateMachine(missingStateConfiguration)}
    }
}

