package im.actor.server.api

import akka.actor.ActorSystem
import com.google.android.gcm.server.Sender
import com.relayrides.pushy.apns.{ PushManager, ApnsEnvironment, PushManagerConfiguration }
import com.relayrides.pushy.apns.util.{ SimpleApnsPushNotification, SSLContextUtil }
import slick.driver.PostgresDriver.api._

import im.actor.server.push.{ SeqUpdatesManagerRegion, SeqUpdatesManager }

trait ActorSpecHelpers {
  def buildSeqUpdManagerRegion()(implicit system: ActorSystem, db: Database): SeqUpdatesManagerRegion = {
    val gcmConfig = system.settings.config.getConfig("push.gcm")
    val apnsConfig = system.settings.config.getConfig("push.apns")

    implicit val gcmSender = new Sender(gcmConfig.getString("key"))

    implicit val apnsManager = new PushManager[SimpleApnsPushNotification](
      ApnsEnvironment.getProductionEnvironment,
      SSLContextUtil.createDefaultSSLContext(apnsConfig.getString("cert.path"), apnsConfig.getString("cert.password")),
      null,
      null,
      null,
      new PushManagerConfiguration(),
      "ActorPushManager"
    )

    SeqUpdatesManager.startRegion()
  }
}