package im.actor.server.util

import scala.concurrent._
import scala.language.postfixOps

import akka.actor.ActorSystem
import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }
import slick.profile.SqlAction

import im.actor.api.rpc._
import im.actor.api.rpc.users._
import im.actor.server.{ models, persist }

object UserUtils {
  def userContactRecords(phones: Vector[models.UserPhone], emails: Vector[models.UserEmail]): Vector[ContactRecord] = {
    val phoneRecords = phones map { phone ⇒
      ContactRecord(ContactType.Phone, stringValue = None, longValue = Some(phone.number), title = Some(phone.title), subtitle = None)
    }

    val emailRecords = emails map { email ⇒
      ContactRecord(ContactType.Email, stringValue = Some(email.title), longValue = None, title = Some(email.title), subtitle = None)
    }

    phoneRecords ++ emailRecords
  }

  def userStruct(u: models.User, localName: Option[String], senderAuthId: Long)(
    implicit
    ec: ExecutionContext,
    s:  ActorSystem
  ): DBIOAction[User, NoStream, Read with Read with Read with Read] =
    for {
      phones ← persist.UserPhone.findByUserId(u.id) map (_.toVector)
      emails ← persist.UserEmail.findByUserId(u.id)
      adOpt ← persist.AvatarData.findByUserId(u.id).headOption
    } yield {
      users.User(
        id = u.id,
        accessHash = ACLUtils.userAccessHash(senderAuthId, u),
        name = u.name,
        localName = normalizeLocalName(localName),
        sex = u.sex.toOption map (sex ⇒ users.Sex.apply(sex.toInt)),
        avatar = adOpt flatMap (AvatarUtils.avatar),
        phone = phones.headOption map (_.number),
        phones = phones.map(_.id),
        emails = Vector.empty,
        isBot = None,
        contactInfo = userContactRecords(phones.toVector, emails.toVector)
      )
    }

  def userStruct(u: models.User, senderUserId: Int, senderAuthId: Long)(
    implicit
    ec: ExecutionContext,
    s:  ActorSystem
  ): DBIOAction[User, NoStream, Read with Read with Read with Read with Read] =
    for {
      localName ← persist.contact.UserContact.findName(senderUserId: Int, u.id).headOption map (_.getOrElse(None))
      keyHashes ← persist.UserPublicKey.findKeyHashes(u.id)
      phones ← persist.UserPhone.findByUserId(u.id) map (_.toVector)
      emails ← persist.UserEmail.findByUserId(u.id)
      adOpt ← persist.AvatarData.findByUserId(u.id).headOption
    } yield {
      users.User(
        id = u.id,
        accessHash = ACLUtils.userAccessHash(senderAuthId, u),
        name = u.name,
        localName = normalizeLocalName(localName),
        sex = u.sex.toOption map (sex ⇒ users.Sex.apply(sex.toInt)),
        avatar = adOpt flatMap (AvatarUtils.avatar),
        phone = phones.headOption map (_.number),
        phones = phones map (_.id),
        emails = Vector.empty,
        isBot = None,
        contactInfo = userContactRecords(phones.toVector, emails.toVector)
      )
    }

  def userStructOption(userId: Int, senderUserId: Int, senderAuthId: Long)(implicit ec: ExecutionContext, s: ActorSystem): DBIOAction[Option[User], NoStream, Read with Read with Read with Read with Read with Read] =
    persist.User.find(userId).headOption flatMap {
      case Some(userModel) ⇒ userStruct(userModel, senderUserId, senderAuthId) map (Some(_))
      case None            ⇒ DBIO.successful(None)
    }

  // TODO: #perf lots of sql queries
  def userStructs(userIds: Set[Int], senderUserId: Int, senderAuthId: Long)(implicit ec: ExecutionContext, s: ActorSystem): DBIOAction[Seq[User], NoStream, Read with Read with Read with Read with Read with Read] = {
    DBIO.sequence(userIds.toSeq map (userStructOption(_, senderUserId, senderAuthId))) map (_.flatten)
  }

  def userStructs(userIds: Set[Int])(implicit client: AuthorizedClientData, ec: ExecutionContext, s: ActorSystem): DBIOAction[Seq[User], NoStream, Read with Read with Read with Read with Read with Read] =
    userStructs(userIds, client.userId, client.authId)

  def getClientUser(implicit client: AuthorizedClientData): SqlAction[Option[models.User], NoStream, Read] = {
    persist.User.find(client.userId).headOption
  }

  def getClientUserUnsafe(implicit client: AuthorizedClientData, ec: ExecutionContext): DBIOAction[models.User, NoStream, Read] = {
    getClientUser map {
      case Some(user) ⇒ user
      case None       ⇒ throw new Exception("Client user not found")
    }
  }

  def getClientUserPhone(implicit client: AuthorizedClientData, ec: ExecutionContext): DBIOAction[Option[(models.User, models.UserPhone)], NoStream, Read with Read] = {
    getClientUser.flatMap {
      case Some(user) ⇒
        persist.UserPhone.findByUserId(client.userId).headOption map {
          case Some(userPhone) ⇒ Some((user, userPhone))
          case None            ⇒ None
        }
      case None ⇒ DBIO.successful(None)
    }
  }

  def getClientUserPhoneUnsafe(implicit client: AuthorizedClientData, ec: ExecutionContext): DBIOAction[(models.User, models.UserPhone), NoStream, Read with Read] = {
    getClientUserPhone map {
      case Some(user_phone) ⇒ user_phone
      case None             ⇒ throw new Exception("Client user phone not found")
    }
  }

  def normalizeLocalName(name: Option[String]) = name match {
    case n @ Some(name) if name.nonEmpty ⇒ n
    case _                               ⇒ None
  }

  // for compatibility with clients before 2.0 api version
  def userPhonesEmails(user: User): (Vector[Phone], Vector[Email]) = {
    user.contactInfo.foldLeft(Vector.empty[Phone], Vector.empty[Email]) {
      case ((phones, emails), cr) ⇒
        cr.`type` match {
          case ContactType.Email ⇒
            (phones, emails :+ Email(user.id, 0L, cr.stringValue.getOrElse(""), cr.title.getOrElse("")))
          case ContactType.Phone ⇒
            (phones :+ Phone(user.id, 0L, cr.longValue.getOrElse(0L), cr.title.getOrElse("")), emails)
        }

    }
  }
}
