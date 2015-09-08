package im.actor.server

import im.actor.serialization.ActorSerializer
import org.scalatest.Suite

trait ActorSerializerPrepare {
  this: Suite ⇒

  ActorSerializer.clean()
  CommonSerialization.register()

}
