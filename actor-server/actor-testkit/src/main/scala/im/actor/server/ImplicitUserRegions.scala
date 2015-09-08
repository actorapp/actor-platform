package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.dialog.DialogExtension
import im.actor.server.dialog.privat.PrivateDialogRegion
import im.actor.server.user.{ UserExtension, UserProcessor, UserProcessorRegion, UserViewRegion }
import org.scalatest.Suite

trait ImplicitUserRegions extends ImplicitSocialManagerRegion with ImplicitSeqUpdatesManagerRegion with ActorSerializerPrepare {
  this: Suite ⇒

  UserProcessor.register()

  protected implicit val system: ActorSystem

  protected implicit lazy val userProcessorRegion: UserProcessorRegion = UserExtension(system).processorRegion
  protected implicit lazy val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
  protected implicit lazy val privateDialogRegion: PrivateDialogRegion = DialogExtension(system).privateRegion

}
