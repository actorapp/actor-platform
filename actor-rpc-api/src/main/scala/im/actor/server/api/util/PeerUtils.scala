package im.actor.server.api.util

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scalaz._

import akka.actor._
import slick.dbio.DBIO

import im.actor.api.rpc._
import im.actor.api.rpc.peers._
import im.actor.server.util.ACLUtils
import im.actor.server.{ models, persist }

object PeerUtils {
  def withOutPeer[R <: RpcResponse](
    clientUserId: Int,
    outPeer:      OutPeer
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
          optGroup ← persist.Group.find(outPeer.id).headOption
          grouperrOrGroup ← validGroup(optGroup)
          usererrOrGroup ← validateGroupAccess(optGroup, client.userId)(ec)
          hasherrOrGroup ← DBIO.successful(usererrOrGroup.map(validGroupAccessHash(outPeer.accessHash, _)))
        } yield hasherrOrGroup).flatMap {
          case Error(err) ⇒ DBIO.successful(Error(err))
          case _          ⇒ f
        }
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

  def withGroupOutPeer[R <: RpcResponse](groupOutPeer: GroupOutPeer)(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext): DBIO[RpcError \/ R] = {
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

  def withOwnGroupMember[R <: RpcResponse](groupOutPeer: GroupOutPeer, userId: Int)(f: models.FullGroup ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext): DBIO[RpcError \/ R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      (for (user ← persist.GroupUser.find(group.id, userId)) yield user).flatMap {
        case Some(user) ⇒ f(group)
        case None ⇒ {
          DBIO.successful(Error(CommonErrors.UserNotAuthorized))
        }
      }
    }
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
        case Some(models.GroupUser(_, _, inviterUserId, _)) ⇒
          if (kickUserOutPeer.userId != client.userId && (inviterUserId == client.userId || group.creatorUserId == client.userId)) {
            f(group)
          } else {
            DBIO.successful(Error(RpcError(403, "NO_PERMISSION", "You are permitted to kick this user.", false, None)))
          }
        case None ⇒ DBIO.successful(Error(RpcError(404, "USER_NOT_FOUND", "User is not a group member.", false, None)))
      }
    }
  }

  private def checkUserPeer(userId: Int, accessHash: Long)(implicit
    client: AuthorizedClientData,
                                                           actorSystem: ActorSystem,
                                                           ec:          ExecutionContext): DBIO[Option[Boolean]] = {
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
        case None ⇒ {
          DBIO.successful(Error(CommonErrors.UserNotAuthorized))
        }
      }
    case None ⇒ DBIO.successful(Error(CommonErrors.GroupNotFound))
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
