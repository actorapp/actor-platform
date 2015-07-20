package im.actor.server.api.rpc.service.pubgroups

import im.actor.server.group.GroupOfficeRegion

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import com.amazonaws.services.s3.transfer.TransferManager
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.groups.{ ResponseCreateGroup, ResponseJoinGroup, ResponseJoinGroupDirect, GroupsService }
import im.actor.api.rpc.pubgroups.{ PublicGroup, ResponseGetPublicGroups, PubgroupsService }
import im.actor.server.api.rpc.service.groups.GroupInviteConfig
import im.actor.server.models
import im.actor.server.persist
import im.actor.server.presences.GroupPresenceManagerRegion
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.GroupUtils
import im.actor.server.util.GroupUtils.getPubgroupStructUnsafe

class PubgroupsServiceImpl(
  implicit
  db:          Database,
  actorSystem: ActorSystem
) extends PubgroupsService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleGetPublicGroups(clientData: ClientData): Future[HandlerResult[ResponseGetPublicGroups]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      for {
        groups ← persist.Group.findPublic
        pubGroupStructs ← DBIO.sequence(groups.view map getPubgroupStructUnsafe)
        sorted = pubGroupStructs.sortWith((g1, g2) ⇒ g1.friendsCount >= g2.friendsCount && g1.membersCount >= g2.membersCount)
      } yield Ok(ResponseGetPublicGroups(sorted.toVector))
    }

    db.run(toDBIOAction(authorizedAction))
  }
}

