package sesame.state

data class Output(val state: State, val messages: List<String> = emptyList())