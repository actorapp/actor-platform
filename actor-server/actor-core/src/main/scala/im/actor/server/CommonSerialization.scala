package im.actor.server

import im.actor.serialization.ActorSerializer

object CommonSerialization {
  def register(): Unit = {
    ActorSerializer.register(100, classOf[im.actor.server.event.TSEvent])
  }
}
