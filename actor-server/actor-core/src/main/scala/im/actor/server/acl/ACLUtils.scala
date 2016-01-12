package im.actor.server.acl

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import akka.actor.ActorSystem
import com.google.protobuf.ByteString
import im.actor.acl.{ ACLBase, ACLFiles }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.server.group.GroupExtension
import im.actor.server.model
import im.actor.server.model.UserPassword
import im.actor.server.persist.UserPasswordRepo
import im.actor.server.user.UserExtension
import org.apache.commons.codec.digest.DigestUtils
import slick.dbio.DBIO

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

object ACLUtils extends ACLBase with ACLFiles {
  val PasswordMinLength = 8
  val PasswordMaxLength = 160

  type Hash = Array[Byte]
  type Salt = Array[Byte]

  def userAccessHash(authId: Long, userId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$authId:$userId:$accessSalt:${secretKey()}")

  def userAccessHash(authId: Long, u: model.User)(implicit s: ActorSystem): Long =
    userAccessHash(authId, u.id, u.accessSalt)

  def phoneAccessHash(authId: Long, userId: Int, phoneId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$authId:$userId:$phoneId:$accessSalt:${secretKey()}")

  def phoneAccessHash(authId: Long, p: model.UserPhone)(implicit s: ActorSystem): Long =
    phoneAccessHash(authId, p.userId, p.id, p.accessSalt)

  def emailAccessHash(authId: Long, userId: Int, emailId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$authId:$userId:$emailId:$accessSalt:${secretKey()}")

  def emailAccessHash(authId: Long, e: model.UserEmail)(implicit s: ActorSystem): Long =
    emailAccessHash(authId, e.userId, e.id, e.accessSalt)

  def stickerPackAccessHash(id: Int, ownerUserId: Int, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$id:$ownerUserId:$accessSalt:${secretKey()}")

  def stickerPackAccessHash(pack: model.StickerPack)(implicit s: ActorSystem): Long =
    stickerPackAccessHash(pack.id, pack.ownerUserId, pack.accessSalt)

  def authTransactionHash(accessSalt: String)(implicit s: ActorSystem): String =
    DigestUtils.sha1Hex(s"$accessSalt:${secretKey()}")

  def randomHash()(implicit s: ActorSystem): String =
    DigestUtils.sha1Hex(s"${randomString()}:${secretKey()}")

  def accessToken(): String = accessToken(ThreadLocalRandom.current())

  def accessToken(rng: ThreadLocalRandom): String = DigestUtils.sha256Hex(rng.nextLong().toString)

  def checkOutPeer(outPeer: ApiOutPeer, clientAuthId: Long)(implicit s: ActorSystem): Future[Boolean] = {
    outPeer.`type` match {
      case ApiPeerType.Group ⇒
        GroupExtension(s).checkAccessHash(outPeer.id, outPeer.accessHash)
      case ApiPeerType.Private ⇒
        UserExtension(s).checkAccessHash(outPeer.id, clientAuthId, outPeer.accessHash)
    }
  }

  def getOutPeer(peer: ApiPeer, clientAuthId: Long)(implicit s: ActorSystem): Future[ApiOutPeer] = {
    implicit val ec: ExecutionContext = s.dispatcher
    peer.`type` match {
      case ApiPeerType.Group ⇒
        GroupExtension(s).getAccessHash(peer.id) map (ApiOutPeer(ApiPeerType.Group, peer.id, _))
      case ApiPeerType.Private ⇒
        UserExtension(s).getAccessHash(peer.id, clientAuthId) map (ApiOutPeer(ApiPeerType.Private, peer.id, _))
    }
  }

  def getUserOutPeer(userId: Int, clientAuthId: Long)(implicit s: ActorSystem): Future[ApiUserOutPeer] = {
    import s.dispatcher
    UserExtension(s).getAccessHash(userId, clientAuthId) map (ApiUserOutPeer(userId, _))
  }

  def isPasswordValid(password: String) = password.length > PasswordMinLength && password.length < PasswordMaxLength

  /**
   * Generates password salt and hash
   *
   * @param password
   * @return (hash, salt)
   */
  def hashPassword(password: String): (Hash, Salt) = {
    val seedBytes = 20

    val random = new SecureRandom()
    val salt = random.generateSeed(seedBytes)

    (hashPassword(password, salt), salt)
  }

  def hashPassword(password: String, salt: Array[Byte]): Hash = {
    val hashBytes = 20
    val iterations = 1000

    val spec = new PBEKeySpec(password.toCharArray, salt, iterations, hashBytes * 8)
    val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    skf.generateSecret(spec).getEncoded
  }

  def checkPassword(userId: Int, password: String)(implicit ec: ExecutionContext): DBIO[Boolean] =
    UserPasswordRepo.find(userId) map {
      case Some(UserPassword(_, hash, salt)) ⇒
        ByteString.copyFrom(hashPassword(password, salt.toByteArray)) == hash
      case None ⇒ false
    }
}
