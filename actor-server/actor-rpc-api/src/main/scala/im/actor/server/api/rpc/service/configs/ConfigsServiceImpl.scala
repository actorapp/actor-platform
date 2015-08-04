package im.actor.server.api.rpc.service.configs

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.configs.{ ConfigsService, Parameter, ResponseGetParameters, UpdateParameterChanged }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server.sequence.SeqState
import im.actor.server.user.{ UserOffice, UserViewRegion }
import im.actor.server.{ models, persist }
import im.actor.server.push.SeqUpdatesManagerRegion

final class ConfigsServiceImpl(implicit userViewRegion: UserViewRegion, seqUpdManagerRegion: SeqUpdatesManagerRegion, db: Database, actorSystem: ActorSystem) extends ConfigsService {

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
        SeqState(seq, state) ← DBIO.from(UserOffice.broadcastClientUpdate(update, None, isFat = false))
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
          Parameter(param.key, param.value.getOrElse(""))
        }

        Ok(ResponseGetParameters(paramsStructs.toVector))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }
}
