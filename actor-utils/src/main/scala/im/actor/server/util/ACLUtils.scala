package im.actor.server.util

import java.nio.ByteBuffer
import java.security.MessageDigest

import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.actor.ActorSystem
import org.apache.commons.codec.digest.DigestUtils

import im.actor.server.models

object ACLUtils {
  def secretKey()(implicit s: ActorSystem) =
    s.settings.config.getString("secret")

  def hash(s: String): Long =
    ByteBuffer.wrap(MessageDigest.getInstance("MD5").digest(s.getBytes)).getLong

  def userAccessHash(authId: Long, userId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$authId:$userId:$accessSalt:${secretKey()}")

  def userAccessHash(authId: Long, u: models.User)(implicit s: ActorSystem): Long =
    userAccessHash(authId, u.id, u.accessSalt)

  def phoneAccessHash(authId: Long, userId: Int, phoneId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$authId:$userId:$phoneId:$accessSalt:${secretKey()}")

  def phoneAccessHash(authId: Long, p: models.UserPhone)(implicit s: ActorSystem): Long =
    phoneAccessHash(authId, p.userId, p.id, p.accessSalt)

  def emailAccessHash(authId: Long, userId: Int, emailId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$authId:$userId:$emailId:$accessSalt:${secretKey()}")

  def emailAccessHash(authId: Long, e: models.UserEmail)(implicit s: ActorSystem): Long =
    emailAccessHash(authId, e.userId, e.id, e.accessSalt)

  def fileAccessHash(fileId: Long, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$fileId:$accessSalt:${secretKey()}")

  def nextAccessSalt(rnd: ThreadLocalRandom): String = rnd.nextLong().toString

  def nextAccessSalt(): String = {
    nextAccessSalt(ThreadLocalRandom.current())
  }

  def accessToken(rng: ThreadLocalRandom): String = DigestUtils.sha256Hex(rng.nextLong().toString)
}
