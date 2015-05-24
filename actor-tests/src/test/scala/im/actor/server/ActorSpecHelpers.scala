package im.actor.server.api

import akka.actor.ActorSystem
import com.google.android.gcm.server.Sender
import slick.driver.PostgresDriver.api._

import im.actor.server.push.{ ApplePushManager, ApplePushManagerConfig, SeqUpdatesManager, SeqUpdatesManagerRegion }

trait ActorSpecHelpers {
  def buildSeqUpdManagerRegion()(implicit system: ActorSystem, db: Database): SeqUpdatesManagerRegion = {
    val gcmConfig = system.settings.config.getConfig("push.google")
    val appleConfig = ApplePushManagerConfig.fromConfig(system.settings.config.getConfig("push.apple"))

    implicit val gcmSender = new Sender(gcmConfig.getString("key"))

    implicit val applePushManager = new ApplePushManager(appleConfig, system)

    SeqUpdatesManager.startRegion()
  }
}
