package im.actor.server.session

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{ SubscribeAck, Subscribe }
import akka.cluster.sharding.ShardRegion.Passivate
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }
import akka.pattern.pipe
import akka.stream.{ ClosedShape, Materializer }
import akka.stream.actor._
import akka.stream.scaladsl._
import com.typesafe.config.Config
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server.db.DbExtension
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.Drop
import im.actor.server.persist.{ AuthSessionRepo, AuthIdRepo }
import im.actor.server.pubsub.PubSubExtension
import im.actor.server.user.{ AuthEvents, UserExtension }
import scodec.DecodeResult
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Success, Try }

final case class SessionConfig(idleTimeout: Duration, reSendConfig: ReSenderConfig)

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
    case env @ SessionEnvelope(authId, sessionId, payload) ⇒
      Try(env.getField(SessionEnvelope.descriptor.findFieldByNumber(payload.number))) match {
        case Success(any) ⇒ s"${authId}_$sessionId" → any
        case _            ⇒ throw new RuntimeException(s"Empty payload $env")
      }
  }

  private[this] val extractShardId: ShardRegion.ExtractShardId = {
    case SessionEnvelope(authId, sessionId, _) ⇒ (authId % 10).toString // TODO: configurable
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
  private case object AuthIdInvalid
}

final private class Session(implicit config: SessionConfig, materializer: Materializer) extends Actor with ActorLogging with MessageIdHelper with Stash {

  implicit val ec: ExecutionContext = context.dispatcher

  private val pubSubExt = PubSubExtension(context.system)
  private val db: Database = DbExtension(context.system).db

  private[this] var authData: Option[AuthData] = None
  private[this] var clients = immutable.Set.empty[ActorRef]

  private val (authId, sessionId) = self.path.name.split("_").toList match {
    case a :: s :: Nil ⇒ (a.toLong, s.toLong)
    case _ ⇒
      val e = new RuntimeException("Wrong actor name")
      log.error(e, e.getMessage)
      throw e
  }

  context.setReceiveTimeout(config.idleTimeout)

  db.run(AuthIdRepo.find(authId) flatMap {
    case Some(_) ⇒
      AuthSessionRepo.findByAuthId(authId) map {
        case Some(session) ⇒ Session.Initialized(Some(AuthData(session.userId, session.id)))
        case None          ⇒ Session.Initialized(None)
      }
    case None ⇒ DBIO.successful(Session.AuthIdInvalid)
  }) pipeTo self

  private val updatesHandler = context.actorOf(UpdatesHandler.props(authId), "updatesHandler")
  val reSender = context.actorOf(ReSender.props(authId, sessionId)(config.reSendConfig), "reSender")
  val sessionMessagePublisher = context.actorOf(SessionMessagePublisher.props(), "messagePublisher")
  val rpcHandler = context.actorOf(RpcHandler.props, "rpcHandler")

  def receive = initializing

  def initializing: Receive = {
    case Session.Initialized(authDataOpt) ⇒
      log.debug("Initialized: {}", authDataOpt)

      authDataOpt foreach {
        case AuthData(userId, authSid) ⇒ authorize(userId, authSid)
      }

      unstashAll()

      val subscribe = Subscribe(UserExtension(context.system).authIdTopic(authId), self)

      pubSubExt.subscribe(subscribe)

      val waiting: Receive = {
        case SubscribeAck(`subscribe`) ⇒
          unstashAll()
          context.become(anonymous)
        case msg ⇒
          stash()
      }

      context become (waiting orElse internal)
    case Session.AuthIdInvalid ⇒
      log.warning("AuthId invalid, waiting for message and dying...")
      unstashAll()

      val waiting: Receive = {
        case _: SessionMessage ⇒
          log.warning("Reporting AuthIdInvalid and dying")
          sender ! MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require
          context stop self
        case msg ⇒ log.warning("Ignoring {}", msg)
      }

      context become (waiting orElse internal)
    case msg ⇒ stash()
  }

  def anonymous: Receive = {
    case HandleMessageBox(messageBoxBytes) ⇒
      recordClient(sender())

      withValidMessageBox(messageBoxBytes.toByteArray) { mb ⇒
        val graph = SessionStream.graph(authId, sessionId, rpcHandler, updatesHandler, reSender)

        RunnableGraph.fromGraph(GraphDSL.create() { implicit b ⇒
          import GraphDSL.Implicits._

          val source = b.add(Source.fromPublisher(ActorPublisher[SessionStreamMessage](sessionMessagePublisher)))
          val sink = b.add(Sink.foreach[BitVector](m ⇒ clients foreach (_ ! m)))
          val encode = b.add(Flow[MessageBox].map(MessageBoxCodec.encode(_).require))
          val bcast = b.add(Broadcast[BitVector](2))

          // format: OFF

          source ~> graph ~> encode ~> bcast ~> sink
          bcast ~> Sink.onComplete { c ⇒
            c.failed foreach { e =>
              log.error(e, "Dying due to stream error");
            }
            self ! PoisonPill
          }

          // format: ON

          ClosedShape
        }).run()

        sessionMessagePublisher ! SessionStreamMessage.SendProtoMessage(NewSession(sessionId, mb.messageId))
        sessionMessagePublisher ! Tuple2(mb, ClientData(authId, sessionId, authData))

        unstashAll()
        context.become(resolved(sessionMessagePublisher, reSender))
      }
    case AuthorizeUser(userId, authSid) ⇒ authorize(userId, authSid, Some(sender()))
    case internal                       ⇒ handleInternal(internal, stashUnmatched = true)
  }

  def resolved(publisher: ActorRef, reSender: ActorRef): Receive = {
    case HandleMessageBox(messageBoxBytes) ⇒
      recordClient(sender())

      withValidMessageBox(messageBoxBytes.toByteArray) { mb ⇒
        publisher ! Tuple2(mb, ClientData(authId, sessionId, authData))
      }
    case cmd: SubscribeCommand ⇒
      publisher ! cmd
    case AuthorizeUser(userId, authSid) ⇒ authorize(userId, authSid, Some(sender()))
    case internal                       ⇒ handleInternal(internal, stashUnmatched = false)
  }

  private def authorize(userId: Int, authSid: Int, ackTo: Option[ActorRef] = None): Unit = {
    log.debug("User {} authorized session {}", userId, sessionId)

    this.authData = Some(AuthData(userId, authSid))

    updatesHandler ! UpdatesHandler.Authorize(userId, authSid)

    ackTo foreach (_ ! AuthorizeUserAck())
  }

  private def recordClient(client: ActorRef): Unit = {
    if (!clients.contains(client)) {
      log.debug("New client: {}", client)
      clients += client
      reSender ! ReSenderMessage.NewClient(client)
      context watch client
    }
  }

  private def withValidMessageBox(messageBoxBytes: Array[Byte])(f: MessageBox ⇒ Unit): Unit =
    decodeMessageBox(messageBoxBytes) match {
      case Some(mb) ⇒ f(mb)
      case None     ⇒ sendDropAndStop("Failed to parse MessageBox")
    }

  private def decodeMessageBox(messageBoxBytes: Array[Byte]): Option[MessageBox] = {
    MessageBoxCodec.decode(BitVector(messageBoxBytes)).toEither match {
      case Right(DecodeResult(mb, _)) ⇒ Some(mb)
      case _                          ⇒ None
    }
  }

  private def handleInternal(message: Any, stashUnmatched: Boolean) = {
    message match {
      case _ if internal.isDefinedAt(message) ⇒ internal(message)
      case AuthEvents.AuthIdInvalidated ⇒
        sendAuthIdInvalidAndStop()
      case unmatched ⇒
        if (stashUnmatched) {
          stash()
        } else {
          log.error("Received unmatched message {}", message)
        }
    }
  }

  private def internal: Receive = {
    case ReceiveTimeout ⇒
      log.debug("Receive timeout, passivating")
      context.parent ! Passivate(stopMessage = PoisonPill)
    case Terminated(client) ⇒
      clients -= client
  }

  private def sendAuthIdInvalidAndStop(): Unit = {
    log.warning("Reporting AuthIdInvalid and dying")
    val authIdInvalid = MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require
    clients foreach (_ ! authIdInvalid)
    self ! PoisonPill
  }

  private def sendDropAndStop(message: String): Unit = {
    log.warning("Sending Drop and dying: {}", message)
    val drop = Drop(0, 0, message)
    clients foreach (_ ! drop)
    self ! PoisonPill
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "Session failed")
  }

}
