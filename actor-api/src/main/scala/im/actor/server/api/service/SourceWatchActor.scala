package im.actor.server.api.service

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source

class SourceWatchActor[T](actorRef: ActorRef) extends Actor with ActorLogging with ActorPublisher[T] {
  import akka.stream.actor.ActorPublisherMessage._

  context.watch(actorRef)

  def receive = {
    case Request(_) =>
    case Terminated(`actorRef`)  =>
      log.error(s"Terminated: {}", actorRef)
      onError(new Throwable("actor terminated"))
  }
}

object SourceWatchActor {
  def apply[T](actorRef: ActorRef)(implicit system: ActorSystem): (ActorRef, Source[T]) = {
    val actor = system.actorOf(Props(new SourceWatchActor[T](actorRef)))
    (actor, Source(ActorPublisher[T](actor)))
  }
}
