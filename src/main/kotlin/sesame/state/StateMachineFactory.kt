package sesame.state

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import sesame.domain.FieldNames
import sesame.domain.Gate
import sesame.domain.Sink

object StateMachineFactory {
    private val stateMachines = HashMap<String, StateMachine>() as MutableMap<String, StateMachine>

    val clearAllStateMachines: () -> Unit = { stateMachines.clear() }
    val getStateMachineByKey: (key: String) -> StateMachine = { key -> stateMachines[key] ?: throw StateMachineNotFoundException("Unknown Statemachine $key") }

    fun createStateMachine(jsonDescription: String, key: String = "DEFAULT"): StateMachine {
        val machineConfig = createMachineConfig(jsonDescription)

        val result = stateMachines[key] ?: StateMachine(machineConfig, key)
        stateMachines[key] =  result

        return result
    }

    private fun createMachineConfig(jsonDescription: String): Map<State, Transitions>{
        val tree = JsonParser.parseString(jsonDescription).asJsonObject
        val beforeStates = tree.keySet()
        return beforeStates.associate { beforeState ->
            State(beforeState) to readTransitions(tree[beforeState].asJsonObject)
        }

    }

    private fun readTransitions(transitions: JsonObject): Transitions {
        val result = transitions.entrySet().associate { (key, value) -> key to createTransition(key, value.asJsonObject) }
        return Transitions(result)
    }

    private fun createTransition(eventName: String, value: JsonObject): Transition {
        val targetStateName = value.get(FieldNames.TransitionFields.NextState.value).asString

        val sinks: List<Sink> = if(value.get(FieldNames.TransitionFields.Sinks.value) != null) {
             createSinks(value.get(FieldNames.TransitionFields.Sinks.value).asJsonArray)
        } else emptyList()

        val gates: List<Gate> = if(value.get(FieldNames.TransitionFields.Gates.value) != null ){
            createGates(value.get(FieldNames.TransitionFields.Gates.value).asJsonArray)
        } else emptyList()

        return Transition(eventName, State(targetStateName), sinks, gates)
    }

    private fun createSinks(sinksConfig: JsonArray): List<Sink>{
        return sinksConfig.map {
            val sinkConfig = it.asJsonObject
            val className = sinkConfig.get(FieldNames.TransitionFields.SinkFields.Class.value).asString
            val name = sinkConfig.get(FieldNames.TransitionFields.SinkFields.Name.value).asString
            Class.forName(className).kotlin.constructors.first().call(name) as Sink
        }
    }

    private fun createGates(gateConfig: JsonArray): List<Gate>{
        return gateConfig.map {
            val sinkConfig = it.asJsonObject
            val className = sinkConfig.get(FieldNames.TransitionFields.GateFields.Class.value).asString
            val name = sinkConfig.get(FieldNames.TransitionFields.GateFields.Name.value).asString
            Class.forName(className).kotlin.constructors.first().call(name) as Gate
        }
    }
}
