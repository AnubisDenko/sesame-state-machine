package sesame.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import sesame.domain.TestEvent
import sesame.domain.TestStates.*
import sesame.state.exceptions.IncorrectConfigException

class StpStateMachineTest {

    @BeforeEach
    fun before(){
        StateMachineFactory.clearAllStateMachines()
    }

    @Test
    fun `automatically transitions along the stp path until no more stp transitions are configured`(){
        val stateMachine = StateMachineFactory.createStateMachine<Any>(basicStpConfig)
        val event = TestEvent("orderPlaced")
        val result = stateMachine.processEvent(event, NEW.state, Any())

        assertEquals(ACCEPTED.state, result.state)
    }

    @Test
    fun `doesn't allow more than one stp path per state`(){
        val config = """
                {
                  "NEW": {
                    "orderPlaced": {
                      "nextState": "FINAL",
                      "stp": true
                    },
                    "alternativeStp" :{
                      "nextState": "FINAL",
                      "stp": true
                    }
                  },      
                  "FINAL": {}
                }
            """
        assertThrows<IncorrectConfigException> { StateMachineFactory.createStateMachine<Any>(config) }
    }

    @Test
    fun `auto transition will also be stopped in case a gate is triggered`(){
        val stateMachine = StateMachineFactory.createStateMachine<Any>(basicStpConfigWithBlockingGate)
        val event = TestEvent("orderPlaced")
        assertEquals( ORDER_RECEIVED.state, stateMachine.processEvent(event, NEW.state, Any()).state)
    }
}

private val basicStpConfig = """
    {
      "NEW": {
        "orderPlaced": {
          "nextState": "ORDER_RECEIVED"
        }
      },
      "ORDER_RECEIVED": {
        "accept": {
          "nextState": "ACCEPTED",
          "stp": true
        },
        "reject": {
          "nextState": "REJECTED"
        }
      },
      "ACCEPTED": {},
      "REJECTED": {}
    }
""".trimIndent()

private val basicStpConfigWithBlockingGate = """
    {
      "NEW": {
        "orderPlaced": {
          "nextState": "ORDER_RECEIVED"
        }
      },
      "ORDER_RECEIVED": {
        "accept": {
          "nextState": "ACCEPTED",
          "stp": true,
          "gates": [{
            "class": "sesame.domain.AlwaysBlockGate",
            "name": "AlwaysBlockGate"
          }]
        },
        "reject": {
          "nextState": "REJECTED"
        }
      },
      "ACCEPTED": {},
      "REJECTED": {}
    }
""".trimIndent()