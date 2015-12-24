package im.actor.server.acl

import akka.actor.ActorSystem
import im.actor.acl.{ ACLBase, ACLFiles }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.server.group.GroupExtension
import im.actor.server.model
import im.actor.server.user.UserExtension
import org.apache.commons.codec.digest.DigestUtils

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

object ACLUtils extends ACLBase with ACLFiles {

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
}
