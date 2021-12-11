package sesame.state

import sesame.domain.TestEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sesame.domain.TestStates.*


class StateMachineTest {
    companion object {
        private val DUMMY_EVENT = TestEvent("Whatever")
    }

    private val sampleStateModel = object {}.javaClass.getResource("/simpleStateModel.json")!!.readText().trimIndent()

    @Test
    fun `can create a statemachine with a given name`(){
        val name = "MyStateMachine"
        val stateMachine = StateMachineFactory.createStateMachine<Any>(sampleStateModel, name)
        assertEquals(name, stateMachine.name)
    }

    @Test
    fun `doesn't create two state machines with same name but instead returns the previously created one`(){
        val name = "MyStateMachine"
        val stateMachine = StateMachineFactory.createStateMachine<Any>(sampleStateModel, name)
        val stateMachineAgain = StateMachineFactory.createStateMachine<Any>(sampleStateModel, name)

        assertTrue(stateMachine == stateMachineAgain) // reference comparison to see if same instance.
        assertEquals(stateMachine.name, stateMachineAgain.name)
    }

    @Test
    fun `can retrieve the already created statemachine if needed`(){
        val name = "MyTestMachine"
        val stateMachine = StateMachineFactory.createStateMachine<Any>(sampleStateModel, name)
        val retrievedMachine = StateMachineFactory.getStateMachineByKey<Any>(name)

        assertTrue(stateMachine == retrievedMachine)
        assertEquals(stateMachine.name, retrievedMachine.name)
    }

    @Test
    fun `transitions from NEW to OR when orderPlaced event occurs on domain object`(){
        val event = TestEvent("orderPlaced")

        val stateMachine = StateMachineFactory.createStateMachine<Any>(sampleStateModel)

        val processedEvent = stateMachine.processEvent(event, NEW.state, Any())
        assertEquals(ORDER_RECEIVED.state, processedEvent.state)
    }

    @Test
    fun `Engine throws an error if a State Object is inserted with an unknown state`() {
        val stateMachine = StateMachineFactory.createStateMachine<Any>(sampleStateModel)

        assertThrows<UnknownStateException> { stateMachine.processEvent(DUMMY_EVENT, UNKNOWN.state, Any()) }
    }

    @Test
    fun `Engine throws an error if an Event is given that is unknown`(){
        val stateMachine = StateMachineFactory.createStateMachine<Any>(sampleStateModel)
        assertThrows<UnknownEventException> { stateMachine.processEvent(TestEvent("UNKNOWN"), ORDER_RECEIVED.state, Any()) }
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

        assertThrows<IncorrectConfigException> { StateMachineFactory.createStateMachine<Any>(missingStateConfiguration)}
    }
}

