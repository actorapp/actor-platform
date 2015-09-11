package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.group.{ GroupExtension, GroupProcessor, GroupProcessorRegion, GroupViewRegion }
import org.scalatest.Suite

trait ImplicitGroupRegions extends ImplicitUserRegions with ImplicitFileStorageAdapter with ActorSerializerPrepare {
  this: Suite ⇒

  GroupProcessor.register()

  protected implicit val system: ActorSystem

  protected implicit lazy val groupProcessorRegion: GroupProcessorRegion = GroupExtension(system).processorRegion
  protected implicit lazy val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion
}