package im.actor.server.api.rpc.service.groupspre

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc._
import im.actor.api.rpc.groupspre.{GroupspreService, ResponseCreateGroupPre, ResponseLoadGroupsPre}
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.surveys.{ApiSurvey, ApiSurveyAnswer, SurveysService}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by 98379720172 on 16/11/16.
 */
final class GroupsPreServiceImpl()(implicit actorSystem: ActorSystem) extends GroupspreService {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  /** Main LoadGroups */
  override protected def doHandleLoadGroupsPre(groupFatherId: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadGroupsPre]] = {
    FastFuture.successful(Ok(ResponseVoid))
  }

  /** fasdfasdfa */
  override protected def doHandleCreateGroupPre(groupId: Int, groupFatherId: Int, clientData: ClientData): Future[HandlerResult[ResponseCreateGroupPre]] = {
    FastFuture.successful(Ok(ResponseVoid))
  }
}
