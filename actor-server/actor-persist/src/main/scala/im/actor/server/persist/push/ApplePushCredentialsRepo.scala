package im.actor.server.persist.push

import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.persist.AuthIdRepo

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

final class ApplePushCredentialsTable(tag: Tag) extends Table[ApplePushCredentials](tag, "apple_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)
  def apnsKey = column[Option[Int32Value]]("apns_key")
  def token = column[ByteString]("token")
  def isVoip = column[Boolean]("is_voip", O.PrimaryKey)
  def bundleId = column[Option[StringValue]]("bundle_id")

  def * = (authId, apnsKey, token, isVoip, bundleId) <> ((ApplePushCredentials.apply _).tupled, ApplePushCredentials.unapply)
}

object ApplePushCredentialsRepo {
  val creds = TableQuery[ApplePushCredentialsTable]

  def byToken(token: ByteString): Query[ApplePushCredentialsTable, ApplePushCredentials, Seq] =
    creds.filter(_.token === token)

  def byToken(token: Array[Byte]): Query[ApplePushCredentialsTable, ApplePushCredentials, Seq] =
    byToken(ByteString.copyFrom(token))

  def createOrUpdate(c: ApplePushCredentials) =
    creds.insertOrUpdate(c)

  private def byAuthId(authId: Rep[Long]) = creds.filter(_.authId === authId)

  private val byAuthIdC = Compiled(byAuthId _)

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