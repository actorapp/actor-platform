package im.actor.server.sequence

import akka.NotUsed
import akka.actor._
import akka.event.Logging
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import akka.stream.scaladsl.{ Flow, Source }
import cats.data.Xor
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import im.actor.server.db.DbExtension
import im.actor.server.model.push.GooglePushCredentials
import im.actor.server.persist.push.GooglePushCredentialsRepo
import io.circe.generic.auto._
import io.circe.jawn._
import io.circe.syntax._
import spray.client.pipelining._
import spray.http.HttpHeaders.Authorization
import spray.http._

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

case class GooglePushKey(projectId: Long, key: String)

object GooglePushKey {
  def load(config: Config): Try[GooglePushKey] = {
    for {
      projectId ← config.get[Try[Long]]("project-id")
      key ← config.get[Try[String]]("key")
    } yield GooglePushKey(projectId, key)
  }
}

case class GooglePushManagerConfig(keys: List[GooglePushKey])

object GooglePushManagerConfig {
  def load(googlePushConfig: Config): Try[GooglePushManagerConfig] =
    for {
      keyConfigs ← googlePushConfig.get[Try[List[Config]]]("keys")
      keys ← Try(keyConfigs map (GooglePushKey.load(_).get))
    } yield GooglePushManagerConfig(keys)
}

final case class GooglePushMessage(
  to:           String,
  collapse_key: Option[String],
  data:         Option[Map[String, String]],
  time_to_live: Option[Int]
)

object GooglePushExtension extends ExtensionId[GooglePushExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): GooglePushExtension = new GooglePushExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = GooglePushExtension
}

final class GooglePushExtension(system: ActorSystem) extends Extension {

  import system.dispatcher

  private implicit val _system = system

  private val streamDecider: Supervision.Decider = {
    case _: RuntimeException ⇒ Supervision.Resume
    case _                   ⇒ Supervision.Stop
  }
  private implicit val mat = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(streamDecider))

  private val log = Logging(system, getClass)
  private val db = DbExtension(system).db

  private val config = GooglePushManagerConfig.load(system.settings.config.getConfig("services.google.push")).get
  private val deliveryPublisher = system.actorOf(GooglePushDelivery.props, "google-push-delivery")

  Source.fromPublisher(ActorPublisher[(HttpRequest, GooglePushDelivery.Delivery)](deliveryPublisher))
    .via(GooglePushDelivery.flow)
    .runForeach {
      // TODO: flatten
      case Xor.Right((body, delivery)) ⇒
        parse(body) match {
          case Xor.Right(json) ⇒
            json.asObject match {
              case Some(obj) ⇒
                obj("error") flatMap (_.asString) match {
                  case Some("InvalidRegistration") ⇒
                    log.warning("Invalid registration, deleting")
                    remove(delivery.m.to)
                  case Some("NotRegistered") ⇒
                    log.warning("Token is not registered, deleting")
                    remove(delivery.m.to)
                  case Some(other) ⇒
                    log.warning("Error in GCM response: {}", other)
                  case None ⇒
                    log.debug("Successfully delivered: {}", delivery)
                }
              case None ⇒
                log.error("Expected JSON Object but got: {}", json)
            }
          case Xor.Left(failure) ⇒ log.error(failure.underlying, "Failed to parse response")
        }
      case Xor.Left(e) ⇒
        log.error(e, "Failed to make request")
    } onComplete {
      case Failure(e) ⇒
        log.error(e, "Failure in stream, continue with next element")
      case Success(_) ⇒ log.debug("Stream completed")
    }

  private def remove(regId: String): Future[Int] = db.run(GooglePushCredentialsRepo.deleteByToken(regId))

  private val keys: Map[Long, String] =
    (config.keys map {
      case GooglePushKey(projectId, key) ⇒ projectId → key
    }).toMap

  def send(projectId: Long, message: GooglePushMessage): Unit =
    keys get projectId match {
      case Some(key) ⇒
        deliveryPublisher ! GooglePushDelivery.Delivery(message, key)
      case None ⇒
        log.warning("Key not found for projectId: {}", projectId)
    }

  def fetchCreds(authIds: Set[Long]): Future[Seq[GooglePushCredentials]] =
    db.run(GooglePushCredentialsRepo.find(authIds))
}

private object GooglePushDelivery {

  object Tick

  final case class Delivery(m: GooglePushMessage, key: String)

  private val MaxQueue = 100000

  def props = Props(classOf[GooglePushDelivery])

  def flow(implicit system: ActorSystem): Flow[(HttpRequest, Delivery), Xor[RuntimeException, (String, Delivery)], NotUsed] = {
    import system.dispatcher
    val pipeline = sendReceive
    Flow[(HttpRequest, GooglePushDelivery.Delivery)].mapAsync(2) {
      case (req, del) ⇒
        pipeline(req) map { resp ⇒
          if (resp.status == StatusCodes.OK)
            Xor.Right(resp.entity.data.asString(HttpCharsets.`UTF-8`) → del)
          else
            Xor.Left(new RuntimeException(s"Failed to deliver message, StatusCode was not OK: ${resp.status}"))
        }
    }
  }
}

private final class GooglePushDelivery extends ActorPublisher[(HttpRequest, GooglePushDelivery.Delivery)] with ActorLogging {

  import GooglePushDelivery._
  import ActorPublisherMessage._

  private[this] var buf = Vector.empty[(HttpRequest, Delivery)]
  private val uri = Uri("https://gcm-http.googleapis.com/gcm/send")

  def receive = {
    case d: Delivery if buf.size == MaxQueue ⇒
      log.error("Current queue is already at size MaxQueue: {}, totalDemand: {}, ignoring delivery", MaxQueue, totalDemand)
      deliverBuf()
    case d: Delivery ⇒
      log.debug("Trying to deliver google push. Queue size: {}, totalDemand: {}", buf.size, totalDemand)
      if (buf.isEmpty && totalDemand > 0) {
        onNext(mkJob(d))
      } else {
        this.buf :+= mkJob(d)
        deliverBuf()
      }
    case Request(n) ⇒
      log.debug("Trying to deliver google push. Queue size: {}, totalDemand: {}, subscriber requests {} elements", buf.size, totalDemand, n)
      deliverBuf()
  }

  @tailrec def deliverBuf(): Unit =
    if (totalDemand > 0) {
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use foreach onNext
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use foreach onNext
        deliverBuf()
      }
    }

  private def mkJob(d: Delivery): (HttpRequest, Delivery) = {
    HttpRequest(
      method = HttpMethods.POST,
      uri = uri,
      headers = List(Authorization(GenericHttpCredentials(s"key=${d.key}", Map.empty[String, String]))),
      entity = HttpEntity(ContentTypes.`application/json`, d.m.asJson.noSpaces)
    ) → d
  }
}