package im.actor.server.session

import scala.collection.immutable
import scala.concurrent.ExecutionContext

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.pipe
import akka.stream.FlowMaterializer
import akka.stream.actor._
import akka.stream.scaladsl._
import scodec.DecodeResult
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ Drop, MTPackage }
import im.actor.server.presences.PresenceManagerRegion
import im.actor.server.push.{ WeakUpdatesManagerRegion, SeqUpdatesManagerRegion, UpdatesPusher }
import im.actor.server.{ models, persist }

object Session {

  import SessionMessage._

  private[this] val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, sessionId, payload) => (authId.toString + "-" + sessionId.toString, env)
  }

  private[this] val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case Envelope(authId, sessionId, _) => (authId % 32).toString // TODO: configurable
  }

  def startRegion(props: Option[Props])(implicit system: ActorSystem): SessionRegion =
    SessionRegion(
      ClusterSharding(system).start(
        typeName = "Session",
        entryProps = props,
        idExtractor = idExtractor,
        shardResolver = shardResolver
      ))

  def startRegionProxy()(implicit system: ActorSystem): SessionRegion = startRegion(None)

  def props(rpcApiService: ActorRef,
            seqUpdManagerRegion: SeqUpdatesManagerRegion,
            weakUpdManagerRegion: WeakUpdatesManagerRegion,
            presenceManagerRegion: PresenceManagerRegion)
           (implicit db: Database, materializer: FlowMaterializer): Props =
    Props(
      classOf[Session],
      rpcApiService,
      seqUpdManagerRegion,
      weakUpdManagerRegion,
      presenceManagerRegion,
      db,
      materializer)
}

class Session(rpcApiService: ActorRef,
              seqUpdManagerRegion: SeqUpdatesManagerRegion,
              weakUpdManagerRegion: WeakUpdatesManagerRegion,
              presenceManagerRegion: PresenceManagerRegion)
             (implicit db: Database, materializer: FlowMaterializer)
  extends Actor with ActorLogging with MessageIdHelper with Stash {

  import SessionMessage._

  implicit val ec: ExecutionContext = context.dispatcher

  private[this] var optUserId: Option[Int] = None
  private[this] var clients = immutable.Set.empty[ActorRef]

  def receive = waitingForEnvelope

  def waitingForEnvelope: Receive = {
    case env @ Envelope(authId, sessionId, _) =>
      stash()
      context.become(waitingForSessionInfo)

      // TODO: handle errors
      // TODO: refactor
      val infoAction = {
        for {
          authIdModelOpt <- persist.AuthId.find(authId).headOption
          infoModel <- persist.SessionInfo.find(authId, sessionId).headOption.map(_.getOrElse(models.SessionInfo(authId, sessionId, None)))
        } yield {
          authIdModelOpt match {
            case Some(models.AuthId(_, Some(userId), _)) =>
              persist.SessionInfo.updateUserId(authId, sessionId, Some(userId))
              models.SessionInfo(authId, sessionId, Some(userId))
            case Some(models.AuthId(_, None, _)) =>
              infoModel
            case None =>
              infoModel
          }
        }
      }

      val infoFuture = db.run(infoAction)

      infoFuture.pipeTo(self)
    case msg => stash()
  }

  def waitingForSessionInfo: Receive = {
    case info: models.SessionInfo =>
      optUserId = info.optUserId
      unstashAll()
      context.become(anonymous)
    case msg => stash()
  }

  def anonymous: Receive = {
    case env @ Envelope(authId, sessionId, HandleMessageBox(messageBoxBytes)) =>
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

        context.actorOf(UpdatesPusher.props(seqUpdManagerRegion, weakUpdManagerRegion, presenceManagerRegion, authId, self))

        context.become(resolved(authId, sessionId, sessionMessagePublisher))
      }
    case Terminated(client) =>
      clients -= client
    case unmatched => handleUnmatched(unmatched)
  }

  def resolved(authId: Long, sessionId: Long, publisher: ActorRef): Receive = {
    case env @ Envelope(eauthId, esessionId, msg) =>
      val client = sender()

      recordClient(client)

      if (authId != eauthId || sessionId != esessionId) // Should never happen
        log.error("Received Envelope with another's authId and sessionId {}", env)
      else
        handleSessionMessage(authId, sessionId, client, msg, publisher)
    case SendProtoMessage(protoMessage) =>
      log.debug("Sending proto message {}", protoMessage)
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
      case SubscribeToOnline(userIds) =>

      case SubscribeFromOnline(userIds) =>
      // TODO: implement
      case SubscribeToGroupOnline(groupIds) =>
      // TODO: implement
      case SubscribeFromGroupOnline(groupIds) =>
      // TODO: implement
      case UserAuthorized(userId) =>
        log.debug("User {} authorized session {}", userId, sessionId)

        this.optUserId = Some(userId)

        // TODO: handle errors
        persist.SessionInfo.updateUserId(authId, sessionId, this.optUserId)
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
