@file:Suppress("UNCHECKED_CAST")

package sesame.state

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import sesame.domain.FieldNames
import sesame.domain.Gate
import sesame.domain.Sink
import sesame.state.exceptions.IncorrectConfigException
import sesame.state.exceptions.UnknownStateException

class StateMachineConfig<T>(jsonConfig: String) {
    private val underlyingConfig = createStateMachineConfig<T>(jsonConfig)

    init {
        verify()
    }

    val states: Set<State>
        get() {
            return underlyingConfig.keys
        }

    val initialState: State
        get() = internalInitialState

    private lateinit var internalInitialState: State

    val containsState: (State) -> Boolean = { state -> underlyingConfig.containsKey(state) }
    val getTransitionsForState: (State) -> Transitions<T> =
        { underlyingConfig[it] ?: throw UnknownStateException("No Transitions for given State $it") }

    private fun verify() {
        val stateStrings = underlyingConfig.keys.map { state -> state.state }
        val unknownStates = underlyingConfig.values
            .map { it.values }
            .flatten()
            .filter { !stateStrings.contains(it.outputState.state) }

        if (unknownStates.isNotEmpty()) {
            throw IncorrectConfigException("Unknown Sates configured in Transitions $unknownStates")
        }
    }

    private fun <T> createStateMachineConfig(jsonDescription: String): Map<State, Transitions<T>> {
        val tree = JsonParser.parseString(jsonDescription).asJsonObject
        val beforeStates = tree.keySet()

        setInitialState(tree)

        return beforeStates.associate { stateString ->
            val inputState = State(stateString)
            inputState to readTransitions<T>(inputState, tree[stateString].asJsonObject)
        }
    }

    private fun setInitialState(tree: JsonObject){
        val allInitStates = tree.entrySet().filter {  (_, value) -> value.asJsonObject.get("initialState") != null }

        when(allInitStates.size){
            0 -> return
            1 -> internalInitialState = State(allInitStates.first().key)
            else -> throw IncorrectConfigException("More than one initial state found in configuration")
        }
    }

    private fun <T> readTransitions(inputState: State, transitions: JsonObject): Transitions<T> {
        val result = transitions.entrySet().filter { (_, value) -> value.isJsonObject }.associate { (key, value) ->
            key to createTransition<T>(
                inputState,
                key,
                value.asJsonObject
            )
        }

        if(result.values.filter { it.isStp }.size > 1){
            throw IncorrectConfigException("${inputState.state} has more than one stp transitions")
        }

        return Transitions(result)
    }

    private fun <T> createTransition(inputState: State, eventName: String, value: JsonObject): Transition<T> {
        val targetStateName = value.get(FieldNames.TransitionFields.NextState.value).asString

        val sinks: List<Sink<T>> = if (value.get(FieldNames.TransitionFields.Sinks.value) != null) {
            createSinks(value.get(FieldNames.TransitionFields.Sinks.value).asJsonArray)
        } else emptyList()

        val gates: List<Gate<T>> = if (value.get(FieldNames.TransitionFields.Gates.value) != null) {
            createGates(value.get(FieldNames.TransitionFields.Gates.value).asJsonArray)
        } else emptyList()

        val isStpTransition = if (value.get("stp") == null) {
            false
        } else {
            value.get("stp").asBoolean
        }

        return Transition(inputState, eventName, State(targetStateName), isStpTransition, sinks, gates)
    }


    private fun <T> createSinks(sinksConfig: JsonArray): List<Sink<T>> {
        return sinksConfig.map {
            val sinkConfig = it.asJsonObject
            val className = sinkConfig.get(FieldNames.TransitionFields.SinkFields.Class.value).asString
            val name = sinkConfig.get(FieldNames.TransitionFields.SinkFields.Name.value).asString
            Class.forName(className).kotlin.constructors.first().call(name) as Sink<T>
        }
    }

    private fun <T> createGates(gateConfig: JsonArray): List<Gate<T>> {
        return gateConfig.map {
            val sinkConfig = it.asJsonObject
            val className = sinkConfig.get(FieldNames.TransitionFields.GateFields.Class.value).asString
            val name = sinkConfig.get(FieldNames.TransitionFields.GateFields.Name.value).asString
            Class.forName(className).kotlin.constructors.first().call(name) as Gate<T>
        }
    }
}