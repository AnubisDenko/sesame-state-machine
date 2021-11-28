package domain

class TestSink(override val name: String = "TestSink"):Sink {
    private var event: Event? = null
    private var stateObject: StateObject? = null

    override fun action(event: Event, stateObject: StateObject) {
        TODO("Not yet implemented")
    }

}