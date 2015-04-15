package im.actor.server.session

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.testkit.TestProbe
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpecLike, Matchers }
import scodec.bits._

import im.actor.api.rpc.auth.{ RequestSendAuthCode, RequestSignOut, RequestSignUp, ResponseAuth, ResponseSendAuthCode }
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.sequence.{ WeakUpdate, SeqUpdate }
import im.actor.api.rpc.{ Update, RpcResult, RpcOk, Request, AuthorizedClientData }
import im.actor.server.{ persist, SqlSpecHelpers }
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.util.testing._

class SessionSpec extends ActorSuite with FlatSpecLike with ScalaFutures with Matchers with SqlSpecHelpers {
  behavior of "Session actor"

  it should "send Drop on message on wrong message box" in sessions().e1
  it should "send NewSession on first HandleMessageBox" in sessions().e2
  it should "reply to RpcRequestBox" in sessions().e3
  it should "handle user authorization" in sessions().e4
  it should "subscribe to sequence updates" in sessions().e5
  it should "subscribe to weak updates" in sessions().e6

  implicit val materializer = ActorFlowMaterializer()
  implicit val (ds, db) = migrateAndInitDb()
  implicit val ec = system.dispatcher

  val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
  val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
  val rpcApiService = system.actorOf(RpcApiService.props())
  val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion)))

  val authService = new AuthServiceImpl(sessionRegion)
  rpcApiService ! RpcApiService.AttachService(authService)

  case class sessions() {

    import SessionMessage._

    val probe = TestProbe()

    def e1() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val session = system.actorOf(Session.props(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion))

      sendEnvelope(authId, sessionId, session, HandleMessageBox(BitVector.empty.toByteArray))

      probe watch session

      probe.expectMsg(Drop(0, 0, "Cannot parse MessageBox"))
      probe.expectTerminated(session)
    }

    def e2() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, messageId, RpcRequestBox(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      probe.receiveOne(1.second)
      probe.receiveOne(1.second)
    }

    def e3() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333334L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, messageId, RpcRequestBox(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      expectMessageAck(authId, sessionId, messageId)

      expectRpcResult() should matchPattern {
        case RpcOk(ResponseSendAuthCode(_, false)) =>
      }
    }

    def e4() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, firstMessageId, RpcRequestBox(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      expectMessageAck(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult().asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCode].smsHash

      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUp(
        phoneNumber = phoneNumber,
        smsHash = smsHash,
        smsCode = "0000",
        name = "Wayne Brain",
        publicKey = Array(1, 2, 3),
        deviceHash = Array(4, 5, 6),
        deviceTitle = "Specs virtual device",
        appId = 1,
        appKey = "appKey",
        isSilent = false
      ))).require

      val secondMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion, secondMessageId, RpcRequestBox(encodedSignUpRequest))

      expectMessageAck(authId, sessionId, secondMessageId)
      expectRpcResult() should matchPattern {
        case RpcOk(ResponseAuth(_, _, _)) =>
      }

      val encodedSignOutRequest = RequestCodec.encode(Request(RequestSignOut)).require

      val thirdMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion, thirdMessageId, RpcRequestBox(encodedSignOutRequest))

      expectMessageAck(authId, sessionId, thirdMessageId)
      expectRpcResult() should matchPattern {
        case RpcOk(ResponseVoid) =>
      }
    }

    def e5() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, firstMessageId, RpcRequestBox(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      expectMessageAck(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult().asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCode].smsHash

      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUp(
        phoneNumber = phoneNumber,
        smsHash = smsHash,
        smsCode = "0000",
        name = "Wayne Brain",
        publicKey = Array(2, 2, 3),
        deviceHash = Array(5, 5, 6),
        deviceTitle = "Specs virtual device",
        appId = 1,
        appKey = "appKey",
        isSilent = false
      ))).require

      val secondMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion, secondMessageId, RpcRequestBox(encodedSignUpRequest))

      expectMessageAck(authId, sessionId, secondMessageId)

      val authResult = expectRpcResult()
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _, _)) =>
      }

      implicit val clientData = AuthorizedClientData(authId, sessionId, authResult.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id)

      val update = UpdateContactRegistered(1, true, 1L)
      Await.result(db.run(SeqUpdatesManager.broadcastClientUpdate(seqUpdManagerRegion, update)), 1.second)

      expectSeqUpdate(authId, sessionId).update should === (update.toByteArray)
    }

    def e6() = {
      val authId = createAuthId()
      val sessionId = Random.nextLong()

      val firstMessageId = Random.nextLong()
      val phoneNumber = 75550000000L

      val encodedCodeRequest = RequestCodec.encode(Request(RequestSendAuthCode(phoneNumber, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, firstMessageId, RpcRequestBox(encodedCodeRequest))

      expectNewSession(authId, sessionId, firstMessageId)
      expectMessageAck(authId, sessionId, firstMessageId)

      val smsHash = expectRpcResult().asInstanceOf[RpcOk].response.asInstanceOf[ResponseSendAuthCode].smsHash

      val encodedSignUpRequest = RequestCodec.encode(Request(RequestSignUp(
        phoneNumber = phoneNumber,
        smsHash = smsHash,
        smsCode = "0000",
        name = "Wayne Brain",
        publicKey = Array(3, 2, 3),
        deviceHash = Array(5, 5, 6),
        deviceTitle = "Specs virtual device",
        appId = 1,
        appKey = "appKey",
        isSilent = false
      ))).require

      val secondMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion, secondMessageId, RpcRequestBox(encodedSignUpRequest))

      expectMessageAck(authId, sessionId, secondMessageId)

      val authResult = expectRpcResult()
      authResult should matchPattern {
        case RpcOk(ResponseAuth(_, _, _)) =>
      }

      implicit val clientData = AuthorizedClientData(authId, sessionId, authResult.asInstanceOf[RpcOk].response.asInstanceOf[ResponseAuth].user.id)

      val update = UpdateContactRegistered(1, true, 1L)
      Await.result(db.run(WeakUpdatesManager.broadcastUserWeakUpdate(weakUpdManagerRegion, clientData.userId, update)), 1.second)

      expectWeakUpdate(authId, sessionId).update should === (update.toByteArray)
    }

    private def createAuthId(): Long = {
      val authId = Random.nextLong()
      Await.result(db.run(persist.AuthId.create(authId, None, None)), 1.second)
      authId
    }

    private def expectSeqUpdate(authId: Long, sessionId: Long): SeqUpdate = {
      UpdateBoxCodec.decode(expectMessageBox(authId, sessionId).body.asInstanceOf[UpdateBox].bodyBytes).require.value.asInstanceOf[SeqUpdate]
    }

    private def expectWeakUpdate(authId: Long, sessionId: Long): WeakUpdate = {
      UpdateBoxCodec.decode(expectMessageBox(authId, sessionId).body.asInstanceOf[UpdateBox].bodyBytes).require.value.asInstanceOf[WeakUpdate]
    }

    private def expectRpcResult(): RpcResult = {
      Option(probe.receiveOne(5.seconds)) match {
        case Some(MTPackage(authId, sessionId, mbBytes)) =>
          MessageBoxCodec.decode(mbBytes).require.value.body match {
            case RpcResponseBox(messageId, rpcResultBytes) =>
              RpcResultCodec.decode(rpcResultBytes).require.value
            case msg => throw new Exception(s"Expected RpcResponseBox but got $msg")
          }
        case Some(msg) => throw new Exception(s"Expected MTPackage but got $msg")
        case None => throw new Exception("No rpc response")
      }
    }

    private def expectMessageAck(authId: Long, sessionId: Long, messageId: Long): MessageAck = {
      val mb = expectMessageBox(authId, sessionId)
      mb.body shouldBe a[MessageAck]

      val ack = mb.body.asInstanceOf[MessageAck]
      ack.messageIds should ===(Vector(messageId))
      ack
    }

    private def expectNewSession(authId: Long, sessionId: Long, messageId: Long): NewSession = {
      val mb = expectMessageBox(authId, sessionId)
      mb.body shouldBe a[NewSession]

      val ns = mb.body.asInstanceOf[NewSession]
      ns should ===(NewSession(sessionId, messageId))

      ns
    }

    private def expectMessageBox(authId: Long, sessionId: Long): MessageBox = {
      val packageBody = probe.expectMsgPF() {
        case MTPackage(aid, sid, body) if aid == authId && sid == sessionId => body
      }

      MessageBoxCodec.decode(packageBody).require.value
    }

    private def sendMessageBox(authId: Long, sessionId: Long, session: ActorRef, messageId: Long, body: ProtoMessage) =
      sendEnvelope(authId, sessionId, session, HandleMessageBox(MessageBoxCodec.encode(MessageBox(messageId, body)).require.toByteArray))

    private def sendEnvelope(authId: Long, sessionId: Long, session: ActorRef, msg: SessionMessage) = {
      session.tell(
        Envelope(
          authId,
          sessionId,
          msg
        ),
        probe.ref
      )
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    closeDb()
  }

  private def closeDb(): Unit = {
    db.close()
    ds.close()
  }
}
