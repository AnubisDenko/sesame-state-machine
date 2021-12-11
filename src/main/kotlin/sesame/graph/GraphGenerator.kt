package sesame.graph

import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.security.SFile
import sesame.state.StateMachineConfig

class GraphGenerator<T> {
    fun generatePlantumlConfigFromStateMachineConfig(config: StateMachineConfig<T>): String{
        val allStates = getAllStatesFromConfig(config)

        val configStringBuilder = StringBuilder()
        with(configStringBuilder){
            appendLine("@startuml")
            appendLine("left to right direction") // to force plantuml to always draw left to right. seems to look better
            appendLine("'states'")
            appendLine(createStateSetup(allStates))
            appendLine("' state transitions")
            appendLine(generateAllTransitions(config))
            appendLine("@enduml")
        }

        return configStringBuilder.toString()
    }

    fun generatePngAndSaveToDisk(path: String, config: StateMachineConfig<T>) {
        val plantumlConfig = generatePlantumlConfigFromStateMachineConfig(config)
        println(plantumlConfig)
        val reader = SourceStringReader(plantumlConfig)
        val file = SFile(path)
        reader.outputImage(file)
    }

    private fun generateAllTransitions(config: StateMachineConfig<T>): String {
        val allTransition = config.states.map { config.getTransitionsForState(it) }.flatMap { it.values }
        val builder = StringBuilder()

        with(builder){
            allTransition.forEach { transition ->
                // if sinks exist we create a dummy state so that gates and sinks can be attached to that one
                val originState = if(transition.sinks.isNotEmpty()){
                    getFakeState(transition.eventName)
                }else{
                    transition.inputState.state
                }

                if(transition.sinks.isNotEmpty()){
                    appendLine("${transition.inputState.state} --> ${originState}: ${transition.eventName}")
                    transition.sinks.forEach { sink ->
                        appendLine("$originState --> ${sink.name}")
                    }
                    if(transition.gates.isEmpty()){
                        appendLine("$originState --> ${transition.outputState.state}")
                    }
                }

                if(transition.gates.isNotEmpty()) {
                        val gateName = getGateName(transition.inputState.state)
                        appendLine("$originState --> $gateName: ${transition.eventName}")
                        appendLine("$gateName --> ${transition.outputState.state}: success")
                        appendLine("$gateName --> ${transition.inputState.state}: failure")
                }

                if(transition.sinks.isEmpty() && transition.gates.isEmpty()) {
                    appendLine("${transition.inputState.state} --> ${transition.outputState.state}: ${transition.eventName}")
                }
            }
        }

        return builder.toString()
    }

    private fun createStateSetup(states: Collection<GraphState>): String {
        val builder = StringBuilder()
        states.forEach { builder.appendLine(it.toString()) }
        return builder.toString()
    }

    private fun getAllStatesFromConfig(config: StateMachineConfig<T>): Collection<GraphState>{
        val allTransition = config.states.map { config.getTransitionsForState(it) }.flatMap { it.values }

        val states = HashSet<GraphState>().toMutableSet()

        for (transition in allTransition){
            states.add(GraphState(Type.State, transition.outputState.state))
            states.add(GraphState(Type.State, transition.inputState.state))

            // create the fork for the sinks if there are any
            if(transition.sinks.isNotEmpty()){
                states.add(GraphState(Type.Fork, getFakeState(transition.eventName)))
            }

            // create the state including the comments for all sinks
            transition.sinks.forEach {
                states.add(GraphState(Type.Sink, transition.eventName, it.name, it.javaClass.name))
            }


            if(transition.gates.isNotEmpty()) {
                val gateComment = transition.gates.joinToString { "${it.name}: [${it.javaClass.name}]" }
                states.add(GraphState(Type.Gate, getGateName(transition.inputState.state), additionalDescription =  gateComment))
            }

        }
        return states
    }

    private val getGateName: (String) -> String = { it + "_Gate"}

}

private enum class Type {
    Fork, Sink, State, Gate
}

private data class GraphState(val type: Type, val name: String, val additionalName: String? = null, val additionalDescription: String? = null){
    override fun toString(): String {
        return when(type){
            Type.State -> "state $name"
            Type.Fork -> "state $name <<fork>>"
            Type.Sink -> """
                state $additionalName{
                    $additionalName: $additionalDescription
                }
            """.trimIndent()
            Type.Gate -> """
                state $name <<choice>>
                note right of $name
                    $additionalDescription
                end note
            """.trimIndent()
        }
    }
}

private val getFakeState: (String) -> String = { inputString -> "fork_${inputString.replaceFirstChar { it.uppercase() }}"}