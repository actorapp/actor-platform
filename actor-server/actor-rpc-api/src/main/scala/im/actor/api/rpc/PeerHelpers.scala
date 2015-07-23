package im.actor.api.rpc

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scalaz._

import akka.actor._
import slick.dbio.DBIO

import im.actor.api.rpc.peers._
import im.actor.server.api.rpc.service.groups.GroupRpcErrors
import im.actor.server.util.{ ACLUtils, StringUtils }
import im.actor.server.{ models, persist }

object PeerHelpers {
  def withOutPeer[R <: RpcResponse](
    outPeer: OutPeer
  )(
    f: ⇒ DBIO[RpcError \/ R]
  )(implicit client: AuthorizedClientData, actorSystem: ActorSystem, ec: ExecutionContext): DBIO[RpcError \/ R] = {
    outPeer.`type` match {
      case PeerType.Private ⇒
        (for {
          optUser ← persist.User.find(outPeer.id).headOption
          usererrOrUser ← validUser(optUser)
          hasherrOrUser ← DBIO.successful(usererrOrUser.map(validUserAccessHash(outPeer.accessHash, _)))
        } yield hasherrOrUser).flatMap {
          case Error(err) ⇒ DBIO.successful(Error(err))
          case _          ⇒ f
        }
      case PeerType.Group ⇒
        (for {
          optGroup ← persist.Group.find(outPeer.id)
          grouperrOrGroup ← validGroup(optGroup)
          usererrOrGroup ← validateGroupAccess(optGroup, client.userId)(ec)
          hasherrOrGroup ← DBIO.successful(usererrOrGroup.map(validGroupAccessHash(outPeer.accessHash, _)))
        } yield hasherrOrGroup).flatMap {
          case Error(err) ⇒ DBIO.successful(Error(err))
          case _          ⇒ f
        }
    }
  }

  def withOutPeerAsGroupPeer[R <: RpcResponse](outPeer: OutPeer)(
    f: GroupOutPeer ⇒ DBIO[RpcError \/ R]
  )(implicit client: AuthorizedClientData, actorSystem: ActorSystem, ec: ExecutionContext): DBIO[RpcError \/ R] = {
    outPeer.`type` match {
      case PeerType.Group   ⇒ f(GroupOutPeer(outPeer.id, outPeer.accessHash))
      case PeerType.Private ⇒ DBIO.successful(Error(RpcError(403, "PEER_IS_NOT_GROUP", "", false, None)))
    }
  }

  def withUserOutPeer[R <: RpcResponse](userOutPeer: UserOutPeer)(f: ⇒ DBIO[RpcError \/ R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError \/ R] = {
    renderCheckResult(Seq(checkUserPeer(userOutPeer.userId, userOutPeer.accessHash)), f)
  }

  def withOwnGroupMember[R <: RpcResponse](groupOutPeer: GroupOutPeer, userId: Int)(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext): DBIO[RpcError \/ R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      (for (user ← persist.GroupUser.find(group.id, userId)) yield user).flatMap {
        case Some(user) ⇒ f(group)
        case None       ⇒ DBIO.successful(Error(CommonErrors.forbidden("You are not a group member.")))
      }
    }
  }

