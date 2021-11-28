package sesame.domain

interface Sink {
    val name: String
    fun action(event: Event, stateObject: StateObject)
}