package im.actor.server.bot

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, Merge, Source }
import im.actor.api.rpc.Update
import im.actor.bots.BotMessages
import im.actor.server.bot.services.{ WebHooksBotService, BotsBotService, KeyValueBotService, MessagingBotService }
import upickle.Js

import scala.concurrent.Future

final class BotServerBlueprint(botUserId: Int, botAuthId: Long, system: ActorSystem) {

  import BotMessages._
  import BotServiceTypes._
  import akka.stream.scaladsl.FlowGraph.Implicits._
  import system.dispatcher

  private lazy val updBuilder = new BotUpdateBuilder(botUserId, botAuthId, system)
  private val msgService = new MessagingBotService(system)
  private val kvService = new KeyValueBotService(system)
  private val botsService = new BotsBotService(system)
  private val webhooksService = new WebHooksBotService(system)

  val flow: Flow[BotRequest, BotMessageOut, Unit] = {
    val updSource =
      Source.actorPublisher[(Int, Update)](UpdatesSource.props(botAuthId))
        .mapAsync(1) {
          case (seq, update) ⇒ updBuilder(seq, update)
        }.collect {
          case Some(upd) ⇒ upd
        }

    val rqrspFlow = Flow[BotRequest]
      .mapAsync(1) {
        case BotRequest(id, service, body) ⇒ handleRequest(id, service, body)
      }
      .map(_.asInstanceOf[BotMessageOut])

    Flow() { implicit b ⇒
      val upd = b.add(updSource)
      val rqrsp = b.add(rqrspFlow)
      val merge = b.add(Merge[BotMessageOut](2))

      upd ~> merge
      rqrsp ~> merge

      (rqrsp.inlet, merge.out)
    }
  }

  private def handleRequest(id: Long, service: String, body: RequestBody): Future[BotResponse] = {
    val resultFuture =
      if (services.isDefinedAt(service)) {
        val handlers = services(service).handlers

        if (handlers.isDefinedAt(body)) {
          for {
            response ← handlers(body).handle(botUserId, botAuthId)
          } yield response
        } else Future.successful(BotError(400, "REQUEST_NOT_SUPPORTED", Js.Obj(), None))
      } else Future.successful(BotError(400, "SERVICE_NOT_REGISTERED", Js.Obj(), None))

    resultFuture map (BotResponse(id, _))
  }

  private val services: PartialFunction[String, BotServiceBase] = {
    case Services.KeyValue  ⇒ kvService
    case Services.Messaging ⇒ msgService
    case Services.Bots      ⇒ botsService
    case Services.WebHooks  ⇒ webhooksService
  }
}
