package im.actor.acl

import java.nio.ByteBuffer
import java.security.MessageDigest

import akka.actor.ActorSystem

import scala.concurrent.forkjoin.ThreadLocalRandom

trait ACLBase {

  def getMDInstance() = MessageDigest.getInstance("MD5")

  def secretKey()(implicit s: ActorSystem) =
    s.settings.config.getString("secret")

  def hash(s: String, md: MessageDigest = getMDInstance()): Long =
    ByteBuffer.wrap(md.digest(s.getBytes)).getLong

  def randomLong(): Long = randomLong(ThreadLocalRandom.current())

  def randomLong(rng: ThreadLocalRandom): Long = rng.nextLong()

  def randomString(): String = randomString(ThreadLocalRandom.current())

  def randomString(rng: ThreadLocalRandom): String = rng.nextLong().toString

  def nextAccessSalt(rng: ThreadLocalRandom): String = randomString(rng)

  def nextAccessSalt(): String = {
    nextAccessSalt(ThreadLocalRandom.current())
  }

}
