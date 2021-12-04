package sesame.state

import sesame.domain.*
import java.lang.Exception

class StateMachine<T>(private val config: Map<State, Transitions<T>>, val name: String) {
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

    fun processEvent(event: Event, state: State, stateObject: T): Output {

        if(!config.containsKey(state)) {
            throw UnknownStateException("Unknown State $state")
        }

        val transitions = config[state] ?: throw StateMachineException("No Transitions for given State $state ")
        if(!transitions.containsKey(event.name)){
            throw  UnknownEventException("Unknown Event ${event.name}")
        }

        val transition = transitions[event.name] ?: throw StateMachineException("No Transition configured for this event ${event.name}")

        // execute gates and sinks configured on the transition
        executeSinks(transition, event, stateObject)

        val gateResult = executeGates(transition, event, stateObject)

        return if( !gateResult.result){
            Output(state, gateResult.errorMessage)
        }else{
            Output(State(transition.outputState.state))
        }
    }

    private fun executeSinks(transition: Transition<T>, event: Event, stateObject: T){
        transition.sinks.forEach { it.action(event, stateObject) }
    }

    private fun executeGates(transition: Transition<T>, event: Event, stateObject: T):GateResponse{
        return transition.gates.fold(GateResponse(true)) { acc, gate ->  acc + gate.accept(event, stateObject) }
    }
}


data class State(val state: String)

data class Transition<T>(val eventName: String, val outputState: State, val sinks: List<Sink<T>> = emptyList(), val gates: List<Gate<T>> = emptyList()){
    override fun toString(): String {
        return "$eventName -> ${outputState.state}"
    }
}

class Transitions<T>(private val transitions: Map<String, Transition<T>>): HashMap<String, Transition<T>>(transitions)

open class StateMachineException: Exception {
    constructor(): super()
    constructor(errorMessage: String): super(errorMessage)
}

class UnknownStateException(errorMessage: String): StateMachineException(errorMessage)
class UnknownEventException(errorMessage: String): StateMachineException(errorMessage)
class IncorrectConfigException(errorMessage: String): StateMachineException(errorMessage)
class StateMachineNotFoundException(errorMessage: String): StateMachineException(errorMessage)
