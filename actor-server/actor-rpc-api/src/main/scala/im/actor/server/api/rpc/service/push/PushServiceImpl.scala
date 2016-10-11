package im.actor.server.api.rpc.service.push

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
import im.actor.api.rpc._
import im.actor.api.rpc.encryption.ApiEncryptionKey
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.push.PushService
import im.actor.server.db.DbExtension
import im.actor.server.model.push.{ ActorPushCredentials, ApplePushCredentials, FirebasePushCredentials, GCMPushCredentials }
import im.actor.server.persist.push.{ ActorPushCredentialsRepo, ApplePushCredentialsRepo, FirebasePushCredentialsKV, GooglePushCredentialsRepo }
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

  private val db = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)

  private val firebaseKv = new FirebasePushCredentialsKV

  private val OkVoid = Ok(ResponseVoid)
  private val ErrWrongToken = Error(PushRpcErrors.WrongToken)

  override def doHandleRegisterGooglePush(projectId: Long, rawToken: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val (isFCM, token) = extractToken(rawToken)
    if (isFCM) {
      val creds = FirebasePushCredentials(clientData.authId, projectId, token)
      for {
        _ ← firebaseKv.deleteByToken(token)
        _ ← firebaseKv.createOrUpdate(creds)
        _ ← seqUpdExt.registerFirebasePushCredentials(creds)
      } yield OkVoid
    } else {
      val creds = GCMPushCredentials(clientData.authId, projectId, token)
      db.run(for {
        _ ← GooglePushCredentialsRepo.deleteByToken(token)
        _ ← GooglePushCredentialsRepo.createOrUpdate(creds)
        _ ← DBIO.from(seqUpdExt.registerGCMPushCredentials(creds))
      } yield OkVoid)
    }
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
        FastFuture.successful(ErrWrongToken)
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
      case None ⇒ FastFuture.successful(ErrWrongToken)
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
        FastFuture.successful(ErrWrongToken)
    }

  // TODO: figure out, should user be authorized?
  override protected def doHandleUnregisterGooglePush(rawToken: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val (isFCM, token) = extractToken(rawToken)
    (if (isFCM) {
      seqUpdExt.unregisterFirebasePushCredentials(token)
    } else {
      seqUpdExt.unregisterGCMPushCredentials(token)
    }) map (_ ⇒ OkVoid)
  }

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
      case None ⇒ FastFuture.successful(ErrWrongToken)
    }

  private def unregisterApple(token: String) =
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        seqUpdExt.unregisterApplePushCredentials(tokenBytes) map (_ ⇒ OkVoid)
      case None ⇒
        FastFuture.successful(ErrWrongToken)
    }

  // FIXME: temporary hack before schema has changed
  private def extractToken(token: String): (Boolean, String) = {
    val fcmPref = "FCM_"
    if (token.startsWith(fcmPref)) {
      true → token.drop(fcmPref.length)
    } else {
      false → token
    }
  }

}
