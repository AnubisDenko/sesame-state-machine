package domain

enum class FieldNames{
    // NO TOP LEVEL FIELDS
    ;
    enum class TransitionFields(val value: String){
        NextState("nextState"),
        Sinks("sinks")
        ;

        enum class SinkFields(val value: String){
            Class("class"),
            Name("name")
        }
    }
}
