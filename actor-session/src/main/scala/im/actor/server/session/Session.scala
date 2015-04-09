package im.actor.server.session

import scala.collection.immutable

import akka.actor._
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.stream.FlowMaterializer
import akka.stream.actor._
import akka.stream.scaladsl._
import scodec._
import scodec.bits._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._

object Session {

  import SessionMessage._

  private[this] val idExtractor: ShardRegion.IdExtractor = {
    case env@Envelope(authId, sessionId, payload) => (authId.toString + "-" + sessionId.toString, env)
  }

  private[this] val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case Envelope(authId, sessionId, _) => (authId % 32).toString // TODO: configurable
  }

  def startRegion(props: Option[Props])(implicit system: ActorSystem): ActorRef =
    ClusterSharding(system).start(
      typeName = "Session",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    )

  def startRegionProxy()(implicit system: ActorSystem): ActorRef = startRegion(None)

  def props(rpcApiService: ActorRef)(implicit materializer: FlowMaterializer) = Props(classOf[Session], rpcApiService, materializer)
}

class Session(rpcApiService: ActorRef)(implicit materializer: FlowMaterializer) extends Actor with ActorLogging with MessageIdHelper {

  import SessionMessage._

  private[this] var optUserId: Option[Int] = None
  private[this] var clients = immutable.Set.empty[ActorRef]

  def receive = receiveAnonymous

  def receiveAnonymous: Receive = {
    case env@Envelope(authId, sessionId, HandleMessageBox(messageBoxBytes)) =>
      val client = sender()

      recordClient(client)

      withValidMessageBox(client, messageBoxBytes) { mb =>
        sendProtoMessage(authId, sessionId)(client, NewSession(sessionId, mb.messageId))

        val sessionMessagePublisher = context.actorOf(SessionMessagePublisher.props())

        val graph = SessionStream.graph(rpcApiService)(context.system)

        val flow = FlowGraph.closed(graph) { implicit b =>
          g =>
            import FlowGraph.Implicits._

            val source = b.add(Source(ActorPublisher[SessionStream.SessionStreamMessage](sessionMessagePublisher)))
            val sink = b.add(Sink.foreach[ProtoMessage](m => self ! SendProtoMessage(m)))

            source ~> g.inlet
            g.outlet ~> sink
        }

        flow.run()

        sessionMessagePublisher ! Tuple2(mb, ClientData(authId, sessionId, optUserId))

        context.become(receiveResolved(authId, sessionId, sessionMessagePublisher))
      }
    case Terminated(client) =>
      clients -= client
    case unmatched => handleUnmatched(unmatched)
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
      clients -= client
    case unmatched => handleUnmatched(unmatched)
  }

  private def recordClient(client: ActorRef): Unit = {
    if (!clients.contains(client)) {
      clients += client
      context watch client
    }
  }

  private def handleSessionMessage(authId: Long,
                                   sessionId: Long,
                                   client: ActorRef,
                                   message: SessionMessage,
                                   publisher: ActorRef): Unit = {
    log.debug("Session message {}", message)
    message match {
      case HandleMessageBox(messageBoxBytes) =>
        withValidMessageBox(client, messageBoxBytes)(mb => publisher ! Tuple2(mb, ClientData(authId, sessionId, optUserId)))
      case SendProtoMessage(protoMessage) =>
        sendProtoMessage(authId, sessionId, protoMessage)
      case UserAuthorized(userId) =>
        log.debug("User {} authorized session {}", userId, sessionId)
        this.optUserId = Some(userId)
    }
  }

  private def sendProtoMessage(authId: Long, sessionId: Long, message: ProtoMessage): Unit = {
    clients foreach (_.tell(packProtoMessage(authId, sessionId, message), self))
  }

  private def withValidMessageBox(client: ActorRef, messageBoxBytes: Array[Byte])(f: MessageBox => Unit): Unit =
    decodeMessageBox(messageBoxBytes) match {
      case Some(mb) => f(mb)
      case None =>
        log.warning("Failed to parse MessageBox. Droping client.")
        client ! Drop(0, 0, "Cannot parse MessageBox")
        context.stop(self)
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

  private def handleUnmatched(message: Any) =
    log.error("Received unmatched message {}", message)
}
