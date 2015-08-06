package im.actor.server

import akka.actor.ActorSystem

import im.actor.server.push.SeqUpdatesExtension

trait ImplicitSeqUpdatesManagerRegion {
  protected implicit val system: ActorSystem

  protected implicit lazy val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
}
