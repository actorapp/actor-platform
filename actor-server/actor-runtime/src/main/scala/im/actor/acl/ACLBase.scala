package im.actor.acl

import java.nio.ByteBuffer
import java.security.MessageDigest

import akka.actor.ActorSystem
import im.actor.util.ThreadLocalSecureRandom

trait ACLBase {

  def getMDInstance() = MessageDigest.getInstance("MD5")

  def secretKey()(implicit s: ActorSystem) =
    s.settings.config.getString("secret")

  def hash(s: String, md: MessageDigest = getMDInstance()): Long =
    ByteBuffer.wrap(md.digest(s.getBytes)).getLong

  def randomLong(): Long = randomLong(ThreadLocalSecureRandom.current())

  def randomLong(rng: ThreadLocalSecureRandom): Long = rng.nextLong()

  def randomString(): String = randomString(ThreadLocalSecureRandom.current())

  def randomString(rng: ThreadLocalSecureRandom): String = rng.nextLong().toString

  def nextAccessSalt(rng: ThreadLocalSecureRandom): String = randomString(rng)

  def nextAccessSalt(): String = {
    nextAccessSalt(ThreadLocalSecureRandom.current())
  }

}
