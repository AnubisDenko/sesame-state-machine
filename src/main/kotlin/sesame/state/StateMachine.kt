package sesame.state

import sesame.domain.*
import java.lang.Exception

class StateMachine(private val config: Map<State, Transitions>, val name: String) {
    init{
        val stateStrings = config.keys.map { state -> state.state }
        val unknownStates = config.values
            .map { it.values }
            .flatten()
            .filter { !stateStrings.contains(it.outputState.state) }

        if(unknownStates.isNotEmpty()){
            throw IncorrectConfigException("Unknown Sates configured in Transitions $unknownStates")
        }
    }

    fun processEvent(event: Event, stateObject: StateObject): Output {
        if(!config.containsKey(stateObject.value)) {
            throw UnknownStateException("Unknown State ${stateObject.value}")
        }

        val transitions = config[stateObject.value] ?: throw StateMachineException("No Transitions for given State ${stateObject.value} ")
        if(!transitions.containsKey(event.name)){
            throw  UnknownEventException("Unknown Event ${event.name}")
        }

        val transition = transitions[event.name] ?: throw StateMachineException("No Transition configured for this event ${event.name}")

        // execute gates and sinks configured on the transition
        executeSinks(transition, event, stateObject)

        val gateResult = executeGates(transition, event, stateObject)

        return if( !gateResult.result){
            Output(stateObject, gateResult.errorMessage)
        }else{
            // TODO is it really the best approach that the state inside the StateObject is modified or should we leave that to the caller
            // as they might want to do something else. Alternative would be to simply return the resulting state. This would also solve state being mutable
            stateObject.value = State(transition.outputState.state)
            Output(stateObject)
        }
    }

    private fun executeSinks(transition: Transition, event: Event, stateObject: StateObject){
        transition.sinks.forEach { it.action(event, stateObject) }
    }

    private fun executeGates(transition: Transition, event: Event, stateObject: StateObject):GateResponse{
        return transition.gates.fold(GateResponse(true)) { acc, gate ->  acc + gate.accept(event, stateObject) }
    }
}


data class State(val state: String)

data class Transition(val eventName: String, val outputState: State, val sinks: List<Sink> = emptyList(), val gates: List<Gate> = emptyList()){
    override fun toString(): String {
        return "$eventName -> ${outputState.state}"
    }
}

class Transitions(private val transitions: Map<String, Transition>): HashMap<String, Transition>(transitions)

open class StateMachineException: Exception {
    constructor(): super()
    constructor(errorMessage: String): super(errorMessage)
}

class UnknownStateException(errorMessage: String): StateMachineException(errorMessage)
class UnknownEventException(errorMessage: String): StateMachineException(errorMessage)
class IncorrectConfigException(errorMessage: String): StateMachineException(errorMessage)
class StateMachineNotFoundException(errorMessage: String): StateMachineException(errorMessage)
