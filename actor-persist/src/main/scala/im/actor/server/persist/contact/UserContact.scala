package im.actor.server.persist.contact

import im.actor.server.models

import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }

import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

class UserContact(tag: Tag) extends Table[models.contact.UserContact](tag, "user_contacts") {
  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)
  def contactUserId = column[Int]("contact_user_id", O.PrimaryKey)
  def phoneNumber = column[Long]("phone_number")
  def name = column[Option[String]]("name")
  def accessSalt = column[String]("access_salt")
  def isDeleted = column[Boolean]("is_deleted")

  def * = (ownerUserId, contactUserId, phoneNumber, name, accessSalt, isDeleted) <> (models.contact.UserContact.tupled, models.contact.UserContact.unapply)
}

object UserContact {
  val contacts = TableQuery[UserContact]

  def byPK(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c => c.ownerUserId === ownerUserId && c.contactUserId === contactUserId)

  def byOwnerUserIdNotDeleted(ownerUserId: Int) =
    contacts.filter(c => c.ownerUserId === ownerUserId && c.isDeleted === false)

  def byPKNotDeleted(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c => c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === false)

  def byPKDeleted(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c => c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === true)

  def find(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).result.headOption

  def findIds(ownerUserId: Int, contactUserIds: Set[Int]) =
    contacts.filter(c => c.isDeleted === false && c.ownerUserId === ownerUserId).filter(_.contactUserId inSet contactUserIds).map(_.contactUserId).result

  def findIds_all(ownerUserId: Int) =
    contacts.filter(_.ownerUserId === ownerUserId).map(_.contactUserId).result

  def findName(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).map(_.name).result

  def findContactIdsAll(ownerUserId: Int) =
    contacts.filter(c => c.ownerUserId === ownerUserId).map(_.contactUserId).result

  def findContactIdsWithLocalNames(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(c => (c.contactUserId, c.name)).result

  def createOrRestore(ownerUserId: Int, contactUserId: Int, phoneNumber: Long, name: Option[String], accessSalt: String) = {
    val contact = models.contact.UserContact(ownerUserId, contactUserId, phoneNumber, name, accessSalt, false)
    contacts.insertOrUpdate(contact)
  }

  def insertOrUpdate(contact: models.contact.UserContact) =
    contacts.insertOrUpdate(contact)

  def delete(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).map(_.isDeleted).update(true)
}
