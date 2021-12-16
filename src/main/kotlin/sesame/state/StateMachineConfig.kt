@file:Suppress("UNCHECKED_CAST")
package sesame.state

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import sesame.domain.FieldNames
import sesame.domain.Gate
import sesame.domain.Sink

class StateMachineConfig<T>(jsonConfig: String){
    private val underlyingConfig = createStateMachineConfig<T>(jsonConfig)

    init {
        verify()
    }

    val states: Set<State>
        get() { return underlyingConfig.keys }

    val containsState: (State) -> Boolean = { state -> underlyingConfig.containsKey(state)}
    val getTransitionsForState: (State) -> Transitions<T> = { underlyingConfig[it] ?: throw UnknownStateException("No Transitions for given State $it" )}

    private fun verify(){
        val stateStrings = underlyingConfig.keys.map { state -> state.state }
        val unknownStates = underlyingConfig.values
            .map { it.values }
            .flatten()
            .filter { !stateStrings.contains(it.outputState.state) }

        if(unknownStates.isNotEmpty()){
            throw IncorrectConfigException("Unknown Sates configured in Transitions $unknownStates")
        }
    }

    private fun <T> createStateMachineConfig(jsonDescription: String): Map<State, Transitions<T>>{
        val tree = JsonParser.parseString(jsonDescription).asJsonObject
        val beforeStates = tree.keySet()
        return beforeStates.associate { stateString ->
                val inputState = State( stateString)
                inputState to readTransitions<T>(inputState, tree[stateString].asJsonObject)
            }
    }

    private fun <T> readTransitions(inputState: State, transitions: JsonObject): Transitions<T> {
        val result = transitions.entrySet().associate { (key, value) -> key to createTransition<T>(
            inputState,
            key,
            value.asJsonObject)
        }
        return Transitions(result)
    }

    private fun <T>createTransition(inputState: State, eventName: String, value: JsonObject): Transition<T> {
        val targetStateName = value.get(FieldNames.TransitionFields.NextState.value).asString

        val sinks: List<Sink<T>> = if(value.get(FieldNames.TransitionFields.Sinks.value) != null) {
            createSinks(value.get(FieldNames.TransitionFields.Sinks.value).asJsonArray)
        } else emptyList()

        val gates: List<Gate<T>> = if(value.get(FieldNames.TransitionFields.Gates.value) != null ){
            createGates(value.get(FieldNames.TransitionFields.Gates.value).asJsonArray)
        } else emptyList()

        return Transition(inputState, eventName, State(targetStateName), sinks, gates)
    }

    private fun <T> createSinks(sinksConfig: JsonArray): List<Sink<T>>{
        return sinksConfig.map {
            val sinkConfig = it.asJsonObject
            val className = sinkConfig.get(FieldNames.TransitionFields.SinkFields.Class.value).asString
            val name = sinkConfig.get(FieldNames.TransitionFields.SinkFields.Name.value).asString
            Class.forName(className).kotlin.constructors.first().call(name) as Sink<T>
        }
    }

    private fun <T> createGates(gateConfig: JsonArray): List<Gate<T>>{
        return gateConfig.map {
            val sinkConfig = it.asJsonObject
            val className = sinkConfig.get(FieldNames.TransitionFields.GateFields.Class.value).asString
            val name = sinkConfig.get(FieldNames.TransitionFields.GateFields.Name.value).asString
            Class.forName(className).kotlin.constructors.first().call(name) as Gate<T>
        }
    }
}