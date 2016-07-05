package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.sequence.SeqUpdatesExtension

trait ImplicitSeqUpdatesManagerRegion {
  protected implicit val system: ActorSystem

  protected val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
}
