package im.actor.server.persist.social

import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

import im.actor.server.model

final class RelationTable(tag: Tag) extends Table[model.social.Relation](tag, "social_relations") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def relatedTo = column[Int]("related_to", O.PrimaryKey)

  def * = (userId, relatedTo) <> (model.social.Relation.tupled, model.social.Relation.unapply)
}

object RelationRepo {
  val relations = TableQuery[RelationTable]

  def create(relation: model.social.Relation): FixedSqlAction[Int, NoStream, Write] =
    relations += relation

  def create(userId: Int, relatedTo: Int): FixedSqlAction[Int, NoStream, Write] =
    relations += model.social.Relation(userId, relatedTo)

  def create(userId: Int, relatedTo: Set[Int]) =
    relations ++= relatedTo.toSeq map (model.social.Relation(userId, _))

  def find(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    relations.filter(_.userId === userId).map(_.relatedTo).result
}