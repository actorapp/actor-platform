package im.actor.server

import akka.actor.ActorSystem

import im.actor.server.group.{ GroupExtension, GroupProcessor, GroupProcessorRegion, GroupViewRegion }

trait ImplicitGroupRegions extends ImplicitUserRegions with ImplicitFileStorageAdapter {
  protected implicit val system: ActorSystem

  protected implicit lazy val groupProcessorRegion: GroupProcessorRegion = GroupExtension(system).processorRegion
  protected implicit lazy val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion
  GroupProcessor.register()
}
