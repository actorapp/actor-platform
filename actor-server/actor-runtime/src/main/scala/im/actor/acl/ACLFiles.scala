package im.actor.acl

import akka.actor.ActorSystem

object ACLFiles extends ACLFiles

trait ACLFiles extends ACLBase {
  def fileAccessHash(fileId: Long, accessSalt: String)(implicit s: ActorSystem): Long =
    hashObsolete(s"$fileId:$accessSalt:${secretKey()}")

  def fileUrlBuilderSeed()(implicit s: ActorSystem): Long =
    hash(s"${randomString()}:${secretKey()}")

  def fileUrlBuilderSecret(seed: String, expire: Int)(implicit s: ActorSystem): Long =
    hash(s"$seed:$expire:${secretKey()}")

}
