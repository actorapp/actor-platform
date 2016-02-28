package im.actor.server.sequence

import java.util

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.event.Logging
import com.relayrides.pushy.apns._
import com.relayrides.pushy.apns.util.{ SSLContextUtil, SimpleApnsPushNotification }
import com.typesafe.config.Config
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

case class ApplePushManagerConfig(certs: List[ApnsCert])

object ApplePushManagerConfig {
  def load(config: Config): ApplePushManagerConfig = {
    ApplePushManagerConfig(
      certs = config.getConfigList("certs").toList map ApnsCert.fromConfig
    )

  }
}

case class ApnsCert(key: Int, path: String, password: String, isSandbox: Boolean, isVoip: Boolean)

object ApnsCert {
  def fromConfig(config: Config): ApnsCert = {
    ApnsCert(
      config.getInt("key"),
      config.getString("path"),
      config.getString("password"),
      Try(config.getBoolean("voip")).getOrElse(false),
      Try(config.getBoolean("sandbox")).getOrElse(false)
    )
  }
}

object ApplePushExtension extends ExtensionId[ApplePushExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ApplePushExtension = new ApplePushExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = ApplePushExtension
}

final class ApplePushExtension(system: ActorSystem) extends Extension {
  import system.dispatcher

  private val config = ApplePushManagerConfig.load(
    Try(system.settings.config.getConfig("services.apple.push"))
      .getOrElse(system.settings.config.getConfig("push.apple"))
  )

  private val (managers, voipManagers): (Map[Int, PushManager[SimpleApnsPushNotification]], Map[Int, PushManager[SimpleApnsPushNotification]]) = {
    val (certs, voipCerts) = config.certs.partition(!_.isVoip)

    ((certs map createManager).toMap, (voipCerts map createManager).toMap)
  }

  def getInstance(key: Int): Option[PushManager[SimpleApnsPushNotification]] = managers.get(key)

  def getVoipInstance(key: Int): Option[PushManager[SimpleApnsPushNotification]] = voipManagers.get(key)

  private def createManager(cert: ApnsCert) = {
    val env = cert.isSandbox match {
      case false ⇒ ApnsEnvironment.getProductionEnvironment
      case true  ⇒ ApnsEnvironment.getSandboxEnvironment
    }

    cert.isSandbox match {
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

    system.scheduler.schedule(10.seconds, 1.hour) {
      mgr.requestExpiredTokens()
    }

    (cert.key, mgr)
  }
}

private class LoggingRejectedNotificationListener(_system: ActorSystem) extends RejectedNotificationListener[SimpleApnsPushNotification] {
  private implicit val system: ActorSystem = _system
  private implicit val ec: ExecutionContext = _system.dispatcher
  private lazy val seqUpdExt = SeqUpdatesExtension(system)
  private val log = Logging(system, getClass)

  override def handleRejectedNotification(pushManager: PushManager[_ <: SimpleApnsPushNotification], notification: SimpleApnsPushNotification, rejectionReason: RejectedNotificationReason): Unit = {
    log.warning("APNS rejected notification with reason: {}", rejectionReason)

    if (rejectionReason.getErrorCode == RejectedNotificationReason.INVALID_TOKEN.getErrorCode) {
      log.warning("Deleting token")
      log.error("Implement push token deletion")
      seqUpdExt.deleteApplePushCredentials(notification.getToken)
    }
  }
}

private class CleanExpiredTokenListener(_system: ActorSystem) extends ExpiredTokenListener[SimpleApnsPushNotification] {
  private implicit val system: ActorSystem = _system
  private val log = Logging(system, getClass)
  implicit val db: Database = DbExtension(system).db

  override def handleExpiredTokens(
    pushManager:   PushManager[_ <: SimpleApnsPushNotification],
    expiredTokens: util.Collection[ExpiredToken]
  ): Unit = {
    expiredTokens foreach { t ⇒
      log.warning("APNS reported expired token")
      //UserExtension(system).logoutByAppleToken(t.getToken)
    }
  }
}