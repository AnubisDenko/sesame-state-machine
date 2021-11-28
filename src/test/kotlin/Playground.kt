import sesame.domain.Sink
import kotlin.reflect.KClass

fun main() {
    val className = "domain.TestSink"
    val name = "TestSink"

    val sinkClass = Class.forName(className).kotlin
    val sinkObject = sinkClass.constructors.first().call(name) as Sink
    println("And Stop for debugging")


}

