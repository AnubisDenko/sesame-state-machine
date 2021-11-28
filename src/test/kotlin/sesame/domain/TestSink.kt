package sesame.domain

import sesame.domain.Event
import sesame.domain.Sink
import sesame.domain.StateObject

class TestSink(override val name: String = "TestSink"): Sink {
    private var event: Event? = null
    private var stateObject: StateObject? = null

    override fun action(event: Event, stateObject: StateObject) {
        DummyDataStore.receivedEventForObject(name, event, stateObject)
    }

}

object DummyDataStore{
    val writtenValues = mutableListOf<Triple<String, Event, StateObject>>()

    fun receivedEventForObject(sinkName: String, event: Event, stateObject: StateObject){
        writtenValues.add(Triple(sinkName, event, stateObject))
    }

    val clear: () -> Unit = { writtenValues.clear() }
}