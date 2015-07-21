package im.actor.server.api.rpc.service.messaging

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.concurrent._
import scala.concurrent.duration._

import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.utils.cache.CacheHelpers._

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl ⇒

  import PeerHelpers._
  import im.actor.api.rpc.Implicits._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  implicit private val timeout: Timeout = Timeout(5.seconds) // TODO: configurable

  private val MaxCacheSize = 100L
  private val caches = scala.collection.mutable.Map.empty[Long, Cache[java.lang.Long, Future[HandlerResult[ResponseSeqDate]]]]

  override def jhandleSendMessage(outPeer: OutPeer, randomId: Long, message: Message, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val cache = caches.getOrElseUpdate(clientData.authId, createCache[java.lang.Long, Future[HandlerResult[ResponseSeqDate]]](MaxCacheSize))

    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(outPeer) {
        val dateTime = new DateTime
        val dateMillis = dateTime.getMillis

        val seqstateAction = outPeer.`type` match {
          case PeerType.Private ⇒
            DBIO.from(UserOffice.sendMessage(outPeer.id, client.userId, client.authId, randomId, dateTime, message))
          case PeerType.Group ⇒
            DBIO.from(GroupOffice.sendMessage(outPeer.id, client.userId, client.authId, randomId, dateTime, message))
        }

        for (seqstate ← seqstateAction) yield {
          val fromPeer = Peer(PeerType.Private, client.userId)
          val toPeer = outPeer.asPeer
          onMessage(Events.PeerMessage(fromPeer.asModel, toPeer.asModel, randomId, dateMillis, message))
          Ok(ResponseSeqDate(seqstate.seq, seqstate.state.toByteArray, dateMillis))
        }
      }
    }

    Option(cache getIfPresent (randomId)) match {
      case Some(resFuture) ⇒
        resFuture
      case None ⇒
        val resFuture = db.run(toDBIOAction(authorizedAction))
        cache.put(randomId, resFuture)

        resFuture onFailure {
          case _ ⇒ cache.invalidate(randomId)
        }

        resFuture
    }
  }
}
