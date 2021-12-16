package sesame.graph

import net.sourceforge.plantuml.SourceStringReader
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File

class GraphPlayground {

    @Test
    fun `try out plantuml API`(){
        val source = """
            @startuml
            Bob -> Alice : hello
            @enduml
        """.trimIndent()

        val reader = SourceStringReader(source)
        val out = ByteArrayOutputStream()

        reader.outputImage(out)

        File("test.png").writeBytes(out.toByteArray())
    }
}