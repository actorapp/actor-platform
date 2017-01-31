package im.actor.server.api.rpc.service.survey

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import cats.data.Xor
import com.github.ghik.silencer.silent
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.{ResponseSeq, ResponseSeqDate, ResponseVoid}
import im.actor.api.rpc.peers.{ApiGroupOutPeer, ApiUserOutPeer}
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.api.rpc.surveys.{ApiSurvey, ApiSurveyAnswer, SurveysService}
import im.actor.api.rpc.users.ApiUser
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.file.{FileErrors, ImageUtils}
import im.actor.server.group._
import im.actor.server.model.GroupInviteToken
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.persist.{GroupInviteTokenRepo, GroupUserRepo}
import im.actor.server.presences.GroupPresenceExtension
import im.actor.server.user.UserExtension
import im.actor.util.ThreadLocalSecureRandom
import im.actor.util.misc.{IdUtils, StringUtils}
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}
/**
 * Created by 98379720172 on 16/11/16.
 */
final class SurveyServiceImpl()(implicit actorSystem: ActorSystem) extends SurveysService {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  /** Criar uma nova enquete */
  /** Criar uma nova enquete */
  override protected def doHandleCreateSurvey(survey: ApiSurvey, answers: IndexedSeq[ApiSurveyAnswer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    FastFuture.successful(Ok(ResponseVoid))
  }
}
