package im.actor.server.sequence

import akka.actor._
import akka.util.Timeout
import im.actor.server.db.DbExtension
import im.actor.server.models
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Success, Try }

sealed trait SeqUpdatesExtension extends Extension {
  val region: SeqUpdatesManagerRegion

  def persistUpdate(upd: models.sequence.SeqUpdate): Future[Unit]
}

final class SeqUpdatesExtensionImpl(
  _system: ActorSystem,
  gpm:     GooglePushManager,
  apm:     ApplePushManager
) extends SeqUpdatesExtension {
  private implicit val OperationTimeout = Timeout(30.seconds)
  private implicit val system: ActorSystem = _system
  private implicit lazy val db: Database = DbExtension(system).db

  SeqUpdatesManager.register()

  lazy val region: SeqUpdatesManagerRegion = SeqUpdatesManagerRegion.start()(system, gpm, apm)

  private val writer = system.actorOf(BatchUpdatesWriter.props, "batch-updates-writer")

  override def persistUpdate(update: models.sequence.SeqUpdate): Future[Unit] = {
    val promise = Promise[Unit]()
    writer ! BatchUpdatesWriter.Enqueue(update, promise)
    promise.future
  }
}

object SeqUpdatesExtension extends ExtensionId[SeqUpdatesExtension] with ExtensionIdProvider {
  override def lookup = SeqUpdatesExtension

  override def createExtension(system: ExtendedActorSystem) = {
    val applePushConfig = ApplePushManagerConfig.load(
      Try(system.settings.config.getConfig("push.apple"))
        .getOrElse(system.settings.config.getConfig("services.apple.push"))
    )
    val googlePushConfig = GooglePushManagerConfig.load(system.settings.config.getConfig("services.google.push")).get

    val gpm = new GooglePushManager(googlePushConfig)
    val apm = new ApplePushManager(applePushConfig, system)

    new SeqUpdatesExtensionImpl(system, gpm, apm)
  }
}