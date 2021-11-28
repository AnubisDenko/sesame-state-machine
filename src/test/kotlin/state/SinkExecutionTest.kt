package state

import domain.TestEvent
import domain.TestStateObject
import org.junit.jupiter.api.Test

class SinkExecutionTest {

    private val stateModel = object {}.javaClass.getResource("/simpleStateModelWithSink.json")!!.readText().trimIndent()

    @Test
    fun `sinks configured on a transition are triggered when even is received`(){
        val engine = StateMachineFactory.createStateMachine(stateModel)
        val testStateObject = TestStateObject("OR")
        val testEvent = TestEvent("accept")
    }


}