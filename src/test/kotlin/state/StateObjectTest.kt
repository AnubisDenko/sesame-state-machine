package state

import domain.TestEvent
import domain.TestStateObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StateObjectTest {
    private var stateMachine = StateMachine(mapOf(),"")

    @Test
    fun `can mutate the state object and override`(){
        val testObject = TestStateObject("Test")
        assertEquals("Test", testObject.value.state)
    }

    @Test
    fun `the statemachine throws an exception if an unknown event is received`(){
        assertThrows<StateMachineException> {  stateMachine.processEvent(TestEvent("UNKNOWN"), TestStateObject()) }
    }
}