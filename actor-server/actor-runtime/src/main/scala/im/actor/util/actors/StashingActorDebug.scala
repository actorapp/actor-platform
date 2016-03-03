package im.actor.util.actors

import java.time.Instant
import java.time.format.DateTimeFormatter

import akka.actor.{ ActorLogging, Stash, ActorRef, Actor }

trait StashingActorDebug extends Actor with Stash with ActorLogging {

  private val formatter = DateTimeFormatter.ISO_INSTANT

  protected def debugMessage(stage: String): String = {
    val date = Instant.now
    s"In stashing. Stage: ${stage.capitalize}, since ${formatter.format(date)}"
  }

  protected def becomeStashing(f: ActorRef ⇒ Receive, debugMessage: String, discardOld: Boolean = false): Unit =
    context.become(receiveStashing(f, debugMessage), discardOld = discardOld)

  protected def receiveStashing(f: ActorRef ⇒ Receive, debugMessage: String): Receive =
    f(sender()) orElse stashing(debugMessage)

  private def stashing(debugMessage: String): Receive = {
    case msg ⇒
      log.debug(debugMessage + "; stashing message: {}", msg)
      stash()
  }
}