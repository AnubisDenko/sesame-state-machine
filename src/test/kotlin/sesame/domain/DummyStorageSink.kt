package sesame.domain

class DummyStorageSink(override val name: String = "TestSink") : Sink {
    override fun action(event: Event, stateObject: Any) {
        DummyDataStore.receivedEventForObject(name, event, stateObject)
    }

}

object DummyDataStore {
    val writtenValues = mutableListOf<Triple<String, Event, Any>>()

    fun receivedEventForObject(sinkName: String, event: Event, stateObject: Any) {
        writtenValues.add(Triple(sinkName, event, stateObject))
    }
    val clear: () -> Unit = { writtenValues.clear() }
}