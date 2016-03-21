package im.actor.server.api.rpc.service.stats

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.collections.ApiRawValue
import im.actor.api.rpc.collections.{ ApiArrayValue, ApiInt32Value, ApiInt64Value, _ }
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.stats._
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.model.ClientStats
import im.actor.server.persist.ClientStatsRepo
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }

final class StatsServiceImpl()(implicit system: ActorSystem) extends StatsService {
  implicit protected val ec: ExecutionContext = system.dispatcher
  private val db = DbExtension(system).db

  protected def doHandleStoreEvents(events: IndexedSeq[ApiEvent], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { implicit client ⇒
      db.run(ClientStatsRepo.create(events map apiEventToStat)) map (_ ⇒ Ok(ResponseVoid))
    }
  }

  private def apiEventToStat(event: ApiEvent)(implicit client: AuthorizedClientData): ClientStats = event match {
    case ApiAppVisibleChanged(visible) ⇒
      stats("VisibleChanged", csv(List(visible.toString)))
    case ApiContentViewChanged(contentType, contentId, visible, params) ⇒
      stats("ContentViewChanged", csv(List(contentType, contentId, visible.toString) ++ stringList(params)))
    case ApiUntypedEvent(eventType, params) ⇒
      stats(eventType, csv(stringList(params)))
  }

  private def stats(eventType: String, eventData: String)(implicit client: AuthorizedClientData) =
    ClientStats(ACLUtils.randomLong(), client.userId, client.authId, eventType, eventData)

  private def csv(items: List[String]) = items mkString ";"

  private def stringList(params: Option[ApiRawValue]) =
    params map (p ⇒ List(toJs(p).toString)) getOrElse List.empty[String]

  private def toJs(raw: ApiRawValue): JsValue = raw match {
    case ApiMapValue(items) ⇒
      val fields = items map { case ApiMapValueItem(key, value) ⇒ key → toJs(value) }
      JsObject(fields)
    case ApiStringValue(t)     ⇒ JsString(t)
    case ApiDoubleValue(d)     ⇒ JsNumber(d)
    case ApiInt32Value(i)      ⇒ JsNumber(i)
    case ApiInt64Value(l)      ⇒ JsNumber(l)
    case ApiArrayValue(values) ⇒ JsArray(values map toJs)
  }

}