package im.actor.server.persist.contact

import slick.dbio.Effect.Write
import im.actor.server.db.ActorPostgresDriver.api._
import slick.profile.FixedSqlAction

import im.actor.server.models

abstract class UserContactBase[T](tag: Tag, tname: String) extends Table[T](tag, tname) {
  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)
  def contactUserId = column[Int]("contact_user_id", O.PrimaryKey)
  def name = column[Option[String]]("name")
  def accessSalt = column[String]("access_salt")
  def isDeleted = column[Boolean]("is_deleted", O.Default(false))

  def idx = index("idx_user_contacts_owner_user_id_is_deleted", (ownerUserId, isDeleted))
}

class UserContactTable(tag: Tag) extends UserContactBase[models.contact.UserContact](tag, "user_contacts") {
  def * = (ownerUserId, contactUserId, name, accessSalt, isDeleted) <> (models.contact.UserContact.tupled, models.contact.UserContact.unapply)
}

object UserContact {
  val contacts = TableQuery[UserContactTable]

  def byPK(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId)

  def byOwnerUserIdNotDeleted(ownerUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.isDeleted === false)

  def byPKNotDeleted(ownerUserId: Rep[Int], contactUserId: Rep[Int]) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === false)
  val nameByPKNotDeletedC = Compiled(
    (ownerUserId: Rep[Int], contactUserId: Rep[Int]) ⇒
      byPKNotDeleted(ownerUserId, contactUserId) map (_.name)
  )

  def byPKDeleted(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === true)

  //TODO: check usages - make sure they dont need phone number
  def find(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).result.headOption

  def findIds(ownerUserId: Int, contactUserIds: Set[Int]) =
    byOwnerUserIdNotDeleted(ownerUserId).filter(_.contactUserId inSet contactUserIds).map(_.contactUserId).result

  def findNotDeletedIds(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(_.contactUserId).result

  def findName(ownerUserId: Int, contactUserId: Int) =
    nameByPKNotDeletedC((ownerUserId, contactUserId)).result

  def findContactIdsAll(ownerUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId).map(_.contactUserId).result

  def findContactIdsActive(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(_.contactUserId).result

  def updateName(ownerUserId: Int, contactUserId: Int, name: Option[String]): FixedSqlAction[Int, NoStream, Write] = {
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId).map(_.name).update(name)
  }

  def delete(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).map(_.isDeleted).update(true)
}
