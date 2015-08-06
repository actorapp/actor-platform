package im.actor.server

import akka.actor.ActorSystem

import im.actor.server.user.{ UserExtension, UserProcessor, UserProcessorRegion, UserViewRegion }

trait ImplicitUserRegions extends ImplicitSocialManagerRegion with ImplicitSeqUpdatesManagerRegion {
  protected implicit val system: ActorSystem

  protected implicit lazy val userProcessorRegion: UserProcessorRegion = UserExtension(system).processorRegion
  protected implicit lazy val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
  UserProcessor.register()
}
