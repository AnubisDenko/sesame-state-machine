package sesame.domain

interface Sink<T> {
    val name: String
    fun action(event: Event, stateObject: T)
}