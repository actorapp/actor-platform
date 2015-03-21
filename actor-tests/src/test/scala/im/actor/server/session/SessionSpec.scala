package im.actor.server.session

import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.testkit.TestProbe
import org.specs2.Specification

import im.actor.api.rpc.{ RpcResult, RpcOk, Request }
import im.actor.api.rpc.auth.{ ResponseSendAuthCode, RequestSendAuthCode }
import im.actor.api.rpc.codecs._
import im.actor.server.api.rpc.{ RpcResultCodec, RpcOkCodec }
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.util.testing._

import scala.concurrent.duration._
import scala.util.Random

import scodec.bits._

import im.actor.server.SqlSpecHelpers

class SessionSpec extends ActorSpecification with SqlSpecHelpers {
  def is = sequential ^ s2"""
  Session Actor should
    send Drop on message on wrong message box ${sessions().e1}
    send NewSession on first HandleMessageBox ${sessions().e2}
    reply to RpcRequestBox ${sessions().e3}
  """

  case class sessions() {
    import Session._
    implicit val materializer = ActorFlowMaterializer()
    implicit val db = migrateAndInitDb()

    val authId = Random.nextLong()
    val sessionId = Random.nextLong()
    val session = system.actorOf(Session.props(), s"Session-$authId-$sessionId")
    val probe = TestProbe()

    def e1() = {
      sendEnvelope(authId, sessionId, session, HandleMessageBox(BitVector.empty.toByteArray))

      probe watch session

      probe.expectMsg(Drop(0, 0, "Cannot parse message box"))
      probe.expectTerminated(session)
    }

    def e2() = {
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, session, messageId, RpcRequestBox(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      probe.receiveOne(1.second)
    }

    def e3() = {
      val messageId = Random.nextLong()

      val encodedRequest = RequestCodec.encode(Request(RequestSendAuthCode(75553333333L, 1, "apiKey"))).require
      sendMessageBox(authId, sessionId, session, messageId, RpcRequestBox(encodedRequest))

      expectNewSession(authId, sessionId, messageId)
      receiveRpcResult() must beLike {
        case RpcOk(ResponseSendAuthCode(_, false)) => ok
      }
    }

    private def receiveRpcResult(): RpcResult = {
      Option(probe.receiveOne(1.second)) match {
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

    private def expectNewSession(authId: Long, sessionId: Long, messageId: Long) =
      expectMessageBox(authId, sessionId, 1L, NewSession(sessionId, messageId))

    private def expectMessageBox(authId: Long, sessionId: Long, messageId: Long, body: ProtoMessage) = {
      probe.expectMsg(MTPackage(
        authId,
        sessionId,
        MessageBoxCodec.encode(MessageBox(messageId, body)).require
      ))
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
}
