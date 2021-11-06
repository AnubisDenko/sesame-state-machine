package state

import domain.Event
import domain.StateObject
import java.lang.Exception

// This needs to change as you probably want to have multiple state machines. Ideally you have a factory or just
// a container where you can get the appropriate instance in some way

object StateMachine {
    fun processEvent(event:Event, stateObject: StateObject): StateObject {
        when(event){
            Event.PROCESS -> stateObject.state = "PROCESSED"
            else -> throw StateMachineException("Unknown Event was received")
        }

        return stateObject
    }
}

class StateMachineException: Exception {
    constructor(): super()
    constructor(errorMessage: String): super(errorMessage)
}