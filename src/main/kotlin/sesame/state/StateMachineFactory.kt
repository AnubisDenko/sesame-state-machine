@file:Suppress("UNCHECKED_CAST")
package sesame.state

object StateMachineFactory {
    private val stateMachines = HashMap<String, StateMachine<out Any?>>() as MutableMap<String, StateMachine<out Any?>>

    val clearAllStateMachines: () -> Unit = { stateMachines.clear() }

    fun <T> getStateMachineByKey(key: String): StateMachine<T> {
        val stateMachine = stateMachines[key] ?: throw StateMachineNotFoundException("Unknown Statemachine $key")

        return stateMachine as StateMachine<T>
    }

    fun <T> createStateMachine(jsonDescription: String, key: String = "DEFAULT"): StateMachine<T> {
        val machineConfig = StateMachineConfig<T>(jsonDescription)
        return createStateMachine(machineConfig, key)
    }

    fun <T> createStateMachine(machineConfig: StateMachineConfig<T>, key: String = "DEFAULT"): StateMachine<T> {
        val result = stateMachines[key] ?: StateMachine(machineConfig, key)
        stateMachines[key] = result
        return result as StateMachine<T>
    }


}
