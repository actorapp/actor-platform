package im.actor.hook

import im.actor.concurrent.FutureExt
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future }

trait Hook

trait SyncHook0[R] extends Hook {
  def run(): R
}

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

  private val log = LoggerFactory.getLogger(this.getClass)

  def register(name: String, hook: H): Unit = {
    val inserted = hooks.putIfAbsent(name, hook).isEmpty
    if (inserted) {
      log.debug("Registered hook {}", name)
    } else {
      log.warn("Hook {} is already registered", name)

      throw HookException.HookAlreadyRegistered(name)
    }
  }
}

final class SyncHooksStorage0[H <: SyncHook0[R], R] extends HooksStorage[H] {
  def runAll(): Seq[R] = hooksList map (_.run())
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
