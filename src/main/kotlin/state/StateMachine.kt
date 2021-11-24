package state

import domain.Event
import domain.StateObject
import java.lang.Exception

class StateMachine(private val config: Map<State, Transitions>, val name: String) {
    init{
        val stateStrings = config.keys.map { state -> state.state }
        val unknownStates = config.values
            .map { it.values }
            .flatten()
            .filter { !stateStrings.contains(it) }

        if(unknownStates.isNotEmpty()){
            throw IncorrectConfigException("Unknown Sates configured in Transitions $unknownStates")
        }
    }

    fun processEvent(event:Event, stateObject: StateObject): StateObject {
        if(!config.containsKey(stateObject.value)) {
            throw UnknownStateException("Unknown State ${stateObject.value}")
        }

        val transitions = config[stateObject.value] ?: throw StateMachineException("No Transitions for given State")
        if(!transitions.containsKey(event.name)){
            throw  UnknownEventException("Unknown Event ${event.name}")
        }

        val resultStateString = transitions[event.name] ?: throw StateMachineException("No result state configured for this event")
        stateObject.value = State(resultStateString)
        return stateObject
    }
}


data class State(val state: String)

data class Transition(val eventName: String, val outputState: State)

class Transitions(private val transitions: Map<String,String>): HashMap<String, String>(transitions)

open class StateMachineException: Exception {
    constructor(): super()
    constructor(errorMessage: String): super(errorMessage)
}

class UnknownStateException(errorMessage: String): StateMachineException(errorMessage)
class UnknownEventException(errorMessage: String): StateMachineException(errorMessage)
class IncorrectConfigException(errorMessage: String): StateMachineException(errorMessage)
