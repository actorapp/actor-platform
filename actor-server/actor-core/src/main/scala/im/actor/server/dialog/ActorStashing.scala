package im.actor.server.dialog

import akka.actor.{ Stash, ActorRef, Actor }

trait ActorStashing {
  self: Actor with Stash ⇒

  private def becomeStashing(f: ActorRef ⇒ Receive): Unit =
    context.become(f(sender()) orElse stashing, discardOld = false)

  protected def receiveStashing(f: ActorRef ⇒ Receive): Receive =
    f(sender()) orElse stashing

  private def stashing: Receive = {
    case msg ⇒ stash()
  }
}
