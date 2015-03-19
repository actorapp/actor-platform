package im.actor.server.session

import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.testkit.TestProbe

import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.util.testing._

import scala.util.Random

import scodec.bits._

class SessionSpec extends ActorSpecification {
  def is = sequential ^ s2"""
  Session Actor should
    send Drop on message on wrong message box ${sessions().e1}
    send NewSession on first HandleMessageBox ${sessions().e2}
    reply to RpcRequestBox ${sessions().e3}
  """

  case class sessions() {
    import Session._
    implicit val materializer = ActorFlowMaterializer()

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

      sendMessageBox(authId, sessionId, session, messageId, RpcRequestBox(BitVector.empty))

      expectNewSession(authId, sessionId, messageId)
    }

    def e3() = {
      val messageId = Random.nextLong()

      sendMessageBox(authId, sessionId, session, messageId, RpcRequestBox(BitVector.empty))

      expectNewSession(authId, sessionId, messageId)
      expectMessageBox(authId, sessionId, 2L, RpcResponseBox(messageId, BitVector.empty))

      probe.expectNoMsg()
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
