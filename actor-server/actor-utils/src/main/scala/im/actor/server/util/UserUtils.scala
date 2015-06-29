package im.actor.server.util

import scala.concurrent._
import scala.language.postfixOps

import akka.actor.ActorSystem
import slick.dbio.Effect.Read
import slick.dbio.{ Effect, DBIO, DBIOAction, NoStream }
import slick.driver.PostgresDriver.api._
import slick.profile.SqlAction

import im.actor.api.rpc._
import im.actor.api.rpc.users._
import im.actor.server.models.UserPhone
import im.actor.server.{ models, persist }

object UserUtils {
  def userContactRecords(phones: Vector[models.UserPhone], emails: Vector[models.UserEmail]): Vector[ContactRecord] = {
    val phoneRecords = phones map { phone ⇒
      ContactRecord(ContactType.Phone, stringValue = None, longValue = Some(phone.number), title = Some(phone.title), subtitle = None)
    }

    val emailRecords = emails map { email ⇒
      ContactRecord(ContactType.Email, stringValue = Some(email.email), longValue = None, title = Some(email.title), subtitle = None)
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
        avatar = adOpt flatMap (ImageUtils.avatar),
        phone = userPhone(u, phones),
        isBot = Some(u.isBot),
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
        avatar = adOpt flatMap (ImageUtils.avatar),
        phone = userPhone(u, phones),
        isBot = Some(u.isBot),
        contactInfo = userContactRecords(phones.toVector, emails.toVector)
      )
    }

  def userPhone(u: models.User, phones: Seq[UserPhone]): Option[Long] = {
    phones.headOption match {
      case Some(phone) ⇒ Some(phone.number)
      case None        ⇒ Some(0L)
    }
  }

  def getUserStructOpt(userId: Int, senderUserId: Int, senderAuthId: Long)(implicit ec: ExecutionContext, s: ActorSystem): DBIOAction[Option[User], NoStream, Read with Read with Read with Read with Read with Read] =
    persist.User.find(userId).headOption flatMap {
      case Some(userModel) ⇒ userStruct(userModel, senderUserId, senderAuthId) map (Some(_))
      case None            ⇒ DBIO.successful(None)
    }

  def getUserStructs(userIds: Set[Int], senderUserId: Int, senderAuthId: Long)(implicit ec: ExecutionContext, s: ActorSystem): DBIOAction[Seq[User], NoStream, Read with Read with Read with Read with Read with Read] = {
    DBIO.sequence(userIds.toSeq map (getUserStructOpt(_, senderUserId, senderAuthId))) map (_.flatten)
  }

  def getUserStructs(userIds: Set[Int])(implicit client: AuthorizedClientData, ec: ExecutionContext, s: ActorSystem): DBIOAction[Seq[User], NoStream, Read with Read with Read with Read with Read with Read] =
    getUserStructs(userIds, client.userId, client.authId)

  def getUserStructsPar(userIds: Set[Int], senderUserId: Int, senderAuthId: Long)(implicit ec: ExecutionContext, s: ActorSystem, db: Database): DBIOAction[Seq[User], NoStream, Effect] = {
    DBIO.sequence(userIds.toSeq map (userId ⇒ DBIO.from(db.run(getUserStructOpt(userId, senderUserId, senderAuthId))))) map (_.flatten)
  }

  def getUserStructsPar(userIds: Set[Int])(implicit client: AuthorizedClientData, ec: ExecutionContext, s: ActorSystem, db: Database): DBIOAction[Seq[User], NoStream, Effect] =
    getUserStructsPar(userIds, client.userId, client.authId)

  def getUser(userId: Int) = {
    persist.User.find(userId).headOption
  }

  def getUserUnsafe(userId: Int)(implicit ec: ExecutionContext) = {
    getUser(userId) map {
      case Some(user) ⇒ user
      case None       ⇒ throw new Exception(s"User ${userId} not found")
    }
  }

  def getClientUser(implicit client: AuthorizedClientData): SqlAction[Option[models.User], NoStream, Read] = {
    getUser(client.userId)
  }

  def getClientUserUnsafe(implicit client: AuthorizedClientData, ec: ExecutionContext): DBIOAction[models.User, NoStream, Read] = {
    getUserUnsafe(client.userId)
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
}
