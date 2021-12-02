package sesame.state

import sesame.domain.TestEvent
import sesame.domain.TestStateObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sesame.state.IncorrectConfigException
import sesame.state.StateMachineFactory
import sesame.state.UnknownEventException
import sesame.state.UnknownStateException

class StateMachineTest {
    companion object {
        private val DUMMY_EVENT = TestEvent("Whatever")
    }

    private val sampleStateModel = object {}.javaClass.getResource("/simpleStateModel.json")!!.readText().trimIndent()

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
    fun `can retrieve the already created statemachine if needed`(){
        val name = "MyTestMachine"
        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel, name)
        val retrievedMachine = StateMachineFactory.getStateMachineByKey(name)

        assertTrue(stateMachine == retrievedMachine)
        assertEquals(stateMachine.name, retrievedMachine.name)
    }

    @Test
    fun `transitions from NEW to OR when orderPlaced event occurs on domain object`(){
        val testStateObject = TestStateObject("NEW")
        val event = TestEvent("orderPlaced")

        val stateMachine = StateMachineFactory.createStateMachine(sampleStateModel)

        val processedEvent = stateMachine.processEvent(event, testStateObject)
        assertEquals("OR", processedEvent.stateObject.value.state)
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
              "NEW": {
                "orderPlaced": {
                  "nextState": "OR"
                }
              }
            }
        """.trimIndent()

        assertThrows<IncorrectConfigException> { StateMachineFactory.createStateMachine(missingStateConfiguration)}
    }
}

