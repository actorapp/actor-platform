package im.actor.server

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api.Database

import im.actor.server.group.{ GroupProcessor, GroupProcessorRegion }

trait ImplicitGroupRegions extends ImplicitUserRegions with ImplicitFileStorageAdapter {
  protected implicit val system: ActorSystem
  protected implicit val db: Database

  protected implicit lazy val groupProcessorRegion: GroupProcessorRegion = GroupProcessorRegion.start()
  GroupProcessor.register()
}
