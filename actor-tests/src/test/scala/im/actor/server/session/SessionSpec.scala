package im.actor.server.session

import scala.concurrent.duration._
import scala.util.Random

import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.testkit.{TestKit, TestProbe}
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.core.Fragments
import scodec.bits._

import im.actor.api.rpc.auth.{ RequestSendAuthCode, RequestSignOut, RequestSignUp, ResponseAuth, ResponseSendAuthCode }
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.{ Request, RpcOk, RpcResult }
import im.actor.server.SqlSpecHelpers
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.{ RpcApiService, RpcResultCodec }
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.util.testing._

class SessionSpec extends ActorSpecification with SqlSpecHelpers with ThrownExpectations {
  def is = sequential ^ s2"""
  Session Actor should
    send Drop on message on wrong message box ${sessions().e1}
    send NewSession on first HandleMessageBox ${sessions().e2}
    reply to RpcRequestBox ${sessions().e3}
    handle user authorization ${sessions().e4}
  """

  implicit val materializer = ActorFlowMaterializer()
  implicit val (ds, db) = migrateAndInitDb()

  val rpcApiService = system.actorOf(RpcApiService.props())
  val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService)))

  val authService = new AuthServiceImpl(sessionRegion)
  rpcApiService ! RpcApiService.AttachService(authService)

  case class sessions() {

    import SessionMessage._

    val authId = Random.nextLong()

    val probe = TestProbe()

    def e1() = {
      val sessionId = Random.nextLong()
      val session = system.actorOf(Session.props(rpcApiService))

      sendEnvelope(authId, sessionId, session, HandleMessageBox(BitVector.empty.toByteArray))

      probe watch session

      probe.expectMsg(Drop(0, 0, "Cannot parse MessageBox"))
      probe.expectTerminated(session)
    }

    def e2() = {
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, messageId, RpcRequestBox(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      probe.receiveOne(1.second)
      probe.receiveOne(1.second)
    }

    def e3() = {
      val sessionId = Random.nextLong()
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, sessionRegion, messageId, RpcRequestBox(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      expectMessageAck(authId, sessionId, messageId)

      expectRpcResult() must beLike {
        case RpcOk(ResponseSendAuthCode(_, false)) => ok
      }
    }

    def e4() = {
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
      expectRpcResult() must beLike {
        case RpcOk(ResponseAuth(_, _, _)) => ok
      }

      val encodedSignOutRequest = RequestCodec.encode(Request(RequestSignOut)).require

      val thirdMessageId = Random.nextLong()
      sendMessageBox(authId, sessionId, sessionRegion, thirdMessageId, RpcRequestBox(encodedSignOutRequest))

      expectMessageAck(authId, sessionId, thirdMessageId)
      expectRpcResult() must beLike {
        case RpcOk(ResponseVoid) => ok
      }
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
      mb.body must beAnInstanceOf[MessageAck]

      val ack = mb.body.asInstanceOf[MessageAck]
      ack.messageIds must be_==(Vector(messageId))
      ack
    }

    private def expectNewSession(authId: Long, sessionId: Long, messageId: Long): NewSession = {
      val mb = expectMessageBox(authId, sessionId)
      mb.body must beAnInstanceOf[NewSession]

      val ns = mb.body.asInstanceOf[NewSession]
      ns must be_==(NewSession(sessionId, messageId))

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

  override def map(fragments: => Fragments) =
    super.map(fragments) ^ step(closeDb())

  private def closeDb(): Unit = {
    TestKit.shutdownActorSystem(actorSystem = system, verifySystemShutdown = true)
    db.close()
    ds.close()
  }
}
