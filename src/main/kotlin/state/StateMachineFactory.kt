package state

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import domain.FieldNames

object StateMachineFactory {
    private val stateMachines = HashMap<String, StateMachine>() as MutableMap<String, StateMachine>

    fun createStateMachine(jsonDescription: String, key: String = "DEFAULT"): StateMachine{
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
        return Transition(eventName,State(targetStateName))
    }

}
