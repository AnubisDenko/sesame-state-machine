package sesame.graph

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import sesame.state.StateMachineConfig
import java.io.File

class StateGraphGenerationTest {
    private val generator = GraphGenerator<Any>()

    @Test
    fun `creates a correct graph for sinks based on the json config using plantuml`(){
        val config = createStateMachineConfigForFile("/simpleStateModelWithSink.json")
        val output = generator.generatePlantumlConfigFromStateMachineConfig(config)
        assertEquals(expectedModelWithSink, output.trimIndent())

    }

    @Test
    fun `creates a correct plantuml graph for config that contains gates`(){
        val config = createStateMachineConfigForFile("/simpleStateModelWithGate.json")
        val output = generator.generatePlantumlConfigFromStateMachineConfig(config)

        assertEquals(expectedModelWithGate, output.trimIndent())
    }

    @Test
    fun `output complex model with both gates and sinks`(){
        val expectedOutput = object{}.javaClass.getResource("/expectedPlantUmlOutput_complexModel.plantuml")!!.readText().trimIndent()
        val config = createStateMachineConfigForFile("/complexModel.json")
        val output = generator.generatePlantumlConfigFromStateMachineConfig(config)

        assertEquals(expectedOutput, output.trimIndent())
    }

    @Test
    fun `writes File to Disk`(){
        val fileName = "generated_model.png"
        val outputFile = File(fileName)
        assertFalse(outputFile.exists())

        val config = createStateMachineConfigForFile("/complexModel.json")
        generator.generatePngAndSaveToDisk(fileName, config)

        assertTrue(outputFile.exists())
        outputFile.delete()
    }


    private fun createStateMachineConfigForFile(configPath: String): StateMachineConfig<Any> {
        val stateModel = object {}.javaClass.getResource(configPath)!!.readText().trimIndent()
        return StateMachineConfig<Any>(stateModel)
    }

    private val expectedModelWithSink = """
        @startuml
        left to right direction
        'states'
        state ORDER_RECEIVED
        state NEW
        state REJECTED
        state ACCEPTED
        state fork_Accept <<fork>>
        state testSink{
            testSink: sesame.domain.DummyStorageSink
        }
        
        ' state transitions
        NEW --> ORDER_RECEIVED: orderPlaced
        ORDER_RECEIVED --> REJECTED: reject
        ORDER_RECEIVED --> fork_Accept: accept
        fork_Accept --> testSink
        fork_Accept --> ACCEPTED
        
        @enduml
        """.trimIndent()

    private val expectedModelWithGate = """
        @startuml
        left to right direction
        'states'
        state ORDER_RECEIVED
        state NEW
        state NEW_Gate <<choice>>
        note right of NEW_Gate
            AlwaysBlockGate: [sesame.domain.AlwaysBlockGate]
        end note
        
        ' state transitions
        NEW --> NEW_Gate: orderPlaced
        NEW_Gate --> ORDER_RECEIVED: success
        NEW_Gate --> NEW: failure
        
        @enduml
    """.trimIndent()
}