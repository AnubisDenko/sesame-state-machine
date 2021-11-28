package domain

enum class FieldNames{
    // NO TOP LEVEL FIELDS
    ;
    enum class TransitionFields(val value: String){
        NextState("nextState"),
        Sinks("sinks")
    }
}
