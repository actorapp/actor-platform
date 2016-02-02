package im.actor.server.session

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import im.actor.api.rpc._
import im.actor.api.rpc.auth.{ ResponseStartPhoneAuth, RequestStartPhoneAuth }
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.contacts.{ UpdateContactsAdded, UpdateContactRegistered }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.sequence.RequestGetState
import im.actor.api.rpc.weak.{ UpdateUserOffline, UpdateUserOnline }
import im.actor.concurrent.FutureExt
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
  it should "not resend messages when another came with the same reduceKey (weak)" in Sessions().reduceKeyWeak
  it should "not resend messages when another came with the same reduceKey (seq)" in Sessions().reduceKeySeq

  case class Sessions() {
    val weakUpdatesExt = WeakUpdatesExtension(system)
    val seqUpdExt = SeqUpdatesExtension(system)

    def resendNoAck() = {
      implicit val probe = TestProbe()

      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
        phoneNumber = 75553333333L,
        appId = 1,
        apiKey = "apiKey",
        deviceHash = Random.nextLong.toBinaryString.getBytes,
        deviceTitle = "Specs Has You",
        timeZone = None,
        preferredLanguages = Vector.empty
      ))).require
      sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedRequest))
      ignoreNewSession()

      expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
        case RpcOk(_: ResponseStartPhoneAuth) ⇒
      }

      // We didn't send Ack
      Thread.sleep(5000)

      expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
        case RpcOk(_: ResponseStartPhoneAuth) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
        case RpcOk(_: ResponseStartPhoneAuth) ⇒
      }

      // Still no ack
      Thread.sleep(5000)

      expectRpcResult(authId, sessionId) should matchPattern {
        case RpcOk(_: ResponseStartPhoneAuth) ⇒
      }

      probe.expectNoMsg(5.seconds)
    }

    def newClient() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      {
        implicit val probe = TestProbe()

        val encodedRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
          phoneNumber = 75553333333L,
          appId = 1,
          apiKey = "apiKey",
          deviceHash = Random.nextLong.toBinaryString.getBytes,
          deviceTitle = "Specs Has You",
          timeZone = None,
          preferredLanguages = Vector.empty
        ))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedRequest))

        expectNewSession(authId, sessionId, messageId)

        expectRpcResult(authId, sessionId, sendAckAt = None) should matchPattern {
          case RpcOk(_: ResponseStartPhoneAuth) ⇒
        }
      }

      // We didn't send Ack
      Thread.sleep(5000)

      {
        implicit val probe = TestProbe()

        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, SessionHello)

        // response to previous request
        expectRpcResult(authId, sessionId) should matchPattern {
          case RpcOk(_: ResponseStartPhoneAuth) ⇒
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
        val encodedRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
          phoneNumber = 75553333333L,
          appId = 1,
          apiKey = "apiKey",
          deviceHash = Random.nextLong.toBinaryString.getBytes,
          deviceTitle = "Specs Has You",
          timeZone = None,
          preferredLanguages = Vector.empty
        ))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedRequest))
        val mb = expectMessageBox()
        sendMessageBox(authId, sessionId, sessionRegion.ref, Random.nextLong(), MessageAck(Vector(mb.messageId)))
        probe.expectNoMsg(6.seconds)
      }

      // Ack inside Container
      {
        val messageId = Random.nextLong()
        val encodedRequest = RequestCodec.encode(Request(RequestStartPhoneAuth(
          phoneNumber = 75553333333L,
          appId = 1,
          apiKey = "apiKey",
          deviceHash = Random.nextLong.toBinaryString.getBytes,
          deviceTitle = "Specs Has You",
          timeZone = None,
          preferredLanguages = Vector.empty
        ))).require
        sendMessageBox(authId, sessionId, sessionRegion.ref, messageId, ProtoRpcRequest(encodedRequest))
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
      expectMessageAck()

      subscribeToSeq(authId, sessionId, user.id)

      val update = UpdateContactRegistered(1, false, 1L, 2L)
      seqUpdExt.deliverSingleUpdate(user.id, update)
      expectSeqUpdate(authId, sessionId, None)

      // Still no ack
      Thread.sleep(5000)

      expectSeqUpdate(authId, sessionId)

      probe.expectNoMsg(6.seconds)
    }

    def reduceKeyWeak() = {
      implicit val probe = TestProbe()

      val (_, authId, _, _) = createUser()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck()

      val upd1 = UpdateUserOffline(1, None, None)
      val upd2first = UpdateUserOnline(2, None, None)
      val upd2second = UpdateUserOffline(2, None, None)
      val upd3 = UpdateUserOffline(3, None, None)

      weakUpdatesExt.pushUpdate(authId, upd1, Some("reduceKey 1 (uniq)"), None)

      weakUpdatesExt.pushUpdate(authId, upd2first, Some("reduceKey 2 (same)"), None)

      weakUpdatesExt.pushUpdate(authId, upd2second, Some("reduceKey 2 (same)"), None)

      weakUpdatesExt.pushUpdate(authId, upd3, Some("reduceKey 3 (uniq)"), None)

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

    def reduceKeySeq() = {
      implicit val probe = TestProbe()

      val (user, authId, _, _) = createUser()
      val sessionId = Random.nextLong()

      val helloMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion.ref, helloMessageId, SessionHello)
      expectNewSession(authId, sessionId, helloMessageId)
      expectMessageAck()

      subscribeToSeq(authId, sessionId, user.id)

      val upd1 = UpdateContactsAdded(Vector(1))
      val upd2first = UpdateContactsAdded(Vector(2))
      val upd2second = UpdateContactsAdded(Vector(2))
      val upd3 = UpdateContactsAdded(Vector(3))
      val upd4 = UpdateContactsAdded(Vector(4))

      whenReady(for {
        _ ← seqUpdExt.deliverSingleUpdate(user.id, upd1, reduceKey = Some("reduceKey 1 (uniq)"))
        _ ← seqUpdExt.deliverSingleUpdate(user.id, upd2first, reduceKey = Some("reduceKey 2 (same)"))
        _ ← seqUpdExt.deliverSingleUpdate(user.id, upd2second, reduceKey = Some("reduceKey 2 (same)"))
        _ ← seqUpdExt.deliverSingleUpdate(user.id, upd3, reduceKey = Some("reduceKey 3 (uniq)"))
        _ ← seqUpdExt.deliverSingleUpdate(user.id, upd4, reduceKey = Some("reduceKey 4 (uniq)"))
      } yield ())(identity)

      expectContactsAdded(authId, sessionId, 1)

      expectContactsAdded(authId, sessionId, 2)
      expectContactsAdded(authId, sessionId, 2)

      expectContactsAdded(authId, sessionId, 3)
      expectContactsAdded(authId, sessionId, 4)

      // No ack
      probe.expectNoMsg(4.seconds)

      expectContactsAdded(authId, sessionId, 1)
      expectContactsAdded(authId, sessionId, 2)
      expectContactsAdded(authId, sessionId, 3)
      expectContactsAdded(authId, sessionId, 4)

      // Still no ack
      probe.expectNoMsg(4.seconds)

      expectContactsAdded(authId, sessionId, 1)
      expectContactsAdded(authId, sessionId, 2)
      expectContactsAdded(authId, sessionId, 3)
      expectContactsAdded(authId, sessionId, 4)
    }
  }

  private def subscribeToSeq(authId: Long, sessionId: Long, userId: Int)(implicit probe: TestProbe): Unit = {
    val encodedGetSeqRequest = RequestCodec.encode(Request(RequestGetState(Vector.empty))).require

    val getSeqMessageId = Random.nextLong()
    sendMessageBox(authId, sessionId, sessionRegion.ref, getSeqMessageId, ProtoRpcRequest(encodedGetSeqRequest))
    expectRpcResult(authId, sessionId) should matchPattern {
      case RpcOk(ResponseSeq(_, _)) ⇒
    }
  }

  private def expectContactsAdded(authId: Long, sessionId: Long, userId: Int)(implicit probe: TestProbe): Unit = {
    val seq = expectSeqUpdate(authId, sessionId, sendAckAt = None)
    seq.updateHeader shouldBe UpdateContactsAdded.header
    UpdateContactsAdded.parseFrom(seq.update) shouldBe Right(UpdateContactsAdded(Vector(userId)))
  }

  private def expectUserOnline(authId: Long, sessionId: Long, userId: Int)(implicit probe: TestProbe): Unit = {
    val weak = expectWeakUpdate(authId, sessionId)
    weak.updateHeader should ===(UpdateUserOnline.header)
    UpdateUserOnline.parseFrom(weak.update) shouldBe Right(UpdateUserOnline(userId, None, None))
  }

  private def expectUserOffline(authId: Long, sessionId: Long, userId: Int)(implicit probe: TestProbe): Unit = {
    val weak = expectWeakUpdate(authId, sessionId)
    weak.updateHeader should ===(UpdateUserOffline.header)
    UpdateUserOffline.parseFrom(weak.update) shouldBe Right(UpdateUserOffline(userId, None, None))
  }
}