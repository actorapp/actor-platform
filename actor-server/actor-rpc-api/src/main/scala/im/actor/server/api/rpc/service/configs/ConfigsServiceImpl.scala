package im.actor.server.api.rpc.service.configs

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc._
import im.actor.api.rpc.configs.{ ApiParameter, ConfigsService, ResponseGetParameters, UpdateParameterChanged }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server.db.DbExtension
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserExtension
import im.actor.server.{ models, persist }
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

final class ConfigsServiceImpl(implicit actorSystem: ActorSystem) extends ConfigsService {
  private val db: Database = DbExtension(actorSystem).db

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val timeout = Timeout(10.seconds)

  override def jhandleEditParameter(rawKey: String, rawValue: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val key = rawKey.trim
      val value =
        rawValue match {
          case "" ⇒ None
          case s  ⇒ Some(s)
        }

      val update = UpdateParameterChanged(key, value)

      for {
        _ ← persist.configs.Parameter.createOrUpdate(models.configs.Parameter(client.userId, key, value))
        SeqState(seq, state) ← DBIO.from(UserExtension(actorSystem).broadcastClientUpdate(update, None, isFat = false))
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetParameters(clientData: ClientData): Future[HandlerResult[ResponseGetParameters]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        params ← persist.configs.Parameter.find(client.userId)
      } yield {
        val paramsStructs = params map { param ⇒
          ApiParameter(param.key, param.value.getOrElse(""))
        }

        Ok(ResponseGetParameters(paramsStructs.toVector))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }
}
