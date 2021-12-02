package sesame.domain

interface Gate {
    val name: String
    fun accept(event: Event, stateObject: StateObject): GateResponse
}