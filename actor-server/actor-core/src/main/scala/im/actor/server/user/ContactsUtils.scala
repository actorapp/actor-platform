package im.actor.server.user

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.server.{ KeyValueMappings, models, persist }
import shardakka.ShardakkaExtension
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object ContactsUtils {

  def localNameKey(ownerUserId: Int, contactId: Int): String = s"${ownerUserId}_${contactId}"

  def addContact(
    ownerUserId: Int,
    userId:      Int,
    phoneNumber: Long,
    name:        Option[String],
    accessSalt:  String
  )(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem): DBIO[Int] =
    for {
      _ ← DBIO.from(registerLocalName(ownerUserId, userId, name))
      contact = models.contact.UserPhoneContact(phoneNumber, ownerUserId, userId, name, accessSalt, isDeleted = false)
      result ← persist.contact.UserPhoneContact.insertOrUpdate(contact)
    } yield result

  def addContact(
    ownerUserId: Int,
    userId:      Int,
    email:       String,
    name:        Option[String],
    accessSalt:  String
  )(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem): DBIO[Int] =
    for {
      _ ← DBIO.from(registerLocalName(ownerUserId, userId, name))
      contact = models.contact.UserEmailContact(email, ownerUserId, userId, name, accessSalt, isDeleted = false)
      result ← persist.contact.UserEmailContact.insertOrUpdate(contact)
    } yield result

  def deleteContact(ownerUserId: Int, userId: Int)(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem): DBIO[Int] =
    for {
      _ ← DBIO.from(registerLocalName(ownerUserId, userId, None))
      result ← persist.contact.UserContact.delete(ownerUserId, userId)
    } yield result

  def updateName(ownerUserId: Int, userId: Int, name: Option[String])(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem): DBIO[Int] =
    for {
      _ ← DBIO.from(registerLocalName(ownerUserId, userId, name))
      result ← persist.contact.UserContact.updateName(ownerUserId, userId, name)
    } yield result

  def registerLocalName(ownerUserId: Int, userId: Int, name: Option[String])(implicit ec: ExecutionContext, timeout: Timeout, system: ActorSystem): Future[Unit] = {
    val kv = ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.LocalNames)
    val contactKey = localNameKey(ownerUserId, userId)
    name map { n ⇒ kv.upsert(contactKey, n) } getOrElse kv.delete(contactKey)
  }

}
