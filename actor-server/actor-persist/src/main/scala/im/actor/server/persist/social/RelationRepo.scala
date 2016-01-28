package im.actor.server.persist.social

import im.actor.server.model.social.Relation
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

final class RelationTable(tag: Tag) extends Table[Relation](tag, "social_relations") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def relatedTo = column[Int]("related_to", O.PrimaryKey)

  def * = (userId, relatedTo) <> (Relation.tupled, Relation.unapply)
}

object RelationRepo {
  val relations = TableQuery[RelationTable]

  def create(relation: Relation): FixedSqlAction[Int, NoStream, Write] =
    relations += relation

  def create(userId: Int, relatedTo: Int): FixedSqlAction[Int, NoStream, Write] =
    relations += Relation(userId, relatedTo)

  def create(userId: Int, relatedTo: Set[Int]) =
    relations ++= relatedTo.toSeq map (Relation(userId, _))

  def find(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    relations.filter(_.userId === userId).map(_.relatedTo).result
}