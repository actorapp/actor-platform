package im.actor.server.session

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ RequestStartPhoneAuth, ResponseStartPhoneAuth }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.server.ActorSpecification
import im.actor.server.mtproto.protocol._

import scala.concurrent.duration._
import scala.util.Random

final class SessionResendOverflowSpec extends BaseSessionSpec(
  ActorSpecification.createSystem(ConfigFactory.parseString(
    """
      |session {
      |  resend {
      |    max-buffer-size = 100
      |  }
      |}
    """.stripMargin
  ))
) {
  behavior of "Session's ReSender with max-buffer-size set to 100 KiB"

  it should "kill session on resend buffer overflow" in Sessions().e1

  case class Sessions() {
    def e1() = {
      val watchProbe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val session = system.actorOf(Session.props, s"${authId}_$sessionId")
      watchProbe watch session

      val encodedRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = 75553333333L,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Spec Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require

      for (_ ← 1 to 10)
        TestProbe().send(session, handleMessageBox(Random.nextLong(), ProtoRpcRequest(encodedRequest)))

      watchProbe.expectTerminated(session)
    }
  }

}