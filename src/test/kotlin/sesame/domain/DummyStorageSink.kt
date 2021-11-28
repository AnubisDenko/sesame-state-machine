package sesame.domain

class DummyStorageSink(override val name: String = "TestSink") : Sink {
    private var event: Event? = null
    private var stateObject: StateObject? = null

    override fun action(event: Event, stateObject: StateObject) {
        DummyDataStore.receivedEventForObject(name, event, stateObject)
    }

}

object DummyDataStore {
    val writtenValues = mutableListOf<Triple<String, Event, StateObject>>()

    fun receivedEventForObject(sinkName: String, event: Event, stateObject: StateObject) {
        writtenValues.add(Triple(sinkName, event, stateObject))
    }

    val clear: () -> Unit = { writtenValues.clear() }
}