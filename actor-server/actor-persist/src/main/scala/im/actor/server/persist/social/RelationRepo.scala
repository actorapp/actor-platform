package im.actor.server.persist.social

import im.actor.server.model.social.{ Relation, RelationStatus }
import RelationStatusColumnType._
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

final class RelationTable(tag: Tag) extends Table[Relation](tag, "social_relations") {
  // userId is relation owner
  def userId = column[Int]("user_id", O.PrimaryKey)
  // his relation to other user
  def relatedTo = column[Int]("related_to", O.PrimaryKey)
  // and this relation have status
  def status = column[RelationStatus]("relation_status")

  def * = (userId, relatedTo, status) <> (Relation.tupled, Relation.unapply)
}

object RelationRepo {
  val relations = TableQuery[RelationTable]

  def create(relation: Relation): FixedSqlAction[Int, NoStream, Write] =
    relations += relation

  def create(userId: Int, relatedTo: Int): FixedSqlAction[Int, NoStream, Write] =
    relations += Relation(userId, relatedTo, RelationStatus.Approved)

  def create(userId: Int, relatedTo: Set[Int]) =
    relations ++= relatedTo.toSeq map (Relation(userId, _, RelationStatus.Approved))

  def fetch(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    relations.filter(_.userId === userId).map(_.relatedTo).result

  private def related(userId: Rep[Int], relatedTo: Rep[Int]) =
    relations.filter(r ⇒ r.userId === userId && r.relatedTo === relatedTo)

  def find(userId: Int, relatedTo: Int): DBIO[Option[Relation]] =
    related(userId, relatedTo).result.headOption

  def block(userId: Int, relatedTo: Int): DBIO[Int] =
    related(userId, relatedTo).map(_.status).update(RelationStatus.Blocked)

  def unblock(userId: Int, relatedTo: Int): DBIO[Int] =
    related(userId, relatedTo).map(_.status).update(RelationStatus.Approved)

  def isBlocked(userId: Int, relatedTo: Int): DBIO[Boolean] =
    related(userId, relatedTo).filter(_.status === RelationStatus.Blocked).exists.result

  def fetchBlockedIds(userId: Int): DBIO[Seq[Int]] =
    relations.filter(r ⇒ r.userId === userId && r.status === RelationStatus.Blocked).map(_.relatedTo).result

}