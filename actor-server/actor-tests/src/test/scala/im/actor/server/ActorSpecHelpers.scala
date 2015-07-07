package im.actor.server.api

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.server.push._

trait ActorSpecHelpers {
  def buildSeqUpdManagerRegion()(implicit system: ActorSystem, db: Database): SeqUpdatesManagerRegion = {
    val appleConfig = ApplePushManagerConfig.load(system.settings.config.getConfig("push.apple"))

    implicit val googlePushManager = new GooglePushManager(GooglePushManagerConfig(List.empty))
    implicit val applePushManager = new ApplePushManager(appleConfig, system)

    SeqUpdatesManager.startRegion()
  }
}
