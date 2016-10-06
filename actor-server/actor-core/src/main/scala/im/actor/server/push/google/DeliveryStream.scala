package im.actor.server.push.google

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{ Flow, Source }
import akka.{ Done, NotUsed }
import cats.data.Xor
import im.actor.server.push.google.GooglePushDelivery.Delivery
import io.circe.parser
import spray.client.pipelining._
import spray.http.{ HttpCharsets, StatusCodes }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

private[google] final class DeliveryStream(publisher: ActorRef, serviceName: String, remove: String ⇒ Future[_])(implicit system: ActorSystem) {
  import system.dispatcher

  private val log = Logging(system, getClass)

  private implicit val mat = tolerantMaterializer

  log.debug("Starting {} stream", serviceName)

  val stream: Future[Done] = Source
    .fromPublisher(ActorPublisher[NotificationDelivery](publisher))
    .via(flow)
    .runForeach {
      // TODO: flatten
      case Xor.Right((body, delivery)) ⇒
        parser.parse(body) match {
          case Xor.Right(json) ⇒
            json.asObject match {
              case Some(obj) ⇒
                obj("error") flatMap (_.asString) match {
                  case Some("InvalidRegistration") ⇒
                    log.warning("{}: Invalid registration, deleting", serviceName)
                    remove(delivery.m.to)
                  case Some("NotRegistered") ⇒
                    log.warning("{}: Token is not registered, deleting", serviceName)
                    remove(delivery.m.to)
                  case Some(other) ⇒
                    log.warning("{}: Error in response: {}", serviceName, other)
                  case None ⇒
                    log.debug("{}: Successfully delivered: {}", serviceName, delivery)
                }
              case None ⇒
                log.error("{}: Expected JSON Object but got: {}", serviceName, json)
            }
          case Xor.Left(failure) ⇒ log.error(failure.underlying, "{}: Failed to parse response", serviceName)
        }
      case Xor.Left(e) ⇒
        log.error(e, "{}: Failed to make request", serviceName)
    }

  stream onComplete {
    case Failure(e) ⇒
      log.error(e, "{}: Failure in stream", serviceName)
    case Success(_) ⇒
      log.debug("{}: Stream completed", serviceName)
  }

  private def flow(implicit system: ActorSystem): Flow[NotificationDelivery, Xor[RuntimeException, (String, Delivery)], NotUsed] = {
    import system.dispatcher
    val pipeline = sendReceive
    Flow[NotificationDelivery].mapAsync(2) {
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
