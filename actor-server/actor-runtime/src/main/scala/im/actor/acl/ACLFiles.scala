package im.actor.acl

import akka.actor.ActorSystem

object ACLFiles extends ACLFiles

trait ACLFiles extends ACLBase {
  def fileAccessHash(fileId: Long, accessSalt: String)(implicit s: ActorSystem): Long =
    hash(s"$fileId:$accessSalt:${secretKey()}")
}
