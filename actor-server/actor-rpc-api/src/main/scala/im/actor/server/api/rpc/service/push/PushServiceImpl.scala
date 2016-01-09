package im.actor.server.api.rpc.service.push

import akka.actor.ActorSystem
import com.google.protobuf.ByteString
import im.actor.api.rpc._
import im.actor.api.rpc.encryption.ApiEncryptionKey
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.push.PushService
import im.actor.server.db.DbExtension
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.{ model, persist }
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

final class PushServiceImpl(
  implicit
  actorSystem: ActorSystem
) extends PushService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)

  override def jhandleUnregisterPush(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      for {
        _ ← seqUpdExt.deletePushCredentials(client.authId)
      } yield Ok(ResponseVoid)
    }
  }

  override def jhandleRegisterGooglePush(projectId: Long, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val creds = model.push.GooglePushCredentials(clientData.authId, projectId, token)
    for {
      _ ← db.run(persist.push.GooglePushCredentialsRepo.deleteByToken(token))
      _ ← db.run(persist.push.GooglePushCredentialsRepo.createOrUpdate(creds))
      _ = seqUpdExt.registerGooglePushCredentials(creds)
    } yield Ok(ResponseVoid)
  }

  override def jhandleRegisterApplePush(apnsKey: Int, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = model.push.ApplePushCredentials(clientData.authId, apnsKey, ByteString.copyFrom(tokenBytes))
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← persist.push.ApplePushCredentialsRepo.deleteByToken(tokenBytes)
          _ ← persist.push.ApplePushCredentialsRepo.createOrUpdate(creds)
          _ = seqUpdExt.registerApplePushCredentials(creds)
        } yield Ok(ResponseVoid)
        db.run(action)
      case None ⇒
        Future.successful(Error(RpcError(400, "WRONG_TOKEN", "Wrong APNS Token", false, None)))
    }
  }

  override def jhandleRegisterActorPush(
    endpoint:   String,
    publicKeys: IndexedSeq[ApiEncryptionKey],
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    Future.failed(new RuntimeException("Not implemented"))
}