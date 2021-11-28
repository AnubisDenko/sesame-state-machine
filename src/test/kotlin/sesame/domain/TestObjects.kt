package sesame.domain

import sesame.domain.Event
import sesame.domain.StateObject
import sesame.state.State

class TestStateObject(initialState: String = "NEW"): StateObject(State(initialState))
class TestEvent(override val name: String): Event