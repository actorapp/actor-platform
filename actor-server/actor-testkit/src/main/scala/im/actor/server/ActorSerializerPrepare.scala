package im.actor.server

import im.actor.serialization.ActorSerializer
import im.actor.server.group.GroupProcessor
import im.actor.server.user.UserProcessor
import org.scalatest.Suite

trait ActorSerializerPrepare {
  this: Suite ⇒

  ActorSerializer.clean()
  CommonSerialization.register()
  GroupProcessor.register()
  UserProcessor.register()

}
