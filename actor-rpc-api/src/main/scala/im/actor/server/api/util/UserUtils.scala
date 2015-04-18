package im.actor.server.api.util

import scala.concurrent._
import scala.language.postfixOps

import akka.actor.ActorSystem
import slick.dbio.Effect.Read
import slick.dbio.{ Effect, DBIO, DBIOAction, NoStream }

import im.actor.api.rpc._
import im.actor.api.rpc.users.User
import im.actor.server.{ models, persist }

object UserUtils {
  def userStruct(u: models.User, localName: Option[String], senderAuthId: Long)
                (implicit
                 ec: ExecutionContext,
                 s: ActorSystem): DBIOAction[User, NoStream, Read with Read with Read with Read] =
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

  def userStruct(u: models.User, senderUserId: Int, senderAuthId: Long)
                (implicit
                 ec: ExecutionContext,
                 s: ActorSystem): DBIOAction[User, NoStream, Read with Read with Read with Read with Read] =
    for {
      localName <- persist.contact.UserContact.findName(senderUserId: Int, u.id).headOption map (_.getOrElse(None))
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

  def userStructOption(userId: Int, senderUserId: Int, senderAuthId: Long)
                   (implicit ec: ExecutionContext, s: ActorSystem): DBIOAction[Option[User], NoStream, Read with Read with Read with Read with Read with Read] =
    persist.User.find(userId).headOption flatMap {
      case Some(userModel) => userStruct(userModel, senderUserId, senderAuthId) map (Some(_))
      case None =>  DBIO.successful(None)
    }

  // TODO: #perf lots of sql queries
  def userStructs(userIds: Set[Int], senderUserId: Int, senderAuthId: Long)
                 (implicit ec: ExecutionContext, s: ActorSystem): DBIOAction[Seq[User], NoStream, Read with Read with Read with Read with Read with Read] = {
    DBIO.sequence(userIds.toSeq map (userStructOption(_, senderUserId, senderAuthId))) map (_.flatten)
  }

  def normalizeLocalName(name: Option[String]) = name match {
    case n @ Some(name) if name.nonEmpty => n
    case _ => None
  }
}
