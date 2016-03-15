package im.actor.concurrent

import java.security.MessageDigest

object ThreadLocalSHA256 {
  private val local: ThreadLocal[MessageDigest] = new ThreadLocal[MessageDigest]() {
    override protected def initialValue(): MessageDigest =
      MessageDigest.getInstance("SHA-256")
  }

  def current(): MessageDigest = local.get()
}