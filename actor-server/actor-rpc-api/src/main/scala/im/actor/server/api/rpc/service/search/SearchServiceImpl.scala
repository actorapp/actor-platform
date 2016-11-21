package im.actor.server.api.rpc.service.search

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
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
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.user.UserExtension

import scala.concurrent.{ ExecutionContext, Future }

class SearchServiceImpl(implicit system: ActorSystem) extends SearchService {
  override implicit protected val ec: ExecutionContext = system.dispatcher

  protected val db = DbExtension(system).db

  private val userExt = UserExtension(system)
  private val groupExt = GroupExtension(system)
  private val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

  private val EmptyResult = ResponsePeerSearch(Vector.empty, Vector.empty, Vector.empty, Vector.empty, Vector.empty)

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
      val peerTypesSorted = peerTypes.toVector.sortBy(_.id)

      texts.toList match {
        case text :: Nil if text.length < 3 ⇒
          FastFuture.successful(Ok(EmptyResult))
        case text :: Nil ⇒
          val tps = if (peerTypes.isEmpty)
            Vector(ApiSearchPeerType.Groups, ApiSearchPeerType.Contacts, ApiSearchPeerType.Public)
          else
            peerTypesSorted
          searchResult(tps, Some(text), optimizations)
        case Nil ⇒ searchResult(peerTypesSorted, None, optimizations)
        case _   ⇒ FastFuture.successful(Error(RpcError(400, "INVALID_QUERY", "Invalid query.", canTryAgain = false, None)))
      }
    }
  }

  override def doHandleMessageSearch(
    query:         ApiSearchCondition,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseMessageSearchResponse]] =
    FastFuture.successful(Error(CommonRpcErrors.NotSupportedInOss))

  override def doHandleMessageSearchMore(
    loadMoreState: Array[Byte],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseMessageSearchResponse]] =
    FastFuture.successful(Error(CommonRpcErrors.NotSupportedInOss))

  private def searchResult(
    pts:           IndexedSeq[ApiSearchPeerType.Value],
    text:          Option[String],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value]
  )(implicit client: AuthorizedClientData): Future[HandlerResult[ResponsePeerSearch]] = {
    val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)
    val loadGroupMembers = !optimizations.contains(ApiUpdateOptimization.GROUPS_V2)

    for {
      results ← FutureExt.ftraverse(pts)(search(_, text)).map(_.reduce(_ ++ _))
      (groupIds, userIds, searchResults) = (results foldLeft (Set.empty[Int], Set.empty[Int], Vector.empty[ApiPeerSearchResult])) {
        case (acc @ (gids, uids, rslts), found @ ApiPeerSearchResult(peer, _)) ⇒
          if (rslts.exists(_.peer == peer)) {
            acc
          } else {
            peer.`type` match {
              case ApiPeerType.Private ⇒ (gids, uids + peer.id, rslts :+ found)
              case ApiPeerType.Group   ⇒ (gids + peer.id, uids, rslts :+ found)
            }
          }
      }
      ((users, userPeers), (groups, groupPeers)) ← EntitiesHelpers.usersAndGroupsByIds(groupIds, userIds, stripEntities, loadGroupMembers)
    } yield {

      Ok(ResponsePeerSearch(
        searchResults = searchResults,
        users = users,
        groups = groups,
        userPeers = userPeers,
        groupPeers = groupPeers
      ))
    }
  }

  type PeerAndMatchString = (ApiPeer, String)

  private def search(pt: ApiSearchPeerType.Value, text: Option[String])(implicit clientData: AuthorizedClientData): Future[IndexedSeq[ApiPeerSearchResult]] = {
    pt match {
      case ApiSearchPeerType.Contacts ⇒
        for {
          users ← searchContacts(text)
        } yield users map result
      case ApiSearchPeerType.Groups ⇒
        for {
          groups ← searchLocalGroups(text)
        } yield groups map result
      case ApiSearchPeerType.Public ⇒
        val usersFull = searchGlobalUsers(text)
        val usersPrefix = searchGlobalUsersPrefix(text)
        val groupsPrefix = searchGlobalGroupsPrefix(text)
        for {
          uf ← usersFull
          up ← usersPrefix
          gp ← groupsPrefix
        } yield (uf map result) ++ (up map result) ++ (gp map result)
    }
  }

  private def result(peer: ApiPeer): ApiPeerSearchResult =
    ApiPeerSearchResult(
      peer = peer,
      optMatchString = None
    )

  private def result(peerAndMatch: PeerAndMatchString): ApiPeerSearchResult =
    ApiPeerSearchResult(
      peer = peerAndMatch._1,
      optMatchString = Some(peerAndMatch._2)
    )

  private def userPeersWithoutSelf(userIds: Seq[Int])(implicit client: AuthorizedClientData): Vector[ApiPeer] =
    (userIds collect {
      case userId if userId != client.userId ⇒ ApiPeer(ApiPeerType.Private, userId)
    }).toVector

  // search users by full phone number, email or nickname
  private def searchGlobalUsers(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[ApiPeer]] = {
    text map { query ⇒
      userExt.findUserIds(query) map userPeersWithoutSelf
    } getOrElse FastFuture.successful(Vector.empty)
  }

  // search users by nickname prefix
  private def searchGlobalUsersPrefix(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[PeerAndMatchString]] = {
    text map { query ⇒
      globalNamesStorage.userIdsByPrefix(normName(query)) map { results ⇒
        results collect {
          case (userId, nickName) if userId != client.userId ⇒
            ApiPeer(ApiPeerType.Private, userId) → s"@$nickName"
        }
      }
    } getOrElse FastFuture.successful(Vector.empty)
  }

  // find groups by global name prefix
  private def searchGlobalGroupsPrefix(text: Option[String]): Future[IndexedSeq[PeerAndMatchString]] = {
    text map { query ⇒
      globalNamesStorage.groupIdsByPrefix(normName(query)) map { results ⇒
        results map {
          case (groupId, globalName) ⇒
            ApiPeer(ApiPeerType.Group, groupId) → s"@$globalName"
        }
      }
    } getOrElse FastFuture.successful(Vector.empty)
  }

  private def normName(n: String) = if (n.startsWith("@")) n.drop(1) else n

  private def searchContacts(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[ApiPeer]] = {
    for {
      userIds ← db.run(UserContactRepo.findContactIdsActive(client.userId))
      users ← FutureExt.ftraverse(userIds)(userExt.getApiStruct(_, client.userId, client.authId))
    } yield filterUsers(users.toVector, text)
  }

  private def filterUsers(users: IndexedSeq[ApiUser], textOpt: Option[String]): IndexedSeq[ApiPeer] =
    textOpt match {
      case Some(text) ⇒
        val lotext = text.toLowerCase
        users filter { user ⇒
          user.name.toLowerCase.contains(lotext) ||
            user.localName.exists(_.toLowerCase.contains(lotext))
        } map { u ⇒
          ApiPeer(ApiPeerType.Private, u.id)
        }
      case None ⇒ users map { u ⇒ ApiPeer(ApiPeerType.Private, u.id) }
    }

  private def searchLocalGroups(text: Option[String])(implicit client: AuthorizedClientData): Future[IndexedSeq[ApiPeer]] = {
    for {
      ids ← DialogExtension(system).fetchGroupedDialogs(client.userId) map (_.filter(_.typ.isGroups).flatMap(_.dialogs.map(_.getPeer.id)))
      groups ← FutureExt.ftraverse(ids) { id ⇒
        groupExt.getApiStruct(id, client.userId)
      }
    } yield filterGroups(groups.toVector, text)
  }

  private def filterGroups(groups: IndexedSeq[ApiGroup], textOpt: Option[String]): IndexedSeq[ApiPeer] = {
    textOpt match {
      case Some(text) ⇒
        groups filter { group ⇒
          val lotext = text.toLowerCase
          group.title.toLowerCase.contains(lotext) ||
            group.about.exists(_.toLowerCase.contains(lotext)) ||
            group.theme.exists(_.toLowerCase.contains(lotext))
        } map { g ⇒
          ApiPeer(ApiPeerType.Group, g.id)
        }
      case None ⇒ groups map { g ⇒ ApiPeer(ApiPeerType.Group, g.id) }
    }
  }
}
