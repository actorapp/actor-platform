package im.actor.server

import akka.actor.ActorSystem

import im.actor.server.group.{ GroupExtension, GroupProcessor, GroupProcessorRegion, GroupViewRegion }
import im.actor.server.dialog.group.{ GroupDialogExtension, GroupDialog, GroupDialogRegion }
import org.scalatest.Suite

trait ImplicitGroupRegions extends ImplicitUserRegions with ImplicitFileStorageAdapter with ActorSerializerPrepare {
  this: Suite ⇒

  GroupDialog.register()
  GroupProcessor.register()

  protected implicit val system: ActorSystem

  protected implicit lazy val groupProcessorRegion: GroupProcessorRegion = GroupExtension(system).processorRegion
  protected implicit lazy val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion
  protected implicit lazy val groupDialogRegion: GroupDialogRegion = GroupDialogExtension(system).region
}