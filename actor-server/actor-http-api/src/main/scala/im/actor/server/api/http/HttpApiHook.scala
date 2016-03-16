package im.actor.server.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ RejectionHandler, Route }
import im.actor.hook.{ HooksControl, SyncHook0, SyncHooksStorage0 }

import scala.concurrent.ExecutionContext

object HttpApiHook {
  abstract class RoutesHook(system: ActorSystem) extends SyncHook0[Route]
  abstract class RejectionsHook(system: ActorSystem) extends SyncHook0[RejectionHandler]
}

final class HttpApiHookControl(implicit ec: ExecutionContext) extends HooksControl {
  import HttpApiHook._

  val routesHook = new SyncHooksStorage0[RoutesHook, Route]()
  val rejectionsHook = new SyncHooksStorage0[RejectionsHook, RejectionHandler]()
}