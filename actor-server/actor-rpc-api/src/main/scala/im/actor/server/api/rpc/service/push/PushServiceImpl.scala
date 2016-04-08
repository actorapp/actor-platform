package im.actor.server.api.rpc.service.push

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import com.google.protobuf.ByteString
import im.actor.api.rpc._
import im.actor.api.rpc.encryption.ApiEncryptionKey
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.push.PushService
import im.actor.server.db.DbExtension
import im.actor.server.model.push.{ ActorPushCredentials, ApplePushCredentials, GooglePushCredentials }
import im.actor.server.persist.push.{ ActorPushCredentialsRepo, ApplePushCredentialsRepo, GooglePushCredentialsRepo }
import im.actor.server.sequence.SeqUpdatesExtension
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

object PushRpcErrors {
  val WrongToken = RpcError(400, "WRONG_TOKEN", "Wrong APNS Token", false, None)
}

final class PushServiceImpl(
  implicit
  actorSystem: ActorSystem
) extends PushService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)

  override def doHandleUnregisterPush(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      for {
        _ ← seqUpdExt.deletePushCredentials(client.authId)
      } yield Ok(ResponseVoid)
    }
  }

  override def doHandleRegisterGooglePush(projectId: Long, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val creds = GooglePushCredentials(clientData.authId, projectId, token)
    for {
      _ ← db.run(GooglePushCredentialsRepo.deleteByToken(token))
      _ ← db.run(GooglePushCredentialsRepo.createOrUpdate(creds))
      _ = seqUpdExt.registerGooglePushCredentials(creds)
    } yield Ok(ResponseVoid)
  }

  override def doHandleRegisterApplePush(apnsKey: Int, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = ApplePushCredentials(clientData.authId, apnsKey, ByteString.copyFrom(tokenBytes), isVoip = false)
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← ApplePushCredentialsRepo.deleteByToken(tokenBytes)
          _ ← ApplePushCredentialsRepo.createOrUpdate(creds)
          _ = seqUpdExt.registerApplePushCredentials(creds)
        } yield Ok(ResponseVoid)
        db.run(action)
      case None ⇒
        Future.successful(Error(PushRpcErrors.WrongToken))
    }
  }

  override def doHandleRegisterActorPush(
    topic:      String,
    publicKeys: IndexedSeq[ApiEncryptionKey],
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = {
    val creds = ActorPushCredentials(clientData.authId, topic)

    db.run(for {
      _ ← ActorPushCredentialsRepo.deleteByTopic(topic)
      _ ← ActorPushCredentialsRepo.createOrUpdate(clientData.authId, topic)
      _ = seqUpdExt.registerActorPushCredentials(creds)
    } yield Ok(ResponseVoid))
  }

  /**
   * Registration of a new Apple's PushKit tokens
   *
   * @param apnsKey APNS key id
   * @param token   token value @note Contains sensitive data!!!
   */
  override protected def doHandleRegisterApplePushKit(
    apnsKey:    Int,
    token:      String,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = ApplePushCredentials(clientData.authId, apnsKey, ByteString.copyFrom(tokenBytes), isVoip = true)
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← ApplePushCredentialsRepo.deleteByToken(tokenBytes)
          _ ← ApplePushCredentialsRepo.createOrUpdate(creds)
        } yield Ok(ResponseVoid)
        db.run(action)
      case None ⇒ Future.successful(Error(PushRpcErrors.WrongToken))
    }
}