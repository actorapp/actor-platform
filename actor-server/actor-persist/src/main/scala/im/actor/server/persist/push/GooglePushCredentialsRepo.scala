package im.actor.server.persist.push

import im.actor.server.model.push.GooglePushCredentials
import im.actor.server.persist.AuthIdRepo
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

final class GooglePushCredentialsTable(tag: Tag) extends Table[GooglePushCredentials](tag, "google_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def projectId = column[Long]("project_id")

  def regId = column[String]("reg_id")

  def * = (authId, projectId, regId) <> ((GooglePushCredentials.apply _).tupled, GooglePushCredentials.unapply)
}

object GooglePushCredentialsRepo {
  val creds = TableQuery[GooglePushCredentialsTable]

  def createOrUpdate(authId: Long, projectId: Long, regId: String)(implicit ec: ExecutionContext) = {
    for {
      _ ← creds.filterNot(_.authId === authId).filter(c ⇒ c.projectId === projectId && c.regId === regId).delete
      r ← creds.insertOrUpdate(GooglePushCredentials(authId, projectId, regId))
    } yield r
  }

  def createOrUpdate(c: GooglePushCredentials) =
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

  def deleteByToken(token: String) =
    creds.filter(_.regId === token).delete
}