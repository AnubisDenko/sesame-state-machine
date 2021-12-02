package sesame.state

import sesame.domain.StateObject

data class Output(val stateObject: StateObject, val messages: List<String> = emptyList())