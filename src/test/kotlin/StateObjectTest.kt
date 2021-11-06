import domain.Event
import domain.StateObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import state.StateMachine
import state.StateMachineException

class StateObjectTest {

    @Test
    fun `can mutate the state object and override`(){
        val testObject = TestStateObject("Test")
        Assertions.assertEquals("Test", testObject.state)
    }

    @Test
    fun `the statemachine can mutate the object based on its state and a given event`(){
        val startObject = TestStateObject()

        val resultObject = StateMachine.processEvent(Event.PROCESS, startObject)

        Assertions.assertEquals("PROCESSED", resultObject.state)
    }

    @Test
    fun `the statemachine throws an exception if an unknown event is received`(){
        assertThrows<StateMachineException> {  StateMachine.processEvent(Event.UNKNOWN, TestStateObject()) }
    }


    private class TestStateObject(initialState: String = "NEW"): StateObject(initialState)
}