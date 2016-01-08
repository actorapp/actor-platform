package im.actor.server.session

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ RequestSendAuthCodeObsolete, ResponseSendAuthCodeObsolete }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.sequence.RequestGetState
import im.actor.api.rpc.weak.{ UpdateUserOffline, UpdateUserOnline }
import im.actor.server.ActorSpecification
import im.actor.server.mtproto.protocol._
import im.actor.server.sequence.{ SeqUpdatesExtension, WeakUpdatesExtension }

import scala.concurrent.duration._
import scala.util.Random

final class SessionResendSpec extends BaseSessionSpec(
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

  it should "resend messages if no ack received within ack-timeout" in Sessions().resendNoAck
  it should "resend messages to new client" in Sessions().newClient
  it should "not resend messages if ack received within ack-timeout" in Sessions().noResendOnAck
  it should "resend updates if no ack received within ack-timeout" in Sessions().resendUpdates
  it should "not resend messages when another came with the same reduceKey" in Sessions().reduceKey

  case class Sessions() {
    val weakUpdatesExt = WeakUpdatesExtension(system)
    val seqUpdExt = SeqUpdatesExtension(system)

    def resendNoAck() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

      ignoreNewSession()
      expectMessageAck(messageId)

      expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      // We didn't send Ack
      Thread.sleep(5000)

      expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
      }

      probe.expectNoMsg(5.seconds)
    }

    def newClient() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      {
        implicit val probe = TestProbe()

        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))

        expectNewSession(authId, sessionId, messageId)

        expectMessageAck(messageId)

        expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
          case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
        }
      }

      // We didn't send Ack
      Thread.sleep(5000)

      {
        implicit val probe = TestProbe()

        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, SessionHello)

        // response to previous request
        expectRpcResult(authId, sessionId) should matchPattern {
          case RpcOk(ResponseSendAuthCodeObsolete(_, _)) ⇒
        }

        expectMessageAck(messageId)

        probe.expectNoMsg(6.seconds)
      }
    }

    def noResendOnAck() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()

      ignoreNewSession()

      // Single ack
      {
        val messageId = Random.nextLong()
        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))
        expectMessageAck(messageId)
        val mb = expectMessageBox()
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong(), MessageAck(Vector(mb.messageId)))
        probe.expectNoMsg(6.seconds)
      }

      // Ack inside Container
      {
        val messageId = Random.nextLong()
        val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCodeObsolete(75553333333L, 1, "apiKey"))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, RpcRequestBox(encodedRequest))
        expectMessageAck(messageId)
        val mb = expectMessageBox()

        val containerMessageId = Random.nextLong()
        sendMessageBox(authId, sessionId, sessionRegion.ref, containerMessageId, Container(Seq(MessageBox(Random.nextLong, MessageAck(Vector(mb.messageId))))))
        expectMessageAck(containerMessageId)
        probe.expectNoMsg(6.seconds)
      }
    }

    def resendUpdates() = {
      implicit val probe = TestProbe()

      val (user, authId, _, _) = createUser()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck(helloMessageId)

      val encodedGetSeqRequest = RequestCodec.encode(Request(RequestGetState)).require

      val getSeqMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, getSeqMessageId, RpcRequestBox(encodedGetSeqRequest))
      expectMessageAck(getSeqMessageId)
      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(ResponseSeq(_, _)) ⇒
      }

      val update = UpdateContactRegistered(1, false, 1L, 2L)
      seqUpdExt.deliverSingleUpdate(user.id, update)
      expectSeqUpdate(authId, sessionId, None)

      // Still no ack
      Thread.sleep(5000)

      expectSeqUpdate(authId, sessionId)

      probe.expectNoMsg(6.seconds)
    }

    def reduceKey() = {
      implicit val probe = TestProbe()

      val (_, authId, _, _) = createUser()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck(helloMessageId)

      val upd1 = UpdateUserOffline(1)
      val upd2first = UpdateUserOnline(2)
      val upd2second = UpdateUserOffline(2)
      val upd3 = UpdateUserOffline(3)

      weakUpdatesExt.pushUpdate(authId, upd1, Some("reduceKey 1 (uniq)"))

      weakUpdatesExt.pushUpdate(authId, upd2first, Some("reduceKey 2 (same)"))

      weakUpdatesExt.pushUpdate(authId, upd2second, Some("reduceKey 2 (same)"))

      weakUpdatesExt.pushUpdate(authId, upd3, Some("reduceKey 3 (uniq)"))

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