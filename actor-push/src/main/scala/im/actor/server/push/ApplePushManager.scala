package im.actor.server.push

import scala.collection.JavaConversions._

import com.relayrides.pushy.apns.{ PushManagerConfiguration, ApnsEnvironment, PushManager }
import com.relayrides.pushy.apns.util.{ SSLContextUtil, SimpleApnsPushNotification }
import com.typesafe.config.Config

case class ApplePushManagerConfig(certs: List[ApnsCert], isSandbox: Boolean)

object ApplePushManagerConfig {
  def fromConfig(config: Config): ApplePushManagerConfig = {
    ApplePushManagerConfig(
      certs = config.getConfigList("certs").toList map (ApnsCert.fromConfig),
      isSandbox = config.getBoolean("sandbox")
    )
  }
}

case class ApnsCert(key: Int, path: String, password: String)

object ApnsCert {
  def fromConfig(config: Config): ApnsCert = {
    ApnsCert(config.getInt("key"), config.getString("path"), config.getString("password"))
  }
}

class ApplePushManager(config: ApplePushManagerConfig) {
  private val managers: Map[Int, PushManager[SimpleApnsPushNotification]] =
    config.certs.map { cert ⇒
      val env = config.isSandbox match {
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

      mgr.start()

      (cert.key, mgr)
    }.toMap

  def getInstance(key: Int): Option[PushManager[SimpleApnsPushNotification]] = {
    managers.get(key)
  }
}
