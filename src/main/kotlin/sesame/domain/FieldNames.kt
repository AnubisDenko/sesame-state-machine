package sesame.domain

enum class FieldNames{
    // NO TOP LEVEL FIELDS
    ;
    enum class TransitionFields(val value: String){
        NextState("nextState"),
        Sinks("sinks"),
        Gates("gates")
        ;

        enum class SinkFields(val value: String){
            Class("class"),
            Name("name")
        }

        enum class GateFields(val value: String){
            Class("class"),
            Name("name")
        }
    }
}
