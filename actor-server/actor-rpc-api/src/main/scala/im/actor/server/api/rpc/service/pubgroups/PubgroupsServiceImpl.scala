package im.actor.server.api.rpc.service.pubgroups

import im.actor.server.group.GroupUtils
import im.actor.server.persist.GroupRepo

import scala.concurrent.{ ExecutionContext, Future }
import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._
import im.actor.api.rpc._
import im.actor.api.rpc.pubgroups.{ PubgroupsService, ResponseGetPublicGroups }
import GroupUtils.getPubgroupStructUnsafe
import im.actor.server.db.DbExtension

class PubgroupsServiceImpl(implicit actorSystem: ActorSystem) extends PubgroupsService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def doHandleGetPublicGroups(clientData: ClientData): Future[HandlerResult[ResponseGetPublicGroups]] = {
    authorized(clientData) { implicit client ⇒
      val action = for {
        groups ← GroupRepo.findPublic
        pubGroupStructs ← DBIO.sequence(groups.view map getPubgroupStructUnsafe)
        sorted = pubGroupStructs.sortWith((g1, g2) ⇒ g1.friendsCount >= g2.friendsCount && g1.membersCount >= g2.membersCount)
      } yield Ok(ResponseGetPublicGroups(sorted.toVector))
      DbExtension(actorSystem).db.run(action)
    }
  }
}

