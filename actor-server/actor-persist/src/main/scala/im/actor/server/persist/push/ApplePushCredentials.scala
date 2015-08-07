package im.actor.server.persist.push

import scala.concurrent.ExecutionContext

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class ApplePushCredentialsTable(tag: Tag) extends Table[models.push.ApplePushCredentials](tag, "apple_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def apnsKey = column[Int]("apns_key")

  def token = column[Array[Byte]]("token")

  def * = (authId, apnsKey, token) <> (models.push.ApplePushCredentials.tupled, models.push.ApplePushCredentials.unapply)
}

object ApplePushCredentials {
  val creds = TableQuery[ApplePushCredentialsTable]

  def byToken(token: Array[Byte]) = creds.filter(_.token === token)

  def createOrUpdate(authId: Long, apnsKey: Int, token: Array[Byte])(implicit ec: ExecutionContext) = {
    for {
      _ ← creds.filterNot(_.authId === authId).filter(c ⇒ c.apnsKey === apnsKey && c.token === token).delete
      r ← creds.insertOrUpdate(models.push.ApplePushCredentials(authId, apnsKey, token))
    } yield r
  }

  def createOrUpdate(c: models.push.ApplePushCredentials) =
    creds.insertOrUpdate(c)

  def byAuthId(authId: Rep[Long]) = creds.filter(_.authId === authId)
  val byAuthIdC = Compiled(byAuthId _)

  def find(authId: Long) =
    byAuthIdC(authId).result.headOption

  def delete(authId: Long) =
    creds.filter(_.authId === authId).delete

  def findByToken(token: Array[Byte]) =
    byToken(token).result

  def deleteByToken(token: Array[Byte]) =
    byToken(token).delete
}