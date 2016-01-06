package im.actor.server.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import im.actor.hook.{ SyncHook0, SyncHooksStorage0, HooksControl }

import scala.concurrent.ExecutionContext

object HttpApiHook {
  abstract class RoutesHook(system: ActorSystem) extends SyncHook0[Route]
}

final class HttpApiHookControl(implicit ec: ExecutionContext) extends HooksControl {
  import HttpApiHook._

  val routesHook = new SyncHooksStorage0[RoutesHook, Route]()
}