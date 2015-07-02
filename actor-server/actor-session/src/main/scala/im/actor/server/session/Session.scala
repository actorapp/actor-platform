package im.actor.server.session

import java.util.concurrent.TimeUnit

import im.actor.server.api.rpc.service.auth.{ AuthEvents, AuthService }

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.pattern.pipe
import akka.actor._
import akka.contrib.pattern.ShardRegion.Passivate
import akka.contrib.pattern.{ DistributedPubSubMediator, ClusterSharding, ShardRegion }
import akka.stream.Materializer
import akka.stream.actor._
import akka.stream.scaladsl._
import com.typesafe.config.Config
import scodec.DecodeResult
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ Drop, MTPackage }
import im.actor.server.presences.{ GroupPresenceManagerRegion, PresenceManagerRegion }
import im.actor.server.push.{ SeqUpdatesManagerRegion, WeakUpdatesManagerRegion }
import im.actor.server.{ models, persist }

case class SessionConfig(idleTimeout: Duration, reSendConfig: ReSenderConfig)

object SessionConfig {
  def load(config: Config): SessionConfig = {
    SessionConfig(
      idleTimeout = config.getDuration("idle-timeout", TimeUnit.SECONDS).seconds,
      reSendConfig = ReSenderConfig.fromConfig(config.getConfig("resend"))
    )
  }
}

object Session {

  import SessionMessage._

  private[this] val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, sessionId, payload) ⇒ (authId.toString + "-" + sessionId.toString, env)
  }

  private[this] val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(authId, sessionId, _) ⇒ (authId % 32).toString // TODO: configurable
  }

  def startRegion(props: Option[Props])(implicit system: ActorSystem): SessionRegion =
    SessionRegion(
      ClusterSharding(system).start(
        typeName = "Session",
        entryProps = props,
        idExtractor = idExtractor,
        shardResolver = shardResolver
      )
    )

  def startRegionProxy()(implicit system: ActorSystem): SessionRegion = startRegion(None)

  def props(mediator: ActorRef)(
    implicit
    config:                     SessionConfig,
    seqUpdManagerRegion:        SeqUpdatesManagerRegion,
    weakUpdManagerRegion:       WeakUpdatesManagerRegion,
    presenceManagerRegion:      PresenceManagerRegion,
    groupPresenceManagerRegion: GroupPresenceManagerRegion,
    db:                         Database,
    materializer:               Materializer
  ): Props =
    Props(
      classOf[Session],
      mediator,
      config,
      seqUpdManagerRegion,
      weakUpdManagerRegion,
      presenceManagerRegion,
      groupPresenceManagerRegion,
      db,
      materializer
    )
}

