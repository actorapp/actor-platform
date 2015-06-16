package im.actor.server.api.rpc.service.llectro.interceptors

import scala.concurrent.{ Future, ExecutionContext }

import akka.actor.{ Actor, ActorLogging, ActorSystem }
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType._
import im.actor.server.api.rpc.service.llectro.LlectroAds
import im.actor.server.llectro.results._
import im.actor.server.models.llectro.LlectroDevice
import im.actor.server.persist

object PeerInterceptor {
  private[llectro] case object Resubscribe

  private[llectro] def interceptorGroupId(peer: Peer): String = {
    peer match {
      case Peer(Group, id)   ⇒ s"group-$id"
      case Peer(Private, id) ⇒ s"private-$id"
    }
  }
}

trait PeerInterceptor extends Actor with ActorLogging {

  protected[this] implicit val system: ActorSystem = context.system
  protected[this] implicit val ec: ExecutionContext = system.dispatcher

  protected def getLlectroDevices(userId: Int): dbio.DBIOAction[Seq[LlectroDevice], NoStream, Read with Read] = {
    for {
      authIds ← persist.AuthId.findByUserId(userId)
      devices ← persist.llectro.LlectroDevice.find(authIds.map(_.id).toSet)
    } yield devices
  }

  protected def fetchBanner(llectroAds: LlectroAds, banner: Banner): Future[(FileLocation, Long)] = {
    for {
      (filePath, fileSize) ← llectroAds.downloadBanner(banner)
      fileLocation ← llectroAds.uploadBannerInternally(banner, filePath, llectroAds.genBannerFileName(banner))
    } yield (fileLocation, fileSize)
  }
}