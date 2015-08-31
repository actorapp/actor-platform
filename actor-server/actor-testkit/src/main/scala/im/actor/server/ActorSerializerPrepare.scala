package im.actor.server

import im.actor.server.api.CommonSerialization
import im.actor.server.commons.serialization.ActorSerializer
import org.scalatest.Suite;

trait ActorSerializerPrepare {
  this: Suite ⇒

  ActorSerializer.clean()
  CommonSerialization.register()

}
