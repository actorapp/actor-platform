package im.actor.server.util.streams

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source

class SourceWatchManager[T](actorRef: ActorRef) extends Actor with ActorLogging with ActorPublisher[T] {
  import akka.stream.actor.ActorPublisherMessage._

  context.watch(actorRef)

  def receive = {
    case Request(_) =>
    case Terminated(`actorRef`)  =>
      log.error("Terminated: {}", actorRef)
      onError(new Throwable("actor terminated"))
  }
}

object SourceWatchManager {
  def apply[T](actorRef: ActorRef)(implicit system: ActorSystem): (ActorRef, Source[T, Unit]) = {
    val actor = system.actorOf(Props(new SourceWatchManager[T](actorRef)))
    (actor, Source(ActorPublisher[T](actor)))
  }
}
