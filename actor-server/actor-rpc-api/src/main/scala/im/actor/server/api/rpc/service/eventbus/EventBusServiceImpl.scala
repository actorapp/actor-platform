package im.actor.server.api.rpc.service.eventbus

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.ClientData
import im.actor.api.rpc._
import im.actor.api.rpc.eventbus._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.server.eventbus.{ EventBus, EventBusErrors, EventBusExtension }

import scala.concurrent.{ ExecutionContext, Future }

object EventBusRpcErrors {
  val EventBusNotFound = Error(RpcError(404, "NOT_FOUND", "EventBus not found.", canTryAgain = false, data = None))
}

final class EventbusServiceImpl(system: ActorSystem) extends EventbusService {
  override implicit protected val ec: ExecutionContext = system.dispatcher
  private val ext = EventBusExtension(system)

  override def doHandleCreateNewEventBus(
    timeout:    Option[Long],
    isOwned:    Option[Boolean],
    clientData: ClientData
  ): Future[HandlerResult[ResponseCreateNewEventBus]] =
    authorized(clientData) { client ⇒
      for {
        (id, deviceId) ← ext.create(client.userId, client.authId, timeout, isOwned)
      } yield Ok(ResponseCreateNewEventBus(id, deviceId))
    }

  override def doHandleDisposeEventBus(id: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for (_ ← ext.dispose(client.userId, client.authId, id)) yield Ok(ResponseVoid)
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }

  /**
   * Rejoining to event bus after session was disposed
   *
   * @param id          Event Bus Id
   * @param rejoinToken Rejoin Token
   */
  override protected def doHandleReJoinEventBus(
    id:          String,
    rejoinToken: Array[Byte],
    clientData:  ClientData
  ): Future[HandlerResult[ResponseReJoinEventBus]] = FastFuture.failed(new RuntimeException("Not implemented"))

  /**
   * Event Bus Destination
   *
   * @param id           Bus Id
   * @param destinations If Empty need to broadcase message to everyone
   * @param message      Message
   */
  override protected def doHandlePostToEventBus(
    id:           String,
    destinations: IndexedSeq[Long],
    message:      Array[Byte],
    clientData:   ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← ext.post(EventBus.ExternalClient(client.userId, client.authId), id, destinations, message)
      } yield Ok(ResponseVoid)
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }

  override def doHandleKeepAliveEventBus(
    id:         String,
    timeout:    Option[Long],
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for (_ ← ext.keepAlive(EventBus.ExternalClient(client.userId, client.authId), id, timeout))
        yield Ok(ResponseVoid)
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }

  override def doHandleJoinEventBus(
    id:         String,
    timeout:    Option[Long],
    clientData: ClientData
  ): Future[HandlerResult[ResponseJoinEventBus]] =
    authorized(clientData) { client ⇒
      for {
        deviceId ← ext.join(EventBus.ExternalClient(client.userId, client.authId), id, timeout)
      } yield Ok(ResponseJoinEventBus(deviceId, None))
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }
}