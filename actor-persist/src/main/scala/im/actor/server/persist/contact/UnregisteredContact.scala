package im.actor.server.persist.contact

import im.actor.server.models

import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }

import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

class UnregisteredContactTable(tag: Tag) extends Table[models.UnregisteredContact](tag, "unregistered_contacts") {
  def phoneNumber = column[Long]("phone_number", O.PrimaryKey)

  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)

  def name = column[Option[String]]("name")

  def * = (phoneNumber, ownerUserId, name) <> (models.UnregisteredContact.tupled, models.UnregisteredContact.unapply)
}

object UnregisteredContact {
  val ucontacts = TableQuery[UnregisteredContactTable]

  def create(phoneNumber: Long, ownerUserId: Int, name: Option[String]) =
    ucontacts += models.UnregisteredContact(phoneNumber, ownerUserId, name)

  def createIfNotExists(phoneNumber: Long, ownerUserId: Int, name: Option[String]) = {
    create(phoneNumber, ownerUserId, name).asTry
  }

  def find(phoneNumber: Long) =
    ucontacts.filter(_.phoneNumber === phoneNumber).result

  def deleteAll(phoneNumber: Long) =
    ucontacts.filter(_.phoneNumber === phoneNumber).delete
}