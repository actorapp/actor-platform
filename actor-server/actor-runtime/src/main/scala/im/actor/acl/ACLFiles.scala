package im.actor.acl

import akka.actor.ActorSystem
import org.apache.commons.codec.digest.HmacUtils

object ACLFiles extends ACLFiles

trait ACLFiles extends ACLBase {
  def fileAccessHash(fileId: Long, accessSalt: String)(implicit s: ActorSystem): Long =
    hashObsolete(s"$fileId:$accessSalt:${secretKey()}")

  def fileUrlBuilderSeed()(implicit s: ActorSystem): Long =
    hash(s"${randomString()}:${secretKey()}")

  def fileUrlBuilderSecret(seed: Array[Byte])(implicit s: ActorSystem): Array[Byte] = {
    HmacUtils.hmacSha256(secretKey().getBytes, seed)
  }

}
