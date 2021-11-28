package state

import domain.DummyDataStore
import domain.TestEvent
import domain.TestStateObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SinkExecutionTest {

    private val stateModel = object {}.javaClass.getResource("/simpleStateModelWithSink.json")!!.readText().trimIndent()

    @Test
    fun `sinks configured on a transition are triggered when even is received`() {
        val engine = StateMachineFactory.createStateMachine(stateModel)
        val testStateObject = TestStateObject("OR")
        val testEvent = TestEvent("accept")

        engine.processEvent(testEvent, testStateObject)

        assertEquals(1, DummyDataStore.writtenValues.size)
        assertEquals("testSink", DummyDataStore.writtenValues[0].first)
        assertEquals(testEvent, DummyDataStore.writtenValues[0].second)
        assertEquals(testStateObject, DummyDataStore.writtenValues[0].third)
    }

    @Test
    fun `all sinks are triggered on a transition`() {
        val engine = StateMachineFactory.createStateMachine(multipleSinksOnTransition)
        val testStateObject = TestStateObject("NEW")
        val testEvent = TestEvent("orderPlaced")
        engine.processEvent(testEvent, testStateObject)

        assertEquals(2, DummyDataStore.writtenValues.size)
        assertEquals("testSink", DummyDataStore.writtenValues[0].first)
        assertEquals("testSink2", DummyDataStore.writtenValues[1].first)

    }

    private val multipleSinksOnTransition = """
        {
          "NEW": {
            "orderPlaced": {
              "nextState": "OR",
              "sinks": [
                {
                  "class": "domain.TestSink",
                  "name": "testSink"
                },
                {
                  "class": "domain.TestSink",
                  "name": "testSink2"
                }
              ]
            }
          },
          "OR": {}
        }
        """.trimIndent()
}