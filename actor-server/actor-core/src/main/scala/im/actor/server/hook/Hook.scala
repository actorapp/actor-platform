package im.actor.server.hook

import im.actor.concurrent.FutureExt

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

trait Hook

class HooksStorage[A <: Hook] {
  private val hooks = TrieMap.empty[String, A]
  protected def hooksList = hooks.values.toSeq

  def register(name: String, hook: A): Unit =
    if (hooks.putIfAbsent(name, hook).nonEmpty)
      throw HookException.HookAlreadyRegistered(name)
}

class HooksStorage0[A <: Hook](run: A ⇒ Future[Unit])(implicit ec: ExecutionContext) extends HooksStorage {
  def runAll(): Future[Unit] =
    FutureExt.ftraverse(hooksList)(run) map (_ ⇒ ())
}

abstract class HooksControl {

}

abstract class HookException(msg: String) extends RuntimeException(msg)

object HookException {
  final case class HookAlreadyRegistered(name: String) extends HookException(s"Hook $name is already registered")
}
