package im.actor.server.push.google

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import io.circe.generic.auto._
import io.circe.syntax._
import spray.http.HttpHeaders.Authorization
import spray.http.HttpMethods.POST
import spray.http._

import scala.annotation.tailrec

private[google] object GooglePushDelivery {
  final case class Delivery(m: GooglePushMessage, key: String)

  private val MaxQueue = 100000

  def props(apiUri: String) = Props(classOf[GooglePushDelivery], apiUri)
}

private final class GooglePushDelivery(apiUri: String) extends ActorPublisher[NotificationDelivery] with ActorLogging {
  import ActorPublisherMessage._
  import GooglePushDelivery._

  private[this] var buf = Vector.empty[NotificationDelivery]
  private[this] val uri = Uri(apiUri)

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

  @tailrec private def deliverBuf(): Unit =
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

  private def mkJob(d: Delivery): NotificationDelivery =
    HttpRequest(
      method = POST,
      uri = uri,
      headers = List(Authorization(GenericHttpCredentials(s"key=${d.key}", Map.empty[String, String]))),
      entity = HttpEntity(ContentTypes.`application/json`, d.m.asJson.noSpaces)
    ) → d
}
