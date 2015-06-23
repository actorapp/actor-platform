package im.actor.server.llectro

import java.util.UUID

import org.scalatest.Inside._

import im.actor.server.BaseAppSuite
import im.actor.server.llectro.results.UserBalance

class LlectroSpec extends BaseAppSuite {

  behavior of "Llectro"

  it should "retreive user's balance" in s.e1

  object s {
    val llectro = new Llectro

    def e1() = {
      val userUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
      whenReady(llectro.getUserBalance(userUuid)) { ub ⇒
        inside(ub) {
          case UserBalance(name, balance) ⇒
            name should not be empty
            (balance > 0) shouldBe true
        }
      }
    }
  }

}