class Session(mediator: ActorRef)(
  implicit
  config:                     SessionConfig,
  seqUpdManagerRegion:        SeqUpdatesManagerRegion,
  weakUpdManagerRegion:       WeakUpdatesManagerRegion,
  presenceManagerRegion:      PresenceManagerRegion,
  groupPresenceManagerRegion: GroupPresenceManagerRegion,
  db:                         Database,
  materializer:               Materializer
)
  extends Actor with ActorLogging with MessageIdHelper with Stash {

  import SessionMessage._

  implicit val ec: ExecutionContext = context.dispatcher

  private[this] var optUserId: Option[Int] = None
  private[this] var clients = immutable.Set.empty[ActorRef]

  context.setReceiveTimeout(config.idleTimeout)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "Session failed")
  }

  def receive = waitingForEnvelope

  def waitingForEnvelope: Receive = {
    case env @ Envelope(authId, sessionId, _) ⇒
      val replyTo = sender()
      stash()

      val subscribe = DistributedPubSubMediator.Subscribe(AuthService.authIdTopic(authId), self)
      mediator ! subscribe

      context.become(waitingForSessionInfo(authId, sessionId, subscribe))

      // TODO: handle errors
      // TODO: refactor
      val infoAction = {
        persist.AuthId.find(authId) flatMap {
          case Some(authIdModel) ⇒
            persist.SessionInfo.find(authId, sessionId) flatMap {
              case s @ Some(sessionInfoModel) ⇒ DBIO.successful(s)
              case None ⇒
                val sessionInfoModel = models.SessionInfo(authId, sessionId, authIdModel.userId)

                for {
                  _ ← persist.SessionInfo.create(sessionInfoModel)
                } yield Some(sessionInfoModel)
            }
          case None ⇒ DBIO.successful(None)
        }
      }

      val infoFuture = db.run(infoAction)

      infoFuture map {
        case Some(info) ⇒ self ! info
        case None ⇒
          log.warning("Reporting AuthIdInvalid and dying")
          replyTo ! MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require)
          self ! PoisonPill
      }
    case msg ⇒ stash()
  }

  def waitingForSessionInfo(authId: Long, sessionId: Long, subscribe: DistributedPubSubMediator.Subscribe): Receive = {
    case info: models.SessionInfo ⇒
      log.debug("SessionInfo: {}", info)
      optUserId = info.optUserId
      unstashAll()
      context.become(waitingForSubscribeAck(authId, sessionId, subscribe))
    case msg ⇒ stash()
  }

  def waitingForSubscribeAck(authId: Long, sessionId: Long, subscribe: DistributedPubSubMediator.Subscribe): Receive = {
    case msg if msg == DistributedPubSubMediator.SubscribeAck(subscribe) ⇒
      unstashAll()
      context.become(anonymous(authId, sessionId))
    case msg ⇒
      stash()
  }

  def anonymous(authId: Long, sessionId: Long): Receive = {
    case env @ Envelope(authId, sessionId, HandleMessageBox(messageBoxBytes)) ⇒
      val client = sender()

      withValidMessageBox(client, messageBoxBytes) { mb ⇒
        val sessionMessagePublisher = context.actorOf(SessionMessagePublisher.props(), "messagePublisher")
        val rpcHandler = context.actorOf(RpcHandler.props, "rpcHandler")
        val updatesHandler = context.actorOf(UpdatesHandler.props(authId), "updatesHandler")
        val reSender = context.actorOf(ReSender.props(authId, sessionId)(config.reSendConfig), "reSender")

        val graph = SessionStream.graph(authId, sessionId, rpcHandler, updatesHandler, reSender)

        val flow = FlowGraph.closed(graph) { implicit b ⇒ g ⇒
          import FlowGraph.Implicits._

          val source = b.add(Source(ActorPublisher[SessionStreamMessage](sessionMessagePublisher)))
          val sink = b.add(Sink.foreach[MTPackage](m ⇒ clients foreach (_ ! m)))
          val bcast = b.add(Broadcast[MTPackage](2))

          // format: OFF

          source   ~> g ~> bcast ~> sink
                           bcast ~> Sink.onComplete {_ ⇒ log.warning("Dying due to stream completion"); self ! PoisonPill  }

          // format: ON
        }

        flow.run()

        recordClient(client, reSender)

        sessionMessagePublisher ! SessionStreamMessage.SendProtoMessage(NewSession(sessionId, mb.messageId))
        sessionMessagePublisher ! Tuple2(mb, ClientData(authId, sessionId, optUserId))

        context.become(resolved(authId, sessionId, sessionMessagePublisher, reSender))
      }
    case internal ⇒ handleInternal(authId, sessionId, internal)
  }

  def resolved(authId: Long, sessionId: Long, publisher: ActorRef, reSender: ActorRef): Receive = {
    case env @ Envelope(eauthId, esessionId, msg) ⇒
      val client = sender()

      if (authId != eauthId || sessionId != esessionId) // Should never happen
        log.error("Received Envelope with another's authId and sessionId {}", env)
      else
        handleSessionMessage(authId, sessionId, client, msg, publisher, reSender)
    case internal ⇒ handleInternal(authId, sessionId, internal)
  }

  private def recordClient(client: ActorRef, reSender: ActorRef): Unit = {
    if (!clients.contains(client)) {
      log.debug("New client")
      clients += client
      reSender ! ReSenderMessage.NewClient(client)
      context watch client
    }
  }

  private def handleSessionMessage(
    authId:    Long,
    sessionId: Long,
    client:    ActorRef,
    message:   SessionMessage,
    publisher: ActorRef,
    reSender:  ActorRef
  ): Unit = {
    message match {
      case HandleMessageBox(messageBoxBytes) ⇒
        withValidMessageBox(client, messageBoxBytes) { mb ⇒
          recordClient(client, reSender)
          publisher ! Tuple2(mb, ClientData(authId, sessionId, optUserId))
        }
      case cmd: SubscribeCommand ⇒
        publisher ! cmd
      case AuthorizeUser(userId) ⇒
        log.debug("User {} authorized session {}", userId, sessionId)

        this.optUserId = Some(userId)

        // TODO: handle errors
        db.run(persist.SessionInfo.updateUserId(authId, sessionId, this.optUserId).map(_ ⇒ AuthorizeUserAck(userId))) pipeTo sender()
    }
  }

  private def withValidMessageBox(client: ActorRef, messageBoxBytes: Array[Byte])(f: MessageBox ⇒ Unit): Unit =
    decodeMessageBox(messageBoxBytes) match {
      case Some(mb) ⇒ f(mb)
      case None ⇒
        log.warning("Failed to parse MessageBox. Droping client.")
        client ! Drop(0, 0, "Cannot parse MessageBox")
        context.stop(self)
    }

  private def decodeMessageBox(messageBoxBytes: Array[Byte]): Option[MessageBox] = {
    MessageBoxCodec.decode(BitVector(messageBoxBytes)).toEither match {
      case Right(DecodeResult(mb, _)) ⇒ Some(mb)
      case _                          ⇒ None
    }
  }

  private def handleInternal(authId: Long, sessionId: Long, message: Any) =
    message match {
      case AuthEvents.AuthIdInvalidated ⇒
        sendAuthIdInvalidAndStop(authId, sessionId)
      case ReceiveTimeout ⇒
        context.parent ! Passivate(stopMessage = PoisonPill)
      case Terminated(client) ⇒
        clients -= client
      case unmatched ⇒
        log.error("Received unmatched message {}", message)
    }

  private def sendAuthIdInvalidAndStop(authId: Long, sessionId: Long): Unit = {
    log.warning("Reporting AuthIdInvalid and dying")

    clients foreach { client ⇒
      client ! MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require)
    }
    self ! PoisonPill
  }
}
