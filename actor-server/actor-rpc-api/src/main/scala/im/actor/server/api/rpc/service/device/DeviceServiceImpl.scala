package im.actor.server.api.rpc.service.device

import akka.actor.ActorSystem
import im.actor.api.rpc.{ ClientData, _ }
import im.actor.api.rpc.device.DeviceService
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.server.auth.DeviceInfo
import im.actor.server.user.UserExtension

import scala.concurrent.{ ExecutionContext, Future }

final class DeviceServiceImpl(implicit system: ActorSystem) extends DeviceService {
  override implicit protected val ec: ExecutionContext = system.dispatcher

  private val userExt = UserExtension(system)

  override def doHandleNotifyAboutDeviceInfo(preferredLanguages: IndexedSeq[String], timeZone: Option[String], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      userExt.setDeviceInfo(client.userId, DeviceInfo(timeZone.getOrElse(""), preferredLanguages))
      Future.successful(Ok(ResponseVoid))
    }
  }
}