package sesame.domain

data class GateResponse( val result: Boolean, val errorMessage: List<String> = emptyList()){
    operator fun plus(other: GateResponse ): GateResponse {
        return GateResponse(
            this.result && other.result,
            this.errorMessage + other.errorMessage
        )
    }
}