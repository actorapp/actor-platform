package im.actor.server.hook

import im.actor.concurrent.FutureExt

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

trait Hook

trait Hook0[R] extends Hook {
  def run(): Future[R]
}

trait Hook1[R, A] extends Hook {
  def run(a: A): Future[R]
}

trait Hook2[R, A, B] extends Hook {
  def run(a: A, b: B): Future[R]
}

trait Hook3[R, A, B, C] extends Hook {
  def run(a: A, b: B, c: C): Future[R]
}

class HooksStorage[H <: Hook] {
  private val hooks = TrieMap.empty[String, H]
  protected def hooksList: Seq[H] = hooks.values.toSeq

  def register(name: String, hook: H): Unit =
    if (hooks.putIfAbsent(name, hook).nonEmpty)
      throw HookException.HookAlreadyRegistered(name)
}

final class HooksStorage0[H <: Hook0[R], R](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(): Future[Seq[R]] = FutureExt.ftraverse(hooksList)(_.run())
}

final class HooksStorage1[H <: Hook1[R, A], R, A](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(a: A): Future[Seq[R]] = FutureExt.ftraverse(hooksList)(_.run(a))
}

final class HooksStorage2[H <: Hook2[R, A, B], R, A, B](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(a: A, b: B): Future[Seq[R]] = FutureExt.ftraverse(hooksList)(_.run(a, b))
}

final class HooksStorage3[H <: Hook3[R, A, B, C], R, A, B, C](implicit ec: ExecutionContext) extends HooksStorage[H] {
  def runAll(a: A, b: B, c: C): Future[Seq[R]] = FutureExt.ftraverse(hooksList)(_.run(a, b, c))
}

abstract class HooksControl

abstract class HookException(msg: String) extends RuntimeException(msg)

object HookException {
  final case class HookAlreadyRegistered(name: String) extends HookException(s"Hook $name is already registered")
}
