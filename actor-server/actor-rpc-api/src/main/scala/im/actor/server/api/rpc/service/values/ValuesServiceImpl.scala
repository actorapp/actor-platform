package im.actor.server.api.rpc.service.values

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.values.{ ResponseLoadSyncedSet, ValuesService }
import im.actor.server.session.{ SessionEnvelope, SessionRegion, SubscribeToWeak }
import im.actor.server.values.ValuesExtension

import scala.concurrent.{ ExecutionContext, Future }

final class ValuesServiceImpl(implicit system: ActorSystem, sessionRegion: SessionRegion) extends ValuesService {
  override implicit protected val ec: ExecutionContext = system.dispatcher
  private val valuesExt = ValuesExtension(system)

  /**
   * Loading synced set
   *
   * @param setName readable name of the set
   */
  override protected def doHandleLoadSyncedSet(
    setName:    String,
    clientData: ClientData
  ): Future[HandlerResult[ResponseLoadSyncedSet]] =
    authorized(clientData) { client ⇒
      sessionRegion.ref !
        SessionEnvelope(clientData.authId, clientData.sessionId)
        .withSubscribeToWeak(SubscribeToWeak(Some(valuesExt.syncedSet.weakGroup(setName))))

      for {
        values ← valuesExt.syncedSet.loadApiValues(client.userId, setName)
      } yield Ok(ResponseLoadSyncedSet(values, isStrong = Some(false)))
    }
}