package im.actor.server.persist.social

import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

import im.actor.server.models

class RelationTable(tag: Tag) extends Table[models.social.Relation](tag, "social_relations") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def relatedTo = column[Int]("related_to", O.PrimaryKey)

  def * = (userId, relatedTo) <> (models.social.Relation.tupled, models.social.Relation.unapply)
}

object Relation {
  val relations = TableQuery[RelationTable]

  def create(relation: models.social.Relation): FixedSqlAction[Int, NoStream, Write] =
    relations += relation

  def create(userId: Int, relatedTo: Int): FixedSqlAction[Int, NoStream, Write] =
    relations += models.social.Relation(userId, relatedTo)

  def create(userId: Int, relatedTo: Set[Int]) =
    relations ++= relatedTo.toSeq map (models.social.Relation(userId, _))

  def find(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    relations.filter(_.userId === userId).map(_.relatedTo).result
}