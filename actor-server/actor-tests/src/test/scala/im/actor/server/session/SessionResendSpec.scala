package im.actor.server.session

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ RequestSendAuthCodeObsolete, ResponseSendAuthCodeObsolete }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.weak.{ UpdateUserOnline, UpdateUserOffline }
import im.actor.server.ActorSpecification
import im.actor.server.mtproto.protocol._
import im.actor.server.sequence.{ SeqUpdatesManager, WeakUpdatesManager }

import scala.concurrent.duration._
import scala.util.Random

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

  it should "resend messages if no ack received within ack-timeout" in Sessions().e1
  it should "resend messages to new client" in Sessions().e2
  it should "not resend messages if ack received within ack-timeout" in Sessions().e3
  it should "resend updates if no ack received within ack-timeout" in Sessions().e4
  it should "not resend messages when another came with the same reduceKey" in Sessions().reduceKey

  case class Sessions() {
    def e1() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

      ignoreNewSession(authId, sessionId)
      expectMessageAck(authId, sessionId, messageId)

      expectRpcResult(sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      // We didn't send Ack
      Thread.sleep(5000)

      expectRpcResult(sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult(sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult() should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      probe.expectNoMsg(5.seconds)
    }

    def e2() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      {
        implicit val probe = TestProbe()

        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

        expectNewSession(authId, sessionId, messageId)

        expectMessageAck(authId, sessionId, messageId)

        expectRpcResult(sendAckAt = None) should matchPattern {
          case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
        }
      }

      // We didn't send Ack
      Thread.sleep(5000)

      {
        implicit val probe = TestProbe()

        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, SessionHello)

        // response to previous request
        expectRpcResult() should matchPattern {
          case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
        }

        expectMessageAck(authId, sessionId, messageId)

        probe.expectNoMsg(6.seconds)
      }
    }

    def e3() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      ignoreNewSession(authId, sessionId)

      // Single ack
      {
        val messageId = Random.nextLong()
        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))
        expectMessageAck(authId, sessionId, messageId)
        val mb = expectMessageBox(authId, sessionId)
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong(), MessageAck(Vector(mb.messageId)))
        probe.expectNoMsg(6.seconds)
      }

      // Ack inside Container
      {
        val messageId = Random.nextLong()
        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))
        expectMessageAck(authId, sessionId, messageId)
        val mb = expectMessageBox(authId, sessionId)

        val containerMessageId = Random.nextLong()
        sendMessageBox(authId, sessionId, sessionRegion.ref, containerMessageId, Container(Seq(MessageBox(Random.nextLong, MessageAck(Vector(mb.messageId))))))
        expectMessageAck(authId, sessionId, containerMessageId)
        probe.expectNoMsg(6.seconds)
      }
    }

    def e4() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck(authId, sessionId, helloMessageId)

      val update = UpdateContactRegistered(1, false, 1L, 2L)
      SeqUpdatesManager.persistAndPushUpdate(authId, update, None, isFat = false)
      expectSeqUpdate(authId, sessionId, None)

      // Still no ack
      Thread.sleep(5000)

      expectSeqUpdate(authId, sessionId)

      probe.expectNoMsg(6.seconds)
    }

    def reduceKey() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck(authId, sessionId, helloMessageId)

      val upd1 = UpdateUserOffline(1)
      val upd2first = UpdateUserOnline(2)
      val upd2second = UpdateUserOffline(2)
      val upd3 = UpdateUserOffline(3)

      WeakUpdatesManager.pushUpdate(authId, upd1, Some("reduceKey 1 (uniq)"))

      WeakUpdatesManager.pushUpdate(authId, upd2first, Some("reduceKey 2 (same)"))
      WeakUpdatesManager.pushUpdate(authId, upd2second, Some("reduceKey 2 (same)"))

      WeakUpdatesManager.pushUpdate(authId, upd3, Some("reduceKey 3 (uniq)"))

      expectUserOffline(authId, sessionId, 1)

      expectUserOnline(authId, sessionId, 2)
      expectUserOffline(authId, sessionId, 2)

      expectUserOffline(authId, sessionId, 3)

      // No ack
      probe.expectNoMsg(4.seconds)

      expectUserOffline(authId, sessionId, 1)
      expectUserOffline(authId, sessionId, 2)
      expectUserOffline(authId, sessionId, 3)

      // Still no ack
      probe.expectNoMsg(4.seconds)

      expectUserOffline(authId, sessionId, 1)
      expectUserOffline(authId, sessionId, 2)
      expectUserOffline(authId, sessionId, 3)
    }
  }

  private def expectUserOnline(authId: Long, sessionId: Long, userId: Int)(implicit probe: TestProbe): Unit = {
    val weak = expectWeakUpdate(authId, sessionId)
    weak.updateHeader should ===(UpdateUserOnline.header)
    UpdateUserOnline.parseFrom(weak.update) shouldBe Right(UpdateUserOnline(userId))
  }

  private def expectUserOffline(authId: Long, sessionId: Long, userId: Int)(implicit probe: TestProbe): Unit = {
    val weak = expectWeakUpdate(authId, sessionId)
    weak.updateHeader should ===(UpdateUserOffline.header)
    UpdateUserOffline.parseFrom(weak.update) shouldBe Right(UpdateUserOffline(userId))
  }
}