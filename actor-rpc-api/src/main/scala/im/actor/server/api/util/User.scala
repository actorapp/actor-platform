package im.actor.server.api.util

import akka.actor.ActorSystem

import im.actor.api.rpc._
import im.actor.server.models
import im.actor.server.persist

import scala.concurrent._
import scala.language.postfixOps

object User {
  def struct(u: models.User, localName: Option[String], senderAuthId: Long)(
    implicit
      ec: ExecutionContext,
      s: ActorSystem
  ) =
    for {
      keyHashes <- persist.UserPublicKey.findKeyHashes(u.id)
      phones <- persist.UserPhone.findByUserId(u.id)
      emails <- persist.UserPhone.findByUserId(u.id)
      adOpt <- persist.AvatarData.findByUserId(u.id).headOption
    } yield {
      users.User(
        id = u.id,
        accessHash = ACL.userAccessHash(senderAuthId, u),
        name = u.name,
        localName = normalizeLocalName(localName),
        sex = u.sex.toOption map (sex => users.Sex.apply(sex.toInt)),
        keyHashes = keyHashes.toVector,
        phone = phones.head.number,
        phones = phones map (_.id) toVector,
        emails = emails map (_.id) toVector,
        userState = users.UserState.apply(u.state.toInt),
        avatar = adOpt flatMap (Avatar.avatar)
      )
    }

  def normalizeLocalName(name: Option[String]) = name match {
    case n @ Some(name) if name.nonEmpty => n
    case _ => None
  }
}
