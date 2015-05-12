package im.actor.server.session

import scala.concurrent.duration._
import scala.util.Random

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory

import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ RequestSendAuthCode, ResponseSendAuthCode }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.server.mtproto.protocol.{ SessionHello, RpcRequestBox }
import im.actor.server.push.SeqUpdatesManager
import im.actor.util.testing.ActorSpecification

class SessionResendSpec extends BaseSessionSpec(
  ActorSpecification.createSystem(ConfigFactory.parseString(
    """
      |session {
      |  resend {
      |    ack-timeout = 5 seconds
      |  }
      |}
    """.stripMargin
  ))
) {
  behavior of "Session's ReSender"

  it should "Resend messages if ack received within ack-timeout" in Sessions().e1
  it should "not Resend messages if ack received within ack-timeout" in Sessions().e3
  it should "Resend messages to new client" in Sessions().e2

  case class Sessions() {
    def e1() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

      ignoreNewSession(authId, sessionId)
      expectMessageAck(authId, sessionId, messageId)

      expectRpcResult(sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCode(_, _)) ⇒
      }

      // We didn't send Ack
      Thread.sleep(5000)

      expectRpcResult(sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCode(_, _)) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult() should matchPattern {
        case RpcOk(ResponseSendAuthCode(_, _)) ⇒
      }

      expectNoMsg(6.seconds)
    }

    def e2() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      {
        implicit val probe = TestProbe()

        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

        expectNewSession(authId, sessionId, messageId)

        expectMessageAck(authId, sessionId, messageId)

        expectRpcResult(sendAckAt = None) should matchPattern {
          case RpcOk(ResponseSendAuthCode(_, _)) ⇒
        }
      }

      // We didn't send Ack
      Thread.sleep(5000)

      {
        implicit val probe = TestProbe()

        val helloMessageId = Random.nextLong()
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, SessionHello(sessionId, helloMessageId))

        // response to previous request
        expectRpcResult() should matchPattern {
          case RpcOk(ResponseSendAuthCode(_, _)) ⇒
        }

        expectMessageAck(authId, sessionId, messageId)

        expectNoMsg(6.seconds)
      }
    }

    def e3() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      implicit val probe = TestProbe()
      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

      ignoreNewSession(authId, sessionId)
      expectMessageAck(authId, sessionId, messageId)

      expectRpcResult(sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCode(_, _)) ⇒
      }

      // We didn't send Ack within ack-timeout
      expectNoMsg(6.seconds)

      // But we've sent Response Ack with delay in 6 seconds, so we don't expect Response resending
      expectNoMsg(5.seconds)
    }
  }
}