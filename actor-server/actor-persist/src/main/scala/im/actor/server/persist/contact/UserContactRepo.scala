package im.actor.server.persist.contact

import im.actor.server.model.contact.UserContact
import slick.dbio.Effect.Write
import im.actor.server.db.ActorPostgresDriver.api._
import slick.profile.FixedSqlAction

private[contact] abstract class UserContactBase[T](tag: Tag, tname: String) extends Table[T](tag, tname) {
  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)
  def contactUserId = column[Int]("contact_user_id", O.PrimaryKey)
  def name = column[Option[String]]("name")
  def isDeleted = column[Boolean]("is_deleted", O.Default(false))

  def idx = index("idx_user_contacts_owner_user_id_is_deleted", (ownerUserId, isDeleted))
}

final class UserContactTable(tag: Tag) extends UserContactBase[UserContact](tag, "user_contacts") {
  def * = (ownerUserId, contactUserId, name, isDeleted) <> (UserContact.tupled, UserContact.unapply)
}

object UserContactRepo {
  val contacts = TableQuery[UserContactTable]
  val active = contacts.filter(_.isDeleted === false)

  private def byOwnerUserIdNotDeleted(ownerUserId: Rep[Int]) =
    active.filter(_.ownerUserId === ownerUserId)

  private val byOwnerUserIdNotDeletedC = Compiled(byOwnerUserIdNotDeleted _)

  private val countC = Compiled { (userId: Rep[Int]) ⇒
    byOwnerUserIdNotDeleted(userId).length
  }

  def byPKNotDeleted(ownerUserId: Rep[Int], contactUserId: Rep[Int]) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === false)

  val nameByPKNotDeletedC = Compiled(
    (ownerUserId: Rep[Int], contactUserId: Rep[Int]) ⇒
      byPKNotDeleted(ownerUserId, contactUserId) map (_.name)
  )

  def byContactUserId(contactUserId: Rep[Int]) = active.filter(_.contactUserId === contactUserId)
  val byContactUserIdC = Compiled(byContactUserId _)

  def byPKDeleted(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === true)

  private def existsC = Compiled { (ownerUserId: Rep[Int], contactUserId: Rep[Int]) ⇒
    byPKNotDeleted(ownerUserId, contactUserId).exists
  }

  def fetchAll = active.result

  def exists(ownerUserId: Int, contactUserId: Int) = existsC((ownerUserId, contactUserId)).result

  def find(ownerUserId: Int, contactUserId: Int): DBIO[Option[UserContact]] =
    byPKNotDeleted(ownerUserId, contactUserId).result.headOption

  def count(ownerUserId: Int) = countC(ownerUserId).result

  def findIds(ownerUserId: Int, contactUserIds: Set[Int]) =
    byOwnerUserIdNotDeletedC.applied(ownerUserId).filter(_.contactUserId inSet contactUserIds).map(_.contactUserId).result

  def findOwners(contactUserId: Int) = byContactUserIdC(contactUserId).result

  def findNotDeletedIds(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(_.contactUserId).result

  def findName(ownerUserId: Int, contactUserId: Int) =
    nameByPKNotDeletedC((ownerUserId, contactUserId)).result

  def findContactIdsAll(ownerUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId).map(_.contactUserId).result

  def findContactIdsActive(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(_.contactUserId).distinct.result

  def updateName(ownerUserId: Int, contactUserId: Int, name: Option[String]): FixedSqlAction[Int, NoStream, Write] = {
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId).map(_.name).update(name)
  }

  def delete(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).map(_.isDeleted).update(true)

  def insertOrUpdate(contact: UserContact) =
    contacts.insertOrUpdate(contact)
}
