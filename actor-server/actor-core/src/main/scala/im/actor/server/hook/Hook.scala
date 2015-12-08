package im.actor.server.hook

import im.actor.concurrent.FutureExt

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

trait Hook

trait Hook0 extends Hook {
  def run(): Future[Unit]
}

trait Hook1[A] extends Hook {
  def run(a: A): Future[Unit]
}

trait Hook2[A, B] extends Hook {
  def run(a: A, b: B): Future[Unit]
}

trait Hook3[A, B, C] extends Hook {
  def run(a: A, b: B, c: C): Future[Unit]
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

final class HooksStorage1[H <: Hook1[A], A](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(a: A): Future[Unit] = FutureExt.ftraverse(hooksList)(_.run(a)) map (_ ⇒ ())
}

final class HooksStorage2[H <: Hook2[A, B], A, B](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(a: A, b: B): Future[Unit] = FutureExt.ftraverse(hooksList)(_.run(a, b)) map (_ ⇒ ())
}

final class HooksStorage3[H <: Hook3[A, B, C], A, B, C](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(a: A, b: B, c: C): Future[Unit] = FutureExt.ftraverse(hooksList)(_.run(a, b, c)) map (_ ⇒ ())
}

abstract class HooksControl

abstract class HookException(msg: String) extends RuntimeException(msg)

object HookException {
  final case class HookAlreadyRegistered(name: String) extends HookException(s"Hook $name is already registered")
}
