package im.actor.server.api.rpc.service.configs

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc._
import im.actor.api.rpc.configs.{ ApiParameter, ConfigsService, ResponseGetParameters }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server.sequence.SeqState
import im.actor.server.userconfig.UserConfigExtension

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

final class ConfigsServiceImpl(implicit system: ActorSystem) extends ConfigsService {
  override implicit protected val ec: ExecutionContext = system.dispatcher
  private implicit val timeout = Timeout(10.seconds)
  private val configExt = UserConfigExtension(system)

  override def doHandleEditParameter(rawKey: String, value: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← configExt.editParameter(client.userId, rawKey, value)
        _ ← configExt.hooks.runAll(client.userId, rawKey, value)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

  override def doHandleGetParameters(clientData: ClientData): Future[HandlerResult[ResponseGetParameters]] =
    authorized(clientData) { implicit client ⇒
      for {
        params ← configExt.fetchParameters(client.userId)
      } yield {
        val paramsStructs = params map {
          case (key, value) ⇒
            ApiParameter(key, value.getOrElse(""))
        }
        Ok(ResponseGetParameters(paramsStructs.toVector))
      }
    }
}
