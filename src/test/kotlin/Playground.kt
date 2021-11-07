import com.google.gson.JsonObject
import com.google.gson.JsonParser
import state.State
import state.Transitions

fun main(){
    val sampleStateModel = """
        { 
            NEW: { orderPlaced: "OR" },
            OR: { 
                accept: "ACCEPTED",
                reject: "REJECTED"    
            }
        }
        
    """.trimIndent()

    val tree = JsonParser.parseString(sampleStateModel).asJsonObject
    val beforeStates = tree.keySet()
    val config = beforeStates.map { beforeState ->
        beforeState to readTransitions(tree[beforeState].asJsonObject)
    }.toMap()

    println("Stop")
}

fun readTransitions(transitions: JsonObject): Transitions {
    val result = transitions.entrySet().map { (key, value) ->  key to value.asString}.toMap()
    return Transitions(result)
}