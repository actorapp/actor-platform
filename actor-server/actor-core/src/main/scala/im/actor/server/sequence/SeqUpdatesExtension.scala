package im.actor.server.sequence

import akka.actor._
import akka.util.Timeout
import im.actor.server.db.DbExtension
import im.actor.server.persist
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

sealed trait SeqUpdatesExtension extends Extension {
  val region: SeqUpdatesManagerRegion
}

final class SeqUpdatesExtensionImpl(
  _system: ActorSystem,
  gpm:     GooglePushManager,
  apm:     ApplePushManager
) extends SeqUpdatesExtension {
  private implicit val OperationTimeout = Timeout(30.seconds)
  private implicit val system: ActorSystem = _system
  private implicit lazy val db: Database = DbExtension(system).db

  lazy val region: SeqUpdatesManagerRegion = SeqUpdatesManagerRegion.start()(system, gpm, apm)

  def getUserId(authId: Long)(implicit ec: ExecutionContext): DBIO[Int] =
    persist.AuthId.findUserId(authId) map (_.getOrElse(throw new Exception(s"Cannot get userId for a non-authorized authId ${authId}")))

  def getUserIdF(authId: Long)(implicit ec: ExecutionContext): Future[Int] =
    db.run(getUserId(authId))
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