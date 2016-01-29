package im.actor.server.api.rpc.service.pubgroups

import im.actor.server.group.GroupUtils
import im.actor.server.persist.GroupRepo

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.pubgroups.{ PubgroupsService, ResponseGetPublicGroups }
import GroupUtils.getPubgroupStructUnsafe

class PubgroupsServiceImpl(
  implicit
  db:          Database,
  actorSystem: ActorSystem
) extends PubgroupsService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleGetPublicGroups(clientData: ClientData): Future[HandlerResult[ResponseGetPublicGroups]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      for {
        groups ← GroupRepo.findPublic
        pubGroupStructs ← DBIO.sequence(groups.view map getPubgroupStructUnsafe)
        sorted = pubGroupStructs.sortWith((g1, g2) ⇒ g1.friendsCount >= g2.friendsCount && g1.membersCount >= g2.membersCount)
      } yield Ok(ResponseGetPublicGroups(sorted.toVector))
    }

    db.run(toDBIOAction(authorizedAction))
  }
}

