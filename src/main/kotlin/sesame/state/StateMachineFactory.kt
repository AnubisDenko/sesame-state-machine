@file:Suppress("UNCHECKED_CAST")

package sesame.state

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import sesame.domain.FieldNames
import sesame.domain.Gate
import sesame.domain.Sink

object StateMachineFactory {
    private val stateMachines = HashMap<String, StateMachine<out Any?>>() as MutableMap<String, StateMachine<out Any?>>

    val clearAllStateMachines: () -> Unit = { stateMachines.clear() }

    fun <T> getStateMachineByKey(key: String): StateMachine<T> {
        val stateMachine = stateMachines[key] ?: throw StateMachineNotFoundException("Unknown Statemachine $key")

        return stateMachine as StateMachine<T>
    }

    fun <T> createStateMachine(jsonDescription: String, key: String = "DEFAULT"): StateMachine<T> {
        val machineConfig = createMachineConfig<T>(jsonDescription)

        val result = stateMachines[key] ?: StateMachine(machineConfig, key)
        stateMachines[key] =  result
        return result as StateMachine<T>
    }

    private fun <T> createMachineConfig(jsonDescription: String): Map<State, Transitions<T>>{
        val tree = JsonParser.parseString(jsonDescription).asJsonObject
        val beforeStates = tree.keySet()
        return beforeStates.associate { beforeState ->
            State(beforeState) to readTransitions<T>(tree[beforeState].asJsonObject)
        }

    }

    private fun <T> readTransitions(transitions: JsonObject): Transitions<T> {
        val result = transitions.entrySet().associate { (key, value) -> key to createTransition<T>(key, value.asJsonObject) }
        return Transitions(result)
    }

    private fun <T>createTransition(eventName: String, value: JsonObject): Transition<T> {
        val targetStateName = value.get(FieldNames.TransitionFields.NextState.value).asString

        val sinks: List<Sink<T>> = if(value.get(FieldNames.TransitionFields.Sinks.value) != null) {
             createSinks(value.get(FieldNames.TransitionFields.Sinks.value).asJsonArray)
        } else emptyList()

        val gates: List<Gate<T>> = if(value.get(FieldNames.TransitionFields.Gates.value) != null ){
            createGates(value.get(FieldNames.TransitionFields.Gates.value).asJsonArray)
        } else emptyList()

        return Transition(eventName, State(targetStateName), sinks, gates)
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
