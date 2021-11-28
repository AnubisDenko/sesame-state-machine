package sesame.domain

class AlwaysBlockGate(override val name: String): Gate {
    override fun accept(event: Event, stateObject: StateObject): Boolean {
        return false
    }
}

class AlwaysPassGate(override val name: String) : Gate {
    override fun accept(event: Event, stateObject: StateObject): Boolean {
        return true
    }
}