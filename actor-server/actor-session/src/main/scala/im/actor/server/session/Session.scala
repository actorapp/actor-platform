package im.actor.server.session

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.cluster.sharding.ShardRegion.Passivate
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.pattern.pipe
import akka.stream.{ Materializer, ActorMaterializer }
import akka.stream.actor._
import akka.stream.scaladsl._
import com.typesafe.config.Config
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server.db.DbExtension
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.{ Drop, MTPackage }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.user.{ AuthEvents, UserExtension }
import im.actor.server.{ model, persist }
import scodec.DecodeResult
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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

  private[this] val extractEntityId: ShardRegion.ExtractEntityId = {
    case env @ SessionEnvelope(authId, sessionId, payload) ⇒ (authId.toString + "-" + sessionId.toString, env)
  }

  private[this] val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case SessionEnvelope(authId, sessionId, _) ⇒ (authId % 32).toString // TODO: configurable
  }

  private val typeName = "Session"

  def startRegion(props: Props)(implicit system: ActorSystem): SessionRegion =
    SessionRegion(
      ClusterSharding(system).start(
        typeName = typeName,
        entityProps = props,
        settings = ClusterShardingSettings(system),
        extractEntityId = extractEntityId,
        extractShardId = extractShardId
      )
    )

  def startRegionProxy()(implicit system: ActorSystem): SessionRegion = SessionRegion(
    ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )
  )

  def props(implicit config: SessionConfig, materializer: Materializer): Props =
    Props(classOf[Session], config, materializer)

  private final case class Initialized(authDataOpt: Option[AuthData])
}

class Session(implicit config: SessionConfig, materializer: Materializer) extends Actor with ActorLogging with MessageIdHelper with Stash {

  import SessionEnvelope.Payload

  implicit val ec: ExecutionContext = context.dispatcher

  private val mediator: ActorRef = DistributedPubSub(context.system).mediator
  private implicit val db: Database = DbExtension(context.system).db
  private implicit val seqUpdManagerRegion = SeqUpdatesExtension(context.system).region

  private[this] var authDataOpt: Option[AuthData] = None
  private[this] var clients = immutable.Set.empty[ActorRef]

  context.setReceiveTimeout(config.idleTimeout)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "Session failed")
  }

  def receive = waitingForEnvelope

  def waitingForEnvelope: Receive = {
    case env @ SessionEnvelope(authId, sessionId, _) ⇒
      val replyTo = sender()
      stash()

      val subscribe = DistributedPubSubMediator.Subscribe(UserExtension(context.system).authIdTopic(authId), self)
      mediator ! subscribe

      context.become(waitingForSessionInfo(authId, sessionId, subscribe))

      db.run(persist.AuthIdRepo.find(authId)) foreach {
        case Some(_) ⇒
          db.run(persist.AuthSessionRepo.findByAuthId(authId) map {
            case Some(session) ⇒ Session.Initialized(Some(AuthData(session.userId, session.id)))
            case None          ⇒ Session.Initialized(None)
          }) pipeTo self
        case None ⇒
          log.warning("Reporting AuthIdInvalid and dying")
          //call helper. нет такого auth id
          replyTo ! MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require)
          self ! PoisonPill
      }
    case msg ⇒ stash()
  }

  def waitingForSessionInfo(authId: Long, sessionId: Long, subscribe: DistributedPubSubMediator.Subscribe): Receive = {
    case Session.Initialized(authDataOpt) ⇒
      log.debug("Initialized: {}", authDataOpt)
      this.authDataOpt = authDataOpt
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
    case env @ SessionEnvelope(authId, sessionId, Payload.HandleMessageBox(HandleMessageBox(messageBoxBytes))) ⇒
      val client = sender()

      withValidMessageBox(client, messageBoxBytes.toByteArray) { mb ⇒
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

          source ~> g ~> bcast ~> sink
          bcast ~> Sink.onComplete { c ⇒
            c.failed foreach { e =>
              log.error(e, "Dying due to stream error");
            }
            self ! PoisonPill
          }

          // format: ON
        }

        flow.run()

        recordClient(client, reSender)

        sessionMessagePublisher ! SessionStreamMessage.SendProtoMessage(NewSession(sessionId, mb.messageId))
        sessionMessagePublisher ! Tuple2(mb, ClientData(authId, sessionId, authDataOpt))

        unstashAll()
        context.become(resolved(authId, sessionId, sessionMessagePublisher, reSender, updatesHandler))
      }
    case internal ⇒ handleInternal(authId, sessionId, internal, stashUnmatched = true)
  }

  def resolved(authId: Long, sessionId: Long, publisher: ActorRef, reSender: ActorRef, updatesHandler: ActorRef): Receive = {
    case env @ SessionEnvelope(eauthId, esessionId, (msg)) ⇒
      val client = sender()

      if (authId != eauthId || sessionId != esessionId) // Should never happen
        log.error("Received Envelope with another's authId and sessionId {}", env)
      else
        handleSessionMessage(authId, sessionId, client, msg, publisher, reSender, updatesHandler)
    case internal ⇒ handleInternal(authId, sessionId, internal, stashUnmatched = false)
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
    authId:         Long,
    sessionId:      Long,
    client:         ActorRef,
    message:        Payload,
    publisher:      ActorRef,
    reSender:       ActorRef,
    updatesHandler: ActorRef
  ): Unit = {
    message match {
      case Payload.HandleMessageBox(HandleMessageBox(messageBoxBytes)) ⇒
        withValidMessageBox(client, messageBoxBytes.toByteArray) { mb ⇒
          recordClient(client, reSender)
          publisher ! Tuple2(mb, ClientData(authId, sessionId, authDataOpt))
        }
      case _: Payload.SubscribeToOnline | _: Payload.SubscribeFromOnline | _: Payload.SubscribeToGroupOnline | _: Payload.SubscribeFromGroupOnline ⇒
        val cmd: SubscribeCommand =
          message.subscribeToOnline
            .orElse(message.subscribeFromOnline)
            .orElse(message.subscribeToGroupOnline)
            .orElse(message.subscribeFromGroupOnline)
            .get

        publisher ! cmd
      case Payload.AuthorizeUser(AuthorizeUser(userId, authSid)) ⇒
        log.debug("User {} authorized session {}", userId, sessionId)

        this.authDataOpt = Some(AuthData(userId, authSid))

        updatesHandler ! UpdatesHandler.Authorize(userId, authSid)
      case unmatched ⇒
        log.error("Unmatched session message {}", unmatched)
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

  private def handleInternal(authId: Long, sessionId: Long, message: Any, stashUnmatched: Boolean) =
    message match {
      case AuthEvents.AuthIdInvalidated ⇒
        sendAuthIdInvalidAndStop(authId, sessionId)
      case ReceiveTimeout ⇒
        context.parent ! Passivate(stopMessage = PoisonPill)
      case Terminated(client) ⇒
        clients -= client
      case unmatched ⇒
        if (stashUnmatched) {
          stash()
        } else {
          log.error("Received unmatched message {}", message)
        }
    }

  private def sendAuthIdInvalidAndStop(authId: Long, sessionId: Long): Unit = {
    log.warning("Reporting AuthIdInvalid and dying")

    clients foreach { client ⇒
      client ! MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require)
    }
    self ! PoisonPill
  }
}
