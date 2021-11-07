package domain

import state.State

class TestStateObject(initialState: String = "NEW"): StateObject(State(initialState))
class TestEvent(override val name: String): Event