package im.actor.server.api.rpc.service.configs

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.configs.{ ConfigsService, Parameter, ResponseGetParameters, UpdateParameterChanged }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server.{ models, persist }
import im.actor.server.push.SeqUpdatesManagerRegion

class ConfigsServiceImpl(implicit seqUpdManagerRegion: SeqUpdatesManagerRegion, db: Database, actorSystem: ActorSystem) extends ConfigsService {

  import im.actor.server.push.SeqUpdatesManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

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
        seqstate ← broadcastClientUpdate(update, None)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
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
