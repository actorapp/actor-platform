package im.actor.server

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  Await.result(ActorServer.newBuilder.start().system.whenTerminated, Duration.Inf)
}