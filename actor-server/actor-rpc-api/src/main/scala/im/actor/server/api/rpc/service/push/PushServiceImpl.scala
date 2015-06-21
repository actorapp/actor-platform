package im.actor.server.api.rpc.service.push

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.push.PushService
import im.actor.server.{ models, persist }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }

class PushServiceImpl(
  implicit
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  db:                  Database,
  actorSystem:         ActorSystem
) extends PushService {
  import SeqUpdatesManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleUnregisterPush(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = for {
      _ ← persist.push.ApplePushCredentials.delete(clientData.authId).asTry
      _ ← persist.push.GooglePushCredentials.delete(clientData.authId).asTry
    } yield Ok(ResponseVoid)

    setUpdatedApplePushCredentials(clientData.authId, None)
    setUpdatedGooglePushCredentials(clientData.authId, None)

    db.run(action)
  }

  override def jhandleRegisterGooglePush(projectId: Long, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val creds = models.push.GooglePushCredentials(clientData.authId, projectId, token)

    val action = for {
      _ ← persist.push.GooglePushCredentials.createOrUpdate(creds)
    } yield Ok(ResponseVoid)

    setUpdatedGooglePushCredentials(clientData.authId, Some(creds))

    db.run(action)
  }

  override def jhandleRegisterApplePush(apnsKey: Int, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val creds = models.push.ApplePushCredentials(clientData.authId, apnsKey, tokenBits.toByteArray)

        val action = for {
          _ ← persist.push.ApplePushCredentials.createOrUpdate(creds)
        } yield (Ok(ResponseVoid))

        setUpdatedApplePushCredentials(clientData.authId, Some(creds))

        db.run(action)
      case None ⇒
        Future.successful(Error(RpcError(400, "WRONG_TOKEN", "Wrong APNS Token", false, None)))
    }
  }
}