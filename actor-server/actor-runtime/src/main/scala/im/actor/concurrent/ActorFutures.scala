package im.actor.concurrent

import akka.actor._
import akka.pattern.pipe

import scala.concurrent.Future
import scala.reflect.ClassTag

trait ActorFutures extends Stash with ActorLogging { this: Actor ⇒
  import context._

  protected final def waitForFuture[T: ClassTag](f: Future[T])(cb: T ⇒ Unit): Unit =
    waitForFuture(f, sender())(cb)

  protected final def waitForFuture[T: ClassTag](f: Future[T], replyTo: ActorRef)(cb: T ⇒ Unit): Unit = {
    f pipeTo self

    val r: Receive = {
      case result: T ⇒
        context.unbecome()
        unstashAll()
        cb(result)
    }

    context.become(r.orElse(futureWaitingBehavior(replyTo)), discardOld = false)
  }

  protected final def futureWaitingBehavior(replyTo: ActorRef): Receive = {
    case f @ Status.Failure(e) ⇒
      log.warning("Failure {}", e)
      replyTo ! f

      unstashAll()
      context.unbecome()
    case msg ⇒
      log.warning("Stashing while waiting for future {}", msg)
      stash()
  }
}