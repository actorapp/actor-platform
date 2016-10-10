package im.actor.server.persist.push

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import com.google.protobuf.wrappers.Int64Value
import im.actor.server.db.DbExtension
import im.actor.server.model.push.FirebasePushCredentials
import im.actor.storage.SimpleStorage

import scala.concurrent.Future

// authId -> FirebasePushCredentials
private object FirebaseAuthIdCreds extends SimpleStorage("firebase_auth_id_creds")

// token -> authId
private object FirebaseTokenAuthId extends SimpleStorage("firebase_token_auth_id")

final class FirebasePushCredentialsKV(implicit system: ActorSystem) {
  import system.dispatcher

  private val (db, conn) = {
    val ext = DbExtension(system)
    (ext.db, ext.connector)
  }

  def createOrUpdate(creds: FirebasePushCredentials): Future[Unit] =
    for {
      _ ← conn.run(
        FirebaseAuthIdCreds.upsert(creds.authId.toString, creds.toByteArray)
      )
      _ ← conn.run(
        FirebaseTokenAuthId.upsert(creds.regId, Int64Value(creds.authId).toByteArray)
      )
    } yield ()

  def deleteByToken(token: String): Future[Unit] =
    for {
      authIdOpt ← findAuthIdByToken(token)
      _ ← authIdOpt map { authId ⇒
        delete(token, authId)
      } getOrElse FastFuture.successful(None)
    } yield ()

  def findByToken(token: String): Future[Option[FirebasePushCredentials]] =
    for {
      authIdOpt ← findAuthIdByToken(token)
      creds ← (authIdOpt map find) getOrElse FastFuture.successful(None)
    } yield creds

  def find(authId: Long): Future[Option[FirebasePushCredentials]] =
    conn.run(FirebaseAuthIdCreds.get(authId.toString)) map (_.map(FirebasePushCredentials.parseFrom))

  def find(authIds: Set[Long]): Future[Seq[FirebasePushCredentials]] =
    Future.sequence(authIds map find) map (_.flatten.toSeq)

  private def findAuthIdByToken(token: String): Future[Option[Long]] =
    conn.run(FirebaseTokenAuthId.get(token)) map (_.map(e ⇒ Int64Value.parseFrom(e).value))

  private def delete(token: String, authId: Long): Future[Unit] =
    for {
      _ ← conn.run(FirebaseAuthIdCreds.delete(authId.toString))
      _ ← conn.run(FirebaseTokenAuthId.delete(token))
    } yield ()

}
