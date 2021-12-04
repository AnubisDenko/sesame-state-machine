package sesame.domain

interface Gate<T> {
    val name: String
    fun accept(event: Event, stateObject: T): GateResponse
}