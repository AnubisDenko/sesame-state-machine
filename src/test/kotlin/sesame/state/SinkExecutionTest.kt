package sesame.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sesame.domain.DummyDataStore
import sesame.domain.TestEvent
import sesame.domain.TestStateObject

class SinkExecutionTest {

    private val stateModel = object {}.javaClass.getResource("/simpleStateModelWithSink.json")!!.readText().trimIndent()

    @BeforeEach
    fun reset(){
        DummyDataStore.clear()
        StateMachineFactory.clearAllStateMachines()
    }

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
                  "class": "sesame.domain.DummyStorageSink",
                  "name": "testSink"
                },
                {
                  "class": "sesame.domain.DummyStorageSink",
                  "name": "testSink2"
                }
              ]
            }
          },
          "OR": {}
        }
        """.trimIndent()
}