package im.actor.server

import im.actor.server.api.CommonSerialization
import im.actor.server.commons.serialization.ActorSerializer
import org.scalatest.{ Suite, BeforeAndAfterAll }

trait ActorSerializerPrepare extends BeforeAndAfterAll {
  this: Suite ⇒

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    ActorSerializer.clean()
    CommonSerialization.register()
  }

}
