package im.actor.server

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val system = ActorServer.newBuilder.start().system
  sys.addShutdownHook(system.terminate())
  Await.result(system.whenTerminated, Duration.Inf)
}
