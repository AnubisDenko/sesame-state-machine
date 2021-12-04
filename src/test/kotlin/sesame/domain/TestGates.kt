package sesame.domain

class AlwaysBlockGate(override val name: String): Gate<Any> {
    override fun accept(event: Event, stateObject: Any): GateResponse {
        return GateResponse(false)
    }
}

class AlwaysPassGate(override val name: String) : Gate<Any> {
    override fun accept(event: Event, stateObject: Any): GateResponse {
        return GateResponse(true)
    }
}

class FlexibleGate(override val name: String): Gate<Any>{
    override fun accept(event: Event, stateObject: Any): GateResponse {
        return if(FlexibleGateResponse.error == null){
            GateResponse(FlexibleGateResponse.succeed)
        }else{
            GateResponse(FlexibleGateResponse.succeed, listOf(FlexibleGateResponse.error!!))
        }

    }
}

object FlexibleGateResponse{
    var succeed = true
    var error: String? = null

    fun setupNextResponse(succeed: Boolean, error: String? = null): FlexibleGateResponse{
        this.succeed = succeed
        this.error = error
        return this
    }

    fun reset(){
        succeed = true
        error = null
    }
}
