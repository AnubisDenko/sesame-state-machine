package sesame.domain

data class GateResponse( val result: Boolean, val errorMessage: List<String> = emptyList()){
    constructor(result: Boolean, errorMessage: String) : this(result, listOf(errorMessage))

    operator fun plus(other: GateResponse ): GateResponse {
        return GateResponse(
            this.result && other.result,
            this.errorMessage + other.errorMessage
        )
    }
}