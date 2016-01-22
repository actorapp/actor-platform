package im.actor.server.session

import akka.actor._

import scala.concurrent.duration.Duration

object IdleControl {
  case object KeepAlive

  def apply(timeout: Duration)(implicit factory: ActorRefFactory) =
    new IdleControl(factory.actorOf(props(timeout), "idle-control"))

  private def props(timeout: Duration) = Props(classOf[IdleControlActor], timeout)
}

final case class IdleControl(val ref: ActorRef) {
  def keepAlive(): Unit = ref ! IdleControl.KeepAlive
}

private final class IdleControlActor(timeout: Duration) extends Actor {
  import IdleControl._

  context.setReceiveTimeout(timeout)

  def receive = {
    case KeepAlive ⇒
    case ReceiveTimeout ⇒
      context.parent ! ReceiveTimeout
  }
}