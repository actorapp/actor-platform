package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.user.{ UserExtension, UserProcessor, UserProcessorRegion, UserViewRegion }
import org.scalatest.Suite

trait ImplicitUserRegions extends ImplicitSocialManagerRegion with ImplicitSeqUpdatesManagerRegion with ActorSerializerPrepare {
  this: Suite ⇒

  UserProcessor.register()

  protected implicit val system: ActorSystem

  protected val userProcessorRegion: UserProcessorRegion = UserExtension(system).processorRegion
  protected val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
}
