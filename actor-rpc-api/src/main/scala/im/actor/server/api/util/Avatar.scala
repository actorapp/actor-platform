package im.actor.server.api.util

import im.actor.api.rpc.files
import im.actor.server.models
import scala.collection.immutable

object Avatar {
  def avatar(ad: models.AvatarData) =
    (ad.smallOpt, ad.largeOpt, ad.fullOpt) match {
      case (None, None, None) ⇒ None
      case (smallOpt, largeOpt, fullOpt) ⇒
        Some(files.Avatar(
          avatarImage(smallOpt, 100, 100),
          avatarImage(largeOpt, 200, 200),
          avatarImage(fullOpt)
        ))
    }

  def avatarImage(idhashsize: Option[(Long, Long, Int)], width: Int, height: Int): Option[files.AvatarImage] =
    idhashsize map {
      case (id, hash, size) ⇒ files.AvatarImage(files.FileLocation(id, hash), width, height, size)
    }

  def avatarImage(idhashsizewh: Option[(Long, Long, Int, Int, Int)]): Option[files.AvatarImage] =
    idhashsizewh flatMap {
      case (id, hash, size, w, h) ⇒ avatarImage(Some((id, hash, size)), w, h)
    }

}
