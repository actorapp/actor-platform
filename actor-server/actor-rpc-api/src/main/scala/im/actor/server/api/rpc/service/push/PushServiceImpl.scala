package im.actor.server.api.rpc.service.push

import akka.actor.ActorSystem
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
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

  private val OkVoid = Ok(ResponseVoid)
  private val ErrWrongToken = Error(PushRpcErrors.WrongToken)

  override def doHandleRegisterGooglePush(projectId: Long, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val creds = GooglePushCredentials(clientData.authId, projectId, token)
    for {
      _ ← db.run(GooglePushCredentialsRepo.deleteByToken(token))
      _ ← db.run(GooglePushCredentialsRepo.createOrUpdate(creds))
      _ = seqUpdExt.registerGooglePushCredentials(creds)
    } yield OkVoid
  }

  override def doHandleRegisterApplePush(apnsKey: Int, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = ApplePushCredentials(
          authId = clientData.authId,
          apnsKey = Some(Int32Value(apnsKey)),
          token = ByteString.copyFrom(tokenBytes),
          isVoip = false
        )
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← ApplePushCredentialsRepo.deleteByToken(tokenBytes)
          _ ← ApplePushCredentialsRepo.createOrUpdate(creds)
          _ = seqUpdExt.registerApplePushCredentials(creds)
        } yield OkVoid
        db.run(action)
      case None ⇒
        Future.successful(ErrWrongToken)
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
    } yield OkVoid)
  }

  override protected def doHandleRegisterApplePushKit(
    apnsKey:    Int,
    token:      String,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = ApplePushCredentials(
          authId = clientData.authId,
          apnsKey = Some(Int32Value(apnsKey)),
          token = ByteString.copyFrom(tokenBytes),
          isVoip = true
        )
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← ApplePushCredentialsRepo.deleteByToken(tokenBytes)
          _ ← ApplePushCredentialsRepo.createOrUpdate(creds)
        } yield OkVoid
        db.run(action)
      case None ⇒ Future.successful(ErrWrongToken)
    }

  override protected def doHandleRegisterApplePushToken(
    bundleId:   String,
    token:      String,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = ApplePushCredentials(
          authId = clientData.authId,
          bundleId = Some(StringValue(bundleId)),
          token = ByteString.copyFrom(tokenBytes),
          isVoip = false
        )
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← ApplePushCredentialsRepo.deleteByToken(tokenBytes)
          _ ← ApplePushCredentialsRepo.createOrUpdate(creds)
          _ = seqUpdExt.registerApplePushCredentials(creds)
        } yield OkVoid
        db.run(action)
      case None ⇒
        Future.successful(ErrWrongToken)
    }

  // TODO: figure out, should user be authorized?
  override protected def doHandleUnregisterGooglePush(token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    seqUpdExt.unregisterGooglePushCredentials(token) map (_ ⇒ OkVoid)

  // TODO: figure out, should user be authorized?
  override protected def doHandleUnregisterActorPush(endpoint: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    seqUpdExt.unregisterActorPushCredentials(endpoint) map (_ ⇒ OkVoid)

  // TODO: figure out, should user be authorized?
  override protected def doHandleUnregisterApplePush(token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    unregisterApple(token)

  // TODO: figure out, should user be authorized?
  override protected def doHandleUnregisterApplePushToken(token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    unregisterApple(token)

  // TODO: figure out, should user be authorized?
  override protected def doHandleUnregisterApplePushKit(token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        db.run(ApplePushCredentialsRepo.deleteByToken(tokenBytes)) map (_ ⇒ OkVoid)
      case None ⇒ Future.successful(ErrWrongToken)
    }

  private def unregisterApple(token: String) =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        seqUpdExt.unregisterApplePushCredentials(tokenBytes) map (_ ⇒ OkVoid)
      case None ⇒
        Future.successful(ErrWrongToken)
    }

}