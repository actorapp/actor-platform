package im.actor.server.push

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import akka.actor._
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupExtension, GroupOffice, GroupViewRegion }
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManagerMessages.{ FatData, FatMetaData }
import im.actor.server.user.{ UserExtension, UserOffice, UserViewRegion }

sealed trait SeqUpdatesExtension extends Extension {
  val region: SeqUpdatesManagerRegion

  def getFatData(
    authId:      Long,
    fatMetaData: FatMetaData
  )(
    implicit
    ec: ExecutionContext
  ): DBIO[FatData]

  def getFatDataF(
    authId:      Long,
    fatMetaData: FatMetaData
  )(
    implicit
    ec: ExecutionContext
  ): Future[FatData]
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

  def getFatData(
    authId:      Long,
    fatMetaData: FatMetaData
  )(
    implicit
    ec: ExecutionContext
  ): DBIO[FatData] = {
    implicit lazy val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
    implicit lazy val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion

    getUserId(authId) flatMap { userId ⇒
      val usersFuture = Future.sequence(fatMetaData.userIds map (UserOffice.getApiStruct(_, userId, authId)))
      val groupsFuture = Future.sequence(fatMetaData.groupIds map (GroupOffice.getApiStruct(_, userId)))

      DBIO.from(for {
        users ← usersFuture
        groups ← groupsFuture
      } yield FatData(users, groups))
    }
  }

  def getFatDataF(
    authId:      Long,
    fatMetaData: FatMetaData
  )(
    implicit
    ec: ExecutionContext
  ): Future[FatData] = {
    implicit lazy val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
    implicit lazy val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion

    getUserIdF(authId) flatMap { userId ⇒
      val usersFuture = Future.sequence(fatMetaData.userIds map (UserOffice.getApiStruct(_, userId, authId)))
      val groupsFuture = Future.sequence(fatMetaData.groupIds map (GroupOffice.getApiStruct(_, userId)))

      for {
        users ← usersFuture
        groups ← groupsFuture
      } yield FatData(users, groups)
    }
  }

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