package im.actor.server.persist.push

import im.actor.server.model.push.ActorPushCredentials
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.persist.AuthIdRepo

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

final class ActorPushCredentialsTable(tag: Tag) extends Table[ActorPushCredentials](tag, "actor_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def topic = column[String]("topic")

  def * = (authId, topic) <> ((ActorPushCredentials.apply _).tupled, ActorPushCredentials.unapply)
}

object ActorPushCredentialsRepo {
  val creds = TableQuery[ActorPushCredentialsTable]

  def byTopic(topic: String): Query[ActorPushCredentialsTable, ActorPushCredentials, Seq] =
    creds.filter(_.topic === topic)

  def createOrUpdate(authId: Long, topic: String)(implicit ec: ExecutionContext) = {
    for {
      _ ← creds.filterNot(_.authId === authId).filter(c ⇒ c.topic === topic).delete
      r ← creds.insertOrUpdate(ActorPushCredentials(authId, topic))
    } yield r
  }

  def createOrUpdate(c: ActorPushCredentials) =
    creds.insertOrUpdate(c)

  def byAuthId(authId: Rep[Long]) = creds.filter(_.authId === authId)

  val byAuthIdC = Compiled(byAuthId _)

  def find(authId: Long) =
    byAuthIdC(authId).result.headOption

  def find(authIds: Set[Long]) =
    creds filter (_.authId inSet authIds) result

  def findByUser(userId: Int)(implicit ec: ExecutionContext) =
    for {
      authIds ← AuthIdRepo.activeByUserIdCompiled(userId).result
      creds ← find(authIds map (_.id) toSet)
    } yield creds

  def delete(authId: Long) =
    creds.filter(_.authId === authId).delete

  def findByTopic(topic: String) =
    byTopic(topic).result

  def deleteByTopic(topic: String) =
    byTopic(topic).delete
}