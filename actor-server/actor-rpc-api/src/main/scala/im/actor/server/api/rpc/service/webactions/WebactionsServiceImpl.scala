package im.actor.server.api.rpc.service.webactions

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.FutureResultRpc._
import im.actor.api.rpc._
import im.actor.api.rpc.collections.ApiMapValue
import im.actor.api.rpc.webactions.{ ResponseCompleteWebaction, ResponseInitWebaction, WebactionsService }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.webactions.Webaction
import shardakka.ShardakkaExtension
import slick.dbio.DBIO

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scalaz.{ \/, \/- }

object WebactionsErrors {
  val WebactionNotFound = RpcError(404, "WEBACTION_NOT_FOUND", "Web action not found", false, None)
  val FailedToCreateWebaction = RpcError(500, "FAILED_TO_CREATE_WEBACTION", "Failed to create webaction", true, None)
  val WrongActionHash = RpcError(400, "WRONG_WEBACTION_HASH", "Web action not found", false, None)
  def actionFailed(message: String) = RpcError(500, "WEBACTION_FAILED", message, false, None)
}

private[rpc] object WebactionsKeyValues {
  def actionHashUserKV()(implicit system: ActorSystem) = ShardakkaExtension(system).simpleKeyValue("WebactionHashUserId")
}

final class WebactionsServiceImpl(implicit actorSystem: ActorSystem) extends WebactionsService {
  import WebactionsErrors._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = Timeout(5.seconds)
  private val actionHashUserKV = WebactionsKeyValues.actionHashUserKV()
  private val db = DbExtension(actorSystem).db

  override def doHandleInitWebaction(actionName: String, params: ApiMapValue, clientData: ClientData): Future[HandlerResult[ResponseInitWebaction]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      (for {
        fqn ← fromOption(WebactionNotFound)(Webaction.list.get(actionName))
        webAction ← fromEither(createWebaction(fqn))
        actionHash = generateActionHash()
        _ ← fromFuture(actionHashUserKV.upsert(actionHash, actionName))
      } yield ResponseInitWebaction(webAction.uri(params), webAction.regex, actionHash)).run
    }
    db.run(toDBIOAction(authorizedAction map DBIO.from))
  }

  override def doHandleCompleteWebaction(actionHash: String, completeUri: String, clientData: ClientData): Future[HandlerResult[ResponseCompleteWebaction]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      (for {
        actionName ← fromFutureOption(WrongActionHash)(actionHashUserKV.get(actionHash))
        fqn ← fromOption(WebactionNotFound)(Webaction.list.get(actionName))
        webAction ← fromEither(createWebaction(fqn))
        response ← fromFuture(webAction.complete(client.userId, completeUri))
        _ ← fromBoolean(actionFailed(response.content.toString))(response.isSuccess)
        _ ← fromFuture(actionHashUserKV.delete(actionHash))
      } yield ResponseCompleteWebaction(response.content)).run
    }
    db.run(toDBIOAction(authorizedAction map DBIO.from))
  }

  private def createWebaction(fqn: String): RpcError \/ Webaction = {
    Webaction.webactionOf(fqn, actorSystem) match {
      case Success(s) ⇒ \/-(s)
      case Failure(f) ⇒
        actorSystem.log.error(f, "Failed to create webaction")
        Error(FailedToCreateWebaction)
    }
  }

  private def generateActionHash(): String = ACLUtils.authTransactionHash(ACLUtils.nextAccessSalt())

}
