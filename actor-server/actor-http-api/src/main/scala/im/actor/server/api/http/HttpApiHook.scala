package im.actor.server.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import im.actor.hook.{ HooksStorage0, HooksControl, Hook0 }

import scala.concurrent.ExecutionContext

object HttpApiHook {
  abstract class RoutesHook(system: ActorSystem) extends Hook0[Route]
}

final class HttpApiHookControl(implicit ec: ExecutionContext) extends HooksControl {
  import HttpApiHook._

  val routesHook = new HooksStorage0[RoutesHook, Route]()
}