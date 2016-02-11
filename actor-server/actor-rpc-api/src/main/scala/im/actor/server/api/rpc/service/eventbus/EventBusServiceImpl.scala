package im.actor.server.api.rpc.service.eventbus

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.ClientData
import im.actor.api.rpc._
import im.actor.api.rpc.eventbus.{ ApiEventBusDestination, ResponseCreateNewEventBus, ResponseJoinEventBus, EventbusService }
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.server.eventbus.{ EventBusErrors, EventBusExtension }

import scala.concurrent.{ ExecutionContext, Future }

object EventBusRpcErrors {
  val EventBusNotFound = Error(RpcError(404, "NOT_FOUND", "EventBus not found.", canTryAgain = false, data = None))
}

final class EventbusServiceImpl(system: ActorSystem) extends EventbusService {
  override implicit protected val ec: ExecutionContext = system.dispatcher
  private val ext = EventBusExtension(system)

  override def jhandleCreateNewEventBus(
    timeout:    Option[Long],
    isOwned:    Option[Boolean],
    clientData: ClientData
  ): Future[HandlerResult[ResponseCreateNewEventBus]] =
    authorized(clientData) { client ⇒
      for {
        (id, deviceId) ← ext.create(client.userId, client.authId, timeout, isOwned)
      } yield Ok(ResponseCreateNewEventBus(id, deviceId))
    }

  override def jhandleDisposeEventBus(id: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for (_ ← ext.dispose(client.userId, id)) yield Ok(ResponseVoid)
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }

  override def jhandlePostToEventBus(
    id:           String,
    destinations: IndexedSeq[ApiEventBusDestination],
    message:      Array[Byte],
    clientData:   ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← ext.post(client.userId, client.authId, id, destinations, message)
      } yield Ok(ResponseVoid)
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }

  override def jhandleKeepAliveEventBus(
    id:         String,
    timeout:    Option[Long],
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      ext.keepAlive(client.authId, id, timeout)
      FastFuture.successful(Ok(ResponseVoid))
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }

  override def jhandleJoinEventBus(
    id:         String,
    timeout:    Option[Long],
    clientData: ClientData
  ): Future[HandlerResult[ResponseJoinEventBus]] =
    authorized(clientData) { client ⇒
      for {
        deviceId ← ext.join(client.userId, client.authId, id, timeout)
      } yield Ok(ResponseJoinEventBus(deviceId))
    } recover {
      case EventBusErrors.EventBusNotFound ⇒ EventBusRpcErrors.EventBusNotFound
    }
}