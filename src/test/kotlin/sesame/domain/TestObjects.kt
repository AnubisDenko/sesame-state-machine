package sesame.domain

import sesame.state.State

class TestEvent(override val name: String): Event
enum class TestStates(val state: State){
    ORDER_RECEIVED( State("ORDER_RECEIVED")),
    NEW( State("NEW")),
    ACCEPTED( State("ACCEPTED")),
    UNKNOWN(State("UNKNOWN"))
}
