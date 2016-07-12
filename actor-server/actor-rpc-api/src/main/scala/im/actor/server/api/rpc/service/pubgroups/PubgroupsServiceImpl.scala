package im.actor.server.api.rpc.service.pubgroups

import im.actor.server.group.GroupUtils
import im.actor.server.persist.{ AvatarDataRepo, GroupRepo, GroupUserRepo }

import scala.concurrent.{ ExecutionContext, Future }
import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._
import im.actor.api.rpc._
import im.actor.api.rpc.pubgroups.{ ApiPublicGroup, PubgroupsService, ResponseGetPublicGroups }
import im.actor.server.ApiConversions
import im.actor.server.db.DbExtension
import im.actor.server.file.ImageUtils
import im.actor.server.model.Group
import im.actor.server.persist.contact.UserContactRepo

class PubgroupsServiceImpl(implicit actorSystem: ActorSystem) extends PubgroupsService {
  import ApiConversions._
  import ImageUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def doHandleGetPublicGroups(clientData: ClientData): Future[HandlerResult[ResponseGetPublicGroups]] = {
    authorized(clientData) { implicit client ⇒
      val action = for {
        groups ← GroupRepo.findPublic
        pubGroupStructs ← DBIO.sequence(groups map (g ⇒ getPubgroupStruct(g, client.userId)))
        sorted = pubGroupStructs.sortWith((g1, g2) ⇒ g1.friendsCount >= g2.friendsCount && g1.membersCount >= g2.membersCount)
      } yield Ok(ResponseGetPublicGroups(sorted.toVector))
      DbExtension(actorSystem).db.run(action)
    }
  }

  def getPubgroupStruct(group: Group, userId: Int)(implicit ec: ExecutionContext): DBIO[ApiPublicGroup] = {
    for {
      membersIds ← GroupUserRepo.findUserIds(group.id)
      userContactsIds ← UserContactRepo.findNotDeletedIds(userId)
      friendsCount = (membersIds intersect userContactsIds).length
      groupAvatarModelOpt ← AvatarDataRepo.findByGroupId(group.id)
    } yield {
      ApiPublicGroup(group.id, group.accessHash, group.title, membersIds.length, friendsCount, group.about.getOrElse(""), groupAvatarModelOpt map getAvatar)
    }
  }

}