  def withGroupAdmin[R <: RpcResponse](groupOutPeer: GroupOutPeer)(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(implicit client: AuthorizedClientData, ec: ExecutionContext): DBIO[RpcError \/ R] = {
    withOwnGroupMember(groupOutPeer, client.userId) { group ⇒
      if (group.creatorUserId == client.userId)
        f(group)
      else
        DBIO.successful(Error(CommonErrors.forbidden("Only admin can perform this action.")))
    }
  }

  def withValidGroupTitle[R <: RpcResponse](title: String)(f: String ⇒ DBIO[RpcError \/ R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError \/ R] = StringUtils.validName(title) match {
    case -\/(err)        ⇒ DBIO.successful(Error(GroupRpcErrors.WrongGroupTitle))
    case \/-(validTitle) ⇒ f(validTitle)
  }

  def withUserOutPeers[R <: RpcResponse](userOutPeers: immutable.Seq[UserOutPeer])(f: ⇒ DBIO[RpcError \/ R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError \/ R] = {
    val checkOptsFutures = userOutPeers map {
      case UserOutPeer(userId, accessHash) ⇒
        checkUserPeer(userId, accessHash)
    }

    renderCheckResult(checkOptsFutures, f)
  }

  val InvalidToken = RpcError(403, "INVALID_INVITE_TOKEN", "No correct token provided.", false, None)

  def withValidInviteToken[R <: RpcResponse](baseUrl: String, urlOrToken: String)(f: (models.FullGroup, models.GroupInviteToken) ⇒ DBIO[RpcError \/ R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError \/ R] = {
    val extractedToken =
      if (urlOrToken.startsWith(baseUrl)) {
        urlOrToken.drop(genInviteUrl(baseUrl).length).takeWhile(c ⇒ c != '?' && c != '#')
      } else {
        urlOrToken
      }

    extractedToken.isEmpty match {
      case false ⇒ (for {
        token ← persist.GroupInviteToken.findByToken(extractedToken)
        group ← token.map(gt ⇒ persist.Group.findFull(gt.groupId).headOption).getOrElse(DBIO.successful(None))
      } yield for (g ← group; t ← token) yield (g, t)).flatMap {
        case Some((g, t)) ⇒ f(g, t)
        case None         ⇒ DBIO.successful(Error(InvalidToken))
      }
      case true ⇒ DBIO.successful(Error(InvalidToken))
    }
  }

  def withKickableGroupMember[R <: RpcResponse](
    groupOutPeer:    GroupOutPeer,
    kickUserOutPeer: UserOutPeer
  )(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError \/ R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      persist.GroupUser.find(group.id, kickUserOutPeer.userId).flatMap {
        case Some(models.GroupUser(_, _, inviterUserId, _, _)) ⇒
          if (kickUserOutPeer.userId != client.userId && (inviterUserId == client.userId || group.creatorUserId == client.userId)) {
            f(group)
          } else {
            DBIO.successful(Error(CommonErrors.forbidden("You are permitted to kick this user.")))
          }
        case None ⇒ DBIO.successful(Error(RpcError(404, "USER_NOT_FOUND", "User is not a group member.", false, None)))
      }
    }
  }

  def withPublicGroup[R <: RpcResponse](groupOutPeer: GroupOutPeer)(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError \/ R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      if (group.isPublic) {
        f(group)
      } else {
        DBIO.successful(Error(RpcError(400, "GROUP_IS_NOT_PUBLIC", "The group is not public.", false, None)))
      }
    }
  }

  def genInviteUrl(baseUrl: String, token: String = "") = s"$baseUrl/join/$token"

  private def checkUserPeer(userId: Int, accessHash: Long)(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[Option[Boolean]] = {
    for {
      userOpt ← persist.User.find(userId).headOption
    } yield {
      userOpt map (u ⇒ ACLUtils.userAccessHash(client.authId, u.id, u.accessSalt) == accessHash)
    }
  }

  private def validUser(optUser: Option[models.User]) = {
    optUser match {
      case Some(user) ⇒
        DBIO.successful(\/-(user))
      case None ⇒ DBIO.successful(Error(CommonErrors.UserNotFound))
    }
  }

  def validateGroupAccess(optGroup: Option[models.Group], userId: Int)(implicit ec: ExecutionContext) = optGroup match {
    case Some(group) ⇒
      (for (user ← persist.GroupUser.find(group.id, userId)) yield user).flatMap {
        case Some(user) ⇒ DBIO.successful(\/-(group))
        case None ⇒
          (for (bot ← persist.GroupBot.find(group.id, userId)) yield bot).flatMap {
            case Some(bot) ⇒ DBIO.successful(\/-(group))
            case None      ⇒ DBIO.successful(Error(CommonErrors.forbidden("No access to the group.")))
          }
      }
    case None ⇒ DBIO.successful(Error(CommonErrors.GroupNotFound))
  }

  private def withGroupOutPeer[R <: RpcResponse](groupOutPeer: GroupOutPeer)(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext): DBIO[RpcError \/ R] = {
    persist.Group.findFull(groupOutPeer.groupId).headOption flatMap {
      case Some(group) ⇒
        if (group.accessHash != groupOutPeer.accessHash) {
          DBIO.successful(Error(CommonErrors.InvalidAccessHash))
        } else {
          f(group)
        }
      case None ⇒
        DBIO.successful(Error(CommonErrors.GroupNotFound))
    }
  }

  private def validGroup(optGroup: Option[models.Group]) = {
    optGroup match {
      case Some(group) ⇒
        DBIO.successful(\/-(group))
      case None ⇒ DBIO.successful(Error(CommonErrors.GroupNotFound))
    }
  }

  private def validUserAccessHash(accessHash: Long, user: models.User)(implicit client: BaseClientData, actorSystem: ActorSystem) = {
    if (accessHash == ACLUtils.userAccessHash(client.authId, user)) {
      \/-(user)
    } else {
      Error(CommonErrors.InvalidAccessHash)
    }
  }

  private def validGroupAccessHash(accessHash: Long, group: models.Group)(implicit client: BaseClientData, actorSystem: ActorSystem) = {
    if (accessHash == group.accessHash) {
      \/-(group)
    } else {
      Error(CommonErrors.InvalidAccessHash)
    }
  }

  private def renderCheckResult[R <: RpcResponse](checkOptsActions: Seq[DBIO[Option[Boolean]]], f: ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext): DBIO[RpcError \/ R] = {
    DBIO.sequence(checkOptsActions) flatMap { checkOpts ⇒
      if (checkOpts.contains(None)) {
        DBIO.successful(Error(RpcError(404, "PEER_NOT_FOUND", "Peer not found.", false, None)))
      } else if (checkOpts.flatten.contains(false)) {
        DBIO.successful(Error(RpcError(401, "ACCESS_HASH_INVALID", "Invalid access hash.", false, None)))
      } else {
        f
      }
    }
  }
}
