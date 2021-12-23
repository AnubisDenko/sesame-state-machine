package sesame.state.exceptions

import java.lang.Exception

open class StateMachineException : Exception {
    constructor() : super()
    constructor(errorMessage: String) : super(errorMessage)
}

class UnknownStateException(errorMessage: String) : StateMachineException(errorMessage)
class UnknownEventException(errorMessage: String) : StateMachineException(errorMessage)
class IncorrectConfigException(errorMessage: String) : StateMachineException(errorMessage)
class StateMachineNotFoundException(errorMessage: String) : StateMachineException(errorMessage)