package im.actor.server.persist

import akka.actor.ExtendedActorSystem
import akka.persistence.journal.{ EventSeq, EventAdapter }

final class ActorEventAdapter(system: ExtendedActorSystem) extends EventAdapter {
  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = {
    println("=== toJournal")
    event
  }

  override def fromJournal(event: Any, manifest: String): EventSeq =
    event match {
      case e: AnyRef ⇒ EventSeq(e)
      case _         ⇒ throw new IllegalArgumentException(s"Supported AnyRef but got: ${event.getClass}")
    }

}