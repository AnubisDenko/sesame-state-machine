package state

import domain.Event
import domain.StateObject
import java.lang.Exception

class StateMachine(private val config: Map<State, Transitions>, val name: String) {
    fun processEvent(event:Event, stateObject: StateObject): StateObject {
        if(!config.containsKey(stateObject.state)) {
            throw StateMachineException("Unknown State ${stateObject.state}")
        }

        val transitions = config[stateObject.state] ?: throw StateMachineException("No Transitions for given State")
        if(!transitions.containsKey(event.name)){
            throw  StateMachineException("Unknown Event ${event.name}")
        }

        val resultStateString = transitions[event.name] ?: throw StateMachineException("No result state configured for this event")
        stateObject.state = State(resultStateString)
        return stateObject
    }
}


data class State(val state: String)

data class Transition(val eventName: String, val outputState: State)

class Transitions(private val transitions: Map<String,String>): HashMap<String, String>(transitions)

class StateMachineException: Exception {
    constructor(): super()
    constructor(errorMessage: String): super(errorMessage)
}