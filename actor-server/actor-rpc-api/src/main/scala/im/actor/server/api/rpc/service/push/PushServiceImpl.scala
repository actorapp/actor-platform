package im.actor.server.api.rpc.service.push

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.push.PushService
import im.actor.server.db.DbExtension
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqUpdatesManager }
import im.actor.server.{ models, persist }
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

class PushServiceImpl(
  implicit
  actorSystem: ActorSystem
) extends PushService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)

  override def jhandleUnregisterPush(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    SeqUpdatesManager.deletePushCredentials(clientData.authId)
    Future.successful(Ok(ResponseVoid))
  }

  override def jhandleRegisterGooglePush(projectId: Long, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val creds = models.push.GooglePushCredentials(clientData.authId, projectId, token)
    val action: DBIO[HandlerResult[ResponseVoid]] = for {
      _ ← persist.push.GooglePushCredentials.deleteByToken(token)
      _ ← DBIO.successful(SeqUpdatesManager.setPushCredentials(clientData.authId, creds))
    } yield Ok(ResponseVoid)
    db.run(action)
  }

  override def jhandleRegisterApplePush(apnsKey: Int, token: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    BitVector.fromHex(token) match {
      case Some(tokenBits) ⇒
        val tokenBytes = tokenBits.toByteArray
        val creds = models.push.ApplePushCredentials(clientData.authId, apnsKey, tokenBytes)
        val action: DBIO[HandlerResult[ResponseVoid]] = for {
          _ ← persist.push.ApplePushCredentials.deleteByToken(tokenBytes)
          _ ← DBIO.successful(SeqUpdatesManager.setPushCredentials(clientData.authId, creds))
        } yield Ok(ResponseVoid)
        db.run(action)
      case None ⇒
        Future.successful(Error(RpcError(400, "WRONG_TOKEN", "Wrong APNS Token", false, None)))
    }
  }
}