package im.actor.server.hook

import im.actor.concurrent.FutureExt

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

trait Hook

trait Hook0 extends Hook {
  def run(): Future[Unit]
}

trait Hook1[P] extends Hook {
  def run(p: P): Future[Unit]
}

class HooksStorage[H <: Hook] {
  private val hooks = TrieMap.empty[String, H]
  protected def hooksList: Seq[H] = hooks.values.toSeq

  def register(name: String, hook: H): Unit =
    if (hooks.putIfAbsent(name, hook).nonEmpty)
      throw HookException.HookAlreadyRegistered(name)
}

final class HooksStorage0[H <: Hook0](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(): Future[Unit] = FutureExt.ftraverse(hooksList)(_.run()) map (_ ⇒ ())
}

final class HooksStorage1[H <: Hook1[P], P](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(p: P): Future[Unit] = FutureExt.ftraverse(hooksList)(_.run(p)) map (_ ⇒ ())
}

abstract class HooksControl {

}

abstract class HookException(msg: String) extends RuntimeException(msg)

object HookException {
  final case class HookAlreadyRegistered(name: String) extends HookException(s"Hook $name is already registered")
}
