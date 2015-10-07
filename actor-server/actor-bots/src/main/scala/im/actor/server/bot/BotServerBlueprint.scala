package im.actor.server.bot

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, Merge, Source }
import im.actor.api.rpc.Update
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.bots.{ BotMessageOut, BotMessages }
import im.actor.server.dialog.DialogExtension
import im.actor.server.sequence.{ SeqStateDate, SeqUpdatesManager }

import scala.concurrent.Future

final class BotServerBlueprint(botUserId: Int, botAuthId: Long, system: ActorSystem) {

  import BotMessages._
  import akka.stream.scaladsl.FlowGraph.Implicits._
  import system._

  private lazy val dialogExt = DialogExtension(system)
  private lazy val updBuilder = new BotUpdateBuilder(botUserId, botAuthId, system)

  val flow: Flow[BotRequest, BotMessageOut, Unit] = {
    val updSource =
      Source.actorPublisher[(Int, Update)](UpdatesSource.props(botAuthId))
        .mapAsync(1) {
          case (seq, update) ⇒ updBuilder(seq, update)
        }.collect {
          case Some(upd) ⇒ upd
        }

    val rqrspFlow = Flow[BotRequest]
      .mapAsync(1)(r ⇒ handleRequest(r.id, r.body))
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

  private def handleRequest(id: Long, body: RequestBody): Future[BotResponse] =
    for {
      response ← handleRequestBody(body)
    } yield BotResponse(id, response)

  private def handleRequestBody(body: RequestBody): Future[ResponseBody] = body match {
    case SendTextMessage(peer, randomId, message) ⇒ sendTextMessage(peer, randomId, message)
  }

  private def sendTextMessage(peer: OutPeer, randomId: Long, message: String): Future[ResponseBody] = {
    // FIXME: check access hash
    for {
      SeqStateDate(_, _, date) ← dialogExt.sendMessage(
        peer = ApiPeer(ApiPeerType(peer.`type`), peer.id),
        senderUserId = botUserId,
        senderAuthId = 0L,
        randomId = randomId,
        message = ApiTextMessage(message, Vector.empty, None),
        isFat = false
      )
    } yield MessageSent(date)
  }
}
