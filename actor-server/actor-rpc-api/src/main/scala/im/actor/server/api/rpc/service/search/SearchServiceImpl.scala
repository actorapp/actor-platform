package im.actor.server.api.rpc.service.search

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.api.rpc.search._
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.api.rpc.users.ApiUser
import im.actor.concurrent.FutureExt
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.group.{ GroupExtension, GroupUtils }
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.persist.GroupRepo
import im.actor.server.user.UserExtension

import scala.concurrent.{ ExecutionContext, Future }

class SearchServiceImpl(implicit system: ActorSystem) extends SearchService {
  override implicit protected val ec: ExecutionContext = system.dispatcher

  protected val db = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected val groupExt = GroupExtension(system)

  override def doHandlePeerSearch(
    query:         IndexedSeq[ApiSearchCondition],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponsePeerSearch]] = {
    authorized(clientData) { implicit client ⇒
      val (peerTypes, texts) = query.foldLeft(Set.empty[ApiSearchPeerType.Value], Set.empty[String]) {
        case ((pts, txts), ApiSearchPieceText(t))          ⇒ (pts, txts + t)
        case ((pts, txts), ApiSearchPeerTypeCondition(pt)) ⇒ (pts + pt, txts)
        case ((pts, txts), _)                              ⇒ (pts, txts)
      }

      texts.toList match {
        case text :: Nil ⇒ searchResult(peerTypes.toVector, Some(text), optimizations)
        case Nil         ⇒ searchResult(peerTypes.toVector, None, optimizations)
        case _           ⇒ Future.successful(Error(RpcError(400, "INVALID_QUERY", "Invalid query.", canTryAgain = false, None)))
      }
    }
  }

  override def doHandleMessageSearch(
    query:         ApiSearchCondition,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseMessageSearchResponse]] =
    Future.successful(Error(CommonRpcErrors.NotSupportedInOss))

  override def doHandleMessageSearchMore(
    loadMoreState: Array[Byte],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseMessageSearchResponse]] =
    Future.successful(Error(CommonRpcErrors.NotSupportedInOss))

  private def searchResult(
    pts:           IndexedSeq[ApiSearchPeerType.Value],
    text:          Option[String],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value]
  )(implicit client: AuthorizedClientData): Future[HandlerResult[ResponsePeerSearch]] = {
    for {
      results ← FutureExt.ftraverse(pts)(search(_, text)).map(_.reduce(_ ++ _))
      (groupIds, userIds) = results.view.map(_.peer).foldLeft(Vector.empty[Int], Vector.empty[Int]) {
        case ((gids, uids), ApiPeer(pt, pid)) ⇒
          pt match {
            case ApiPeerType.Private ⇒ (gids, uids :+ pid)
            case ApiPeerType.Group   ⇒ (gids :+ pid, uids)
          }
      }
      (groups, users) ← GroupUtils.getGroupsUsers(groupIds, userIds, client.userId, client.authId)
    } yield {
      val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)
      Ok(ResponsePeerSearch(
        searchResults = results,
        users = if (stripEntities) Vector.empty else users.toVector,
        groups = if (stripEntities) Vector.empty else groups.toVector,
        userPeers = users.toVector map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)),
        groupPeers = groups.toVector map (g ⇒ ApiGroupOutPeer(g.id, g.accessHash))
      ))
    }
  }

  private def search(pt: ApiSearchPeerType.Value, text: Option[String])(implicit clientData: AuthorizedClientData): Future[IndexedSeq[ApiPeerSearchResult]] = {
    pt match {
      case ApiSearchPeerType.Contacts ⇒
        for {
          users ← searchContacts(text)
        } yield users map result
      case ApiSearchPeerType.Groups ⇒
        for {
          groups ← searchGroups(text)
        } yield groups map (result(_, isPublic = false))
      case ApiSearchPeerType.Public ⇒
        for {
          groups ← searchPublic(text)
        } yield groups map (result(_, isPublic = true))
    }
  }

  private def result(apiUser: ApiUser): ApiPeerSearchResult =
    ApiPeerSearchResult(
      peer = ApiPeer(ApiPeerType.Private, apiUser.id),
      title = apiUser.localName.getOrElse(apiUser.name),
      description = None,
      membersCount = None,
      dateCreated = None,
      creator = None,
      isPublic = None,
      isJoined = None
    )

  private def result(apiGroup: ApiGroup, isPublic: Boolean): ApiPeerSearchResult =
    ApiPeerSearchResult(
      peer = ApiPeer(ApiPeerType.Group, apiGroup.id),
      title = apiGroup.title,
      description = apiGroup.about,
      membersCount = Some(apiGroup.members.size),
      dateCreated = Some(apiGroup.createDate),
      creator = Some(apiGroup.creatorUserId),
      isPublic = Some(isPublic),
      isJoined = apiGroup.isMember
    )

  private def searchContacts(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[ApiUser]] = {
    for {
      userIds ← db.run(UserContactRepo.findContactIdsActive(client.userId))
      users ← FutureExt.ftraverse(userIds)(userExt.getApiStruct(_, client.userId, client.authId))
    } yield filterUsers(users.toVector, text)
  }

  private def filterUsers(users: IndexedSeq[ApiUser], textOpt: Option[String]): IndexedSeq[ApiUser] =
    textOpt match {
      case Some(text) ⇒
        val lotext = text.toLowerCase
        users filter { user ⇒
          user.name.toLowerCase.contains(lotext) ||
            user.localName.exists(_.toLowerCase.contains(lotext))
        }
      case None ⇒ users
    }

  // TODO: rewrite it using async
  private def searchGroups(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[ApiGroup]] = {
    for {
      ids ← DialogExtension(system).fetchGroupedDialogs(client.userId) map (_.filter(_.typ.isGroups).flatMap(_.dialogs.map(_.getPeer.id)))
      groupOpts ← FutureExt.ftraverse(ids) { id ⇒
        groupExt.isPublic(id) flatMap { isPublic ⇒
          if (isPublic) Future.successful(None)
          else groupExt.getApiStruct(id, client.userId).map(Some(_))
        }
      }
    } yield filterGroups(groupOpts.flatten.toVector, text)
  }

  private def searchPublic(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[ApiGroup]] = {
    for {
      groups ← db.run(GroupRepo.findPublic)
      groups ← FutureExt.ftraverse(groups)(g ⇒ groupExt.getApiStruct(g.id, client.userId))
    } yield filterGroups(groups.toVector, text)
  }

  private def filterGroups(groups: IndexedSeq[ApiGroup], textOpt: Option[String]): IndexedSeq[ApiGroup] = {
    textOpt match {
      case Some(text) ⇒
        groups.view.filter { group ⇒
          val lotext = text.toLowerCase
          group.title.toLowerCase.contains(lotext) ||
            group.about.exists(_.toLowerCase.contains(lotext)) ||
            group.theme.exists(_.toLowerCase.contains(lotext))
        }.force
      case None ⇒ groups
    }
  }
}