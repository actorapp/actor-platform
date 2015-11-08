package im.actor.server.persist.push

import com.google.protobuf.ByteString
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.persist.AuthIdRepo

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

final class ApplePushCredentialsTable(tag: Tag) extends Table[ApplePushCredentials](tag, "apple_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def apnsKey = column[Int]("apns_key")

  def token = column[ByteString]("token")

  def * = (authId, apnsKey, token) <> ((ApplePushCredentials.apply _).tupled, ApplePushCredentials.unapply)
}

object ApplePushCredentialsRepo {
  val creds = TableQuery[ApplePushCredentialsTable]

  def byToken(token: ByteString): Query[ApplePushCredentialsTable, ApplePushCredentials, Seq] =
    creds.filter(_.token === token)

  def byToken(token: Array[Byte]): Query[ApplePushCredentialsTable, ApplePushCredentials, Seq] =
    byToken(ByteString.copyFrom(token))

  def createOrUpdate(authId: Long, apnsKey: Int, token: ByteString)(implicit ec: ExecutionContext) = {
    for {
      _ ← creds.filterNot(_.authId === authId).filter(c ⇒ c.apnsKey === apnsKey && c.token === token).delete
      r ← creds.insertOrUpdate(ApplePushCredentials(authId, apnsKey, token))
    } yield r
  }

  def createOrUpdate(c: ApplePushCredentials) =
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

  def findByToken(token: Array[Byte]) =
    byToken(token).result

  def deleteByToken(token: Array[Byte]) =
    byToken(token).delete
}