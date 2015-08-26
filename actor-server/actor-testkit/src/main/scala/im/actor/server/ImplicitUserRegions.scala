package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.dialog.privat.{ PrivateDialog, PrivateDialogExtension, PrivateDialogRegion }
import im.actor.server.user.{ UserExtension, UserProcessor, UserProcessorRegion, UserViewRegion }
import org.scalatest.Suite

trait ImplicitUserRegions extends ImplicitSocialManagerRegion with ImplicitSeqUpdatesManagerRegion with ActorSerializerPrepare {
  this: Suite ⇒

  PrivateDialog.register()
  UserProcessor.register()

  protected implicit val system: ActorSystem

  protected implicit lazy val userProcessorRegion: UserProcessorRegion = UserExtension(system).processorRegion
  protected implicit lazy val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
  protected implicit lazy val privateDialogRegion: PrivateDialogRegion = PrivateDialogExtension(system).region

}
