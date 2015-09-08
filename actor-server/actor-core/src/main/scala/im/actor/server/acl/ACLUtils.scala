package im.actor.server.acl

import java.nio.ByteBuffer
import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType, ApiOutPeer }
import im.actor.server.group.{ GroupExtension, GroupViewRegion, GroupOffice }
import im.actor.server.models
import im.actor.server.user.{ UserExtension, UserViewRegion, UserOffice }
import org.apache.commons.codec.digest.DigestUtils

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.duration._

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

  def authTransactionHash(accessSalt: String)(implicit s: ActorSystem): String =
    DigestUtils.sha1Hex(s"$accessSalt:${secretKey()}")

  def nextAccessSalt(rng: ThreadLocalRandom): String = rng.nextLong().toString

  def nextAccessSalt(): String = {
    nextAccessSalt(ThreadLocalRandom.current())
  }

  def accessToken(rng: ThreadLocalRandom): String = DigestUtils.sha256Hex(rng.nextLong().toString)

  def checkOutPeer(outPeer: ApiOutPeer, clientAuthId: Long)(implicit s: ActorSystem): Future[Boolean] = {
    implicit val ec: ExecutionContext = s.dispatcher
    implicit val timeout: Timeout = Timeout(20.seconds)

    outPeer.`type` match {
      case ApiPeerType.Group ⇒
        implicit val groupViewRegion: GroupViewRegion = GroupExtension(s).viewRegion
        GroupOffice.checkAccessHash(outPeer.id, outPeer.accessHash)
      case ApiPeerType.Private ⇒
        implicit val userViewRegion: UserViewRegion = UserExtension(s).viewRegion
        UserOffice.checkAccessHash(outPeer.id, clientAuthId, outPeer.accessHash)
    }
  }

  def getOutPeer(peer: ApiPeer, clientAuthId: Long)(implicit s: ActorSystem): Future[ApiOutPeer] = {
    implicit val ec: ExecutionContext = s.dispatcher
    implicit val timeout: Timeout = Timeout(20.seconds)

    peer.`type` match {
      case ApiPeerType.Group ⇒
        implicit val groupViewRegion: GroupViewRegion = GroupExtension(s).viewRegion
        GroupOffice.getAccessHash(peer.id) map (ApiOutPeer(ApiPeerType.Group, peer.id, _))
      case ApiPeerType.Private ⇒
        implicit val userViewRegion: UserViewRegion = UserExtension(s).viewRegion
        UserOffice.getAccessHash(peer.id, clientAuthId) map (ApiOutPeer(ApiPeerType.Private, peer.id, _))
    }
  }
}
