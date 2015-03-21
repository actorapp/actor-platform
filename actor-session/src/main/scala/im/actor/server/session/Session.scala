package im.actor.server.session

import scala.collection.mutable

import akka.actor._
import akka.stream.FlowMaterializer
import akka.stream.actor._
import akka.stream.scaladsl._
import scodec._
import scodec.bits._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.ClientData
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._

object Session {

  sealed trait SessionMessage

  @SerialVersionUID(1L)
  case class Envelope(authId: Long, sessionId: Long, msg: SessionMessage)

  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBoxBytes: Array[Byte]) extends SessionMessage

  @SerialVersionUID(1L)
  case class SendProtoMessage(message: ProtoMessage) extends SessionMessage

  def props()(implicit materializer: FlowMaterializer, database: Database) = Props(classOf[Session], materializer, database)
}

class Session(implicit materializer: FlowMaterializer, db: Database) extends Actor with ActorLogging with MessageIdHelper {

  import Session._

  var newSessionSent: Boolean = false
  var optUserId: Option[Int] = None
  val clients = mutable.Set.empty[ActorRef]

  def receive = receiveAnonymous

  def receiveAnonymous: Receive = {
    case env@Envelope(authId, sessionId, HandleMessageBox(messageBoxBytes)) =>
      val client = sender()

      recordClient(client)

      withValidMessageBox(client, messageBoxBytes) { mb =>
        sendProtoMessage(authId, sessionId)(client, NewSession(sessionId, mb.messageId))

        val sessionMessagePublisher = context.actorOf(SessionMessagePublisher.props())
        val rpcResponsePublisher = context.actorOf(RpcResponseManager.props())
        val rpcApiService = context.actorOf(RpcApiService.props()) // TODO: make it adaptive router

        val source = Source(ActorPublisher[SessionStream.SessionStreamMessage](sessionMessagePublisher))
        val graph = SessionStream.graph(
          source = source, rpcApiService = rpcApiService, rpcResponsePublisher = rpcResponsePublisher
        )(context.system)

        graph.run()

        val rpcResponseSource = Source(ActorPublisher[ProtoMessage](rpcResponsePublisher))
        val flow = rpcResponseSource.to(Sink.foreach[ProtoMessage](m => self ! SendProtoMessage(m)))
        flow.run()

        sessionMessagePublisher ! Tuple2(mb, ClientData(authId, optUserId))

        context become receiveResolved(authId, sessionId, sessionMessagePublisher)
      }
    case Terminated(client) =>
      clients.remove(client)
    case unmatched =>
      log.error("Received unmatched message {}", unmatched)
  }

  def receiveResolved(authId: Long, sessionId: Long, publisher: ActorRef): Receive = {
    case env@Envelope(eauthId, esessionId, msg) =>
      val client = sender()

      recordClient(client)

      if (authId != eauthId || sessionId != esessionId) // Should never happen
        log.error("Received Envelope with another's authId and sessionId {}", env)
      else
        handleSessionMessage(authId, sessionId, client, msg, publisher)
    case SendProtoMessage(protoMessage) =>
      sendProtoMessage(authId, sessionId, protoMessage)
    case Terminated(client) =>
      clients.remove(client)
  }

  private def recordClient(client: ActorRef): Unit = {
    if (clients.add(client) == true)
      context watch client
  }

  private def handleSessionMessage(
                                    authId: Long,
                                    sessionId: Long,
                                    client: ActorRef, msg: SessionMessage, publisher: ActorRef
                                    ): Unit = msg match {
    case HandleMessageBox(messageBoxBytes) =>
      withValidMessageBox(client, messageBoxBytes)(publisher.tell(_, self))
    case SendProtoMessage(protoMessage) =>
      sendProtoMessage(authId, sessionId, protoMessage)
  }

  private def sendProtoMessage(authId: Long, sessionId: Long, message: ProtoMessage): Unit = {
    clients foreach (_.tell(packProtoMessage(authId, sessionId, message), self))
  }

  private def withValidMessageBox(client: ActorRef, messageBoxBytes: Array[Byte])(f: MessageBox => Unit): Unit =
    decodeMessageBox(messageBoxBytes) match {
      case Some(mb) => f(mb)
      case None =>
        client ! Drop(0, 0, "Cannot parse message box")
        context stop self
    }

  private def decodeMessageBox(messageBoxBytes: Array[Byte]): Option[MessageBox] =
    MessageBoxCodec.decode(BitVector(messageBoxBytes)).toEither match {
      case Right(DecodeResult(mb, _)) => Some(mb)
      case _ => None
    }

  private def boxProtoMessage(message: ProtoMessage): MessageBox = {
    MessageBox(nextMessageId(), message)
  }

  private def packProtoMessage(authId: Long, sessionId: Long, message: ProtoMessage): MTPackage = {
    val bytes = MessageBoxCodec.encode(boxProtoMessage(message)).require
    MTPackage(authId, sessionId, bytes)
  }

  private def sendProtoMessage(authId: Long, sessionId: Long)(client: ActorRef, message: ProtoMessage): Unit = {
    client ! packProtoMessage(authId, sessionId, message)
  }
}
