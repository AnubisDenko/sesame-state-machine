package sesame.state

import sesame.domain.*
import sesame.state.exceptions.StateMachineException
import sesame.state.exceptions.UnknownEventException
import sesame.state.exceptions.UnknownStateException
import java.util.NoSuchElementException

class StateMachine<T>(private val config: StateMachineConfig<T>, val name: String) {

    fun processEvent(event: Event, state: State, stateObject: T): Output {
        if (!config.states.contains(state)) {
            throw UnknownStateException("Unknown State $state")
        }


        val transitions = config.getTransitionsForState(state)
        if (!transitions.containsKey(event.name)) {
            throw  UnknownEventException("Unknown Event ${event.name}")
        }

        val transition = transitions[event.name] ?: throw StateMachineException("No Transition configured for this event ${event.name}")

        executeSinks(transition, event, stateObject)

        val gateResult = executeGates(transition, event, stateObject)

        if (!gateResult.result) {
            return Output(state, gateResult.errorMessage)
        }

        val stpEvent = config.getTransitionsForState(transition.outputState).getStpTransition()
        return if (stpEvent == null) {
            Output(State(transition.outputState.state))
        } else {
            processEvent(InternalEvent(stpEvent.eventName), transition.outputState, stateObject)
        }
    }

    private fun executeSinks(transition: Transition<T>, event: Event, stateObject: T) {
        transition.sinks.forEach { it.action(event, stateObject) }
    }

    private fun executeGates(transition: Transition<T>, event: Event, stateObject: T): GateResponse {
        return transition.gates.fold(GateResponse(true)) { acc, gate -> acc + gate.accept(event, stateObject) }
    }
}


data class State(val state: String)

data class Transition<T>(val inputState: State, val eventName: String, val outputState: State, val isStp: Boolean, val sinks: List<Sink<T>> = emptyList(), val gates: List<Gate<T>> = emptyList()) {
    override fun toString(): String {
        return "${inputState.state} -> $eventName -> ${outputState.state}"
    }
}

class Transitions<T>(private val transitions: Map<String, Transition<T>>) : HashMap<String, Transition<T>>(transitions) {
    fun getStpTransition(): Transition<T>? {
        return try {
            transitions.values.first { it.isStp }
        } catch (e: NoSuchElementException) {
            return null
        }
    }
}



private class InternalEvent(override val name: String) : Event