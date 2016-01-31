package im.actor.server.cqrs

import akka.actor.ExtendedActorSystem
import akka.persistence.journal.{ Tagged, EventSeq, EventAdapter }

final class ActorEventAdapter(system: ExtendedActorSystem) extends EventAdapter {
  override def manifest(event: Any): String = "V1"

  override def toJournal(event: Any): Any = {
    event match {
      case e: TaggedEvent ⇒ Tagged(e, e.tags)
      case _              ⇒ event
    }
  }

  override def fromJournal(event: Any, manifest: String): EventSeq =
    event match {
      case e: AnyRef ⇒ EventSeq(e)
      case _         ⇒ throw new IllegalArgumentException(s"Supported AnyRef but got: ${event.getClass}")
    }

}