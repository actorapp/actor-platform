package im.actor.server.sequence

import java.util

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.util.Timeout
import com.relayrides.pushy.apns._
import com.relayrides.pushy.apns.util.{ SSLContextUtil, SimpleApnsPushNotification }
import com.typesafe.config.Config
import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.db.DbExtension
import im.actor.server.user.{ UserProcessorRegion, UserExtension, UserOffice }

import scala.util.Try

case class ApplePushManagerConfig(certs: List[ApnsCert])

object ApplePushManagerConfig {
  def load(config: Config): ApplePushManagerConfig = {
    ApplePushManagerConfig(
      certs = config.getConfigList("certs").toList map ApnsCert.fromConfig
    )

  }
}

case class ApnsCert(key: Int, path: String, password: String, isSandbox: Boolean)

object ApnsCert {
  def fromConfig(config: Config): ApnsCert = {
    ApnsCert(
      config.getInt("key"),
      config.getString("path"),
      config.getString("password"),
      Try(config.getBoolean("sandbox")).getOrElse(false)
    )
  }
}

class ApplePushManager(config: ApplePushManagerConfig, system: ActorSystem) {
  private implicit val ec: ExecutionContext = system.dispatcher

  private val managers: Map[Int, PushManager[SimpleApnsPushNotification]] =
    config.certs.map { cert ⇒
      val env = cert.isSandbox match {
        case false ⇒ ApnsEnvironment.getProductionEnvironment
        case true  ⇒ ApnsEnvironment.getSandboxEnvironment
      }

      val mgr = new PushManager[SimpleApnsPushNotification](
        env,
        SSLContextUtil.createDefaultSSLContext(cert.path, cert.password),
        null,
        null,
        null,
        new PushManagerConfiguration(),
        s"ActorPushManager-${cert.key}"
      )
      mgr.registerRejectedNotificationListener(new LoggingRejectedNotificationListener(system))
      mgr.registerExpiredTokenListener(new CleanExpiredTokenListener(system))

      mgr.start()

      system.scheduler.schedule(0.seconds, 1.hour) {
        mgr.requestExpiredTokens()
      }

      (cert.key, mgr)
    }.toMap

  def getInstance(key: Int): Option[PushManager[SimpleApnsPushNotification]] =
    managers.get(key)
}

private class LoggingRejectedNotificationListener(_system: ActorSystem) extends RejectedNotificationListener[SimpleApnsPushNotification] {
  private implicit val system: ActorSystem = _system
  private implicit val ec: ExecutionContext = _system.dispatcher

  override def handleRejectedNotification(pushManager: PushManager[_ <: SimpleApnsPushNotification], notification: SimpleApnsPushNotification, rejectionReason: RejectedNotificationReason): Unit = {
    system.log.warning("APNS rejected notification with reason: {}", rejectionReason)

    if (rejectionReason.getErrorCode == RejectedNotificationReason.INVALID_TOKEN.getErrorCode) {
      system.log.warning("Deleting token")
      SeqUpdatesManager.deleteApplePushToken(notification.getToken)
    }
  }
}

private class CleanExpiredTokenListener(_system: ActorSystem) extends ExpiredTokenListener[SimpleApnsPushNotification] {
  private implicit val system: ActorSystem = _system
  private implicit val ec: ExecutionContext = _system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds)
  private implicit val db: Database = DbExtension(_system).db
  private implicit val userProcessorRegion: UserProcessorRegion = UserExtension(_system).processorRegion

  override def handleExpiredTokens(
    pushManager:   PushManager[_ <: SimpleApnsPushNotification],
    expiredTokens: util.Collection[ExpiredToken]
  ): Unit = {
    expiredTokens foreach { t ⇒
      system.log.warning("APNS reported expired token, loggint out")
      UserOffice.logoutByAppleToken(t.getToken)
    }
  }
}