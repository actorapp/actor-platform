package im.actor.api.rpc

import akka.actor._
import cats.data.Xor
import im.actor.api.rpc.CommonRpcErrors.InvalidAccessHash
import im.actor.api.rpc.peers._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.GroupRpcErrors
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupErrors.GroupNotFound
import im.actor.server.group.GroupExtension
import im.actor.server.model._
import im.actor.server.persist._
import im.actor.server.user.UserErrors.UserNotFound
import im.actor.util.misc.StringUtils
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object PeerHelpers {

  def withOutPeer[R <: RpcResponse](outPeer: ApiOutPeer)(f: ⇒ Future[RpcError Xor R])(
    implicit
    client: AuthorizedClientData,
    system: ActorSystem
  ): Future[RpcError Xor R] = {
    import FutureResultRpc._
    import system.dispatcher
    val action: Result[R] = for {
      valid ← fromFuture(handleNotFound)(ACLUtils.checkOutPeer(outPeer, client.authId))
      result ← if (valid) fromFutureXor(f) else fromXor(Xor.left(InvalidAccessHash))
    } yield result
    action.value
  }

  private def handleNotFound: PartialFunction[Throwable, RpcError] = {
    case _: UserNotFound  ⇒ CommonRpcErrors.UserNotFound
    case _: GroupNotFound ⇒ CommonRpcErrors.GroupNotFound
    case e                ⇒ throw e
  }

  def withOutPeerDBIO[R <: RpcResponse](outPeer: ApiOutPeer)(f: ⇒ DBIO[RpcError Xor R])(
    implicit
    client: AuthorizedClientData,
    system: ActorSystem
  ): DBIO[RpcError Xor R] =
    DBIO.from(withOutPeer(outPeer)(DbExtension(system).db.run(f)))

  def withOutPeerAsGroupPeer[R <: RpcResponse](outPeer: ApiOutPeer)(
    f: ApiGroupOutPeer ⇒ DBIO[RpcError Xor R]
  )(implicit client: AuthorizedClientData, actorSystem: ActorSystem, ec: ExecutionContext): DBIO[RpcError Xor R] = {
    outPeer.`type` match {
      case ApiPeerType.Group   ⇒ f(ApiGroupOutPeer(outPeer.id, outPeer.accessHash))
      case ApiPeerType.Private ⇒ DBIO.successful(Error(RpcError(403, "PEER_IS_NOT_GROUP", "", false, None)))
    }
  }

  def withUserOutPeerF[R <: RpcResponse](userOutPeer: ApiUserOutPeer)(f: ⇒ Future[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): Future[RpcError Xor R] =
    DbExtension(actorSystem).db.run(withUserOutPeer(userOutPeer)(DBIO.from(f)))

  def withUserOutPeer[R <: RpcResponse](userOutPeer: ApiUserOutPeer)(f: ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = {
    renderCheckResult(Seq(checkUserPeer(userOutPeer.userId, userOutPeer.accessHash)), f)
  }

  def withOwnGroupMember[R <: RpcResponse](groupOutPeer: ApiGroupOutPeer, userId: Int)(f: FullGroup ⇒ DBIO[RpcError Xor R])(implicit ec: ExecutionContext): DBIO[RpcError Xor R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      (for (user ← GroupUserRepo.find(group.id, userId)) yield user).flatMap {
        case Some(user) ⇒ f(group)
        case None       ⇒ DBIO.successful(Error(CommonRpcErrors.forbidden("You are not a group member.")))
      }
    }
  }

  def withValidGroupTitle[R <: RpcResponse](title: String)(f: String ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = StringUtils.validName(title) match {
    case Xor.Left(err)         ⇒ DBIO.successful(Error(GroupRpcErrors.WrongGroupTitle))
    case Xor.Right(validTitle) ⇒ f(validTitle)
  }

  def withUserOutPeers[R <: RpcResponse](userOutPeers: Seq[ApiUserOutPeer])(f: ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = {
    val checkOptsFutures = userOutPeers map {
      case ApiUserOutPeer(userId, accessHash) ⇒
        checkUserPeer(userId, accessHash)
    }

    renderCheckResult(checkOptsFutures, f)
  }

  def withUserOutPeersF[R <: RpcResponse](userOutPeers: Seq[ApiUserOutPeer])(f: ⇒ Future[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): Future[RpcError Xor R] =
    DbExtension(actorSystem).db.run(withUserOutPeers(userOutPeers)(DBIO.from(f)))

  val InvalidToken = RpcError(403, "INVALID_INVITE_TOKEN", "No correct token provided.", false, None)

  def withValidInviteToken[R <: RpcResponse](baseUrl: String, urlOrToken: String)(f: (FullGroup, GroupInviteToken) ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = {
    val extractedToken =
      if (urlOrToken.startsWith(baseUrl)) {
        urlOrToken.drop(genInviteUrl(baseUrl).length).takeWhile(c ⇒ c != '?' && c != '#')
      } else {
        urlOrToken
      }

    extractedToken.isEmpty match {
      case false ⇒ (for {
        token ← GroupInviteTokenRepo.findByToken(extractedToken)
        group ← token.map(gt ⇒ GroupRepo.findFull(gt.groupId)).getOrElse(DBIO.successful(None))
      } yield for (g ← group; t ← token) yield (g, t)).flatMap {
        case Some((g, t)) ⇒ f(g, t)
        case None         ⇒ DBIO.successful(Error(InvalidToken))
      }
      case true ⇒ DBIO.successful(Error(InvalidToken))
    }
  }

  def withKickableGroupMember[R <: RpcResponse](
    groupOutPeer:    ApiGroupOutPeer,
    kickUserOutPeer: ApiUserOutPeer
  )(f: FullGroup ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      GroupUserRepo.find(group.id, kickUserOutPeer.userId).flatMap {
        case Some(GroupUser(_, _, inviterUserId, _, _, _)) ⇒
          if (kickUserOutPeer.userId != client.userId && (inviterUserId == client.userId || group.creatorUserId == client.userId)) {
            f(group)
          } else {
            DBIO.successful(Error(CommonRpcErrors.forbidden("You are permitted to kick this user.")))
          }
        case None ⇒ DBIO.successful(Error(RpcError(404, "USER_NOT_FOUND", "User is not a group member.", false, None)))
      }
    }
  }

  def withPublicGroup[R <: RpcResponse](groupOutPeer: ApiGroupOutPeer)(f: FullGroup ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = {
    withGroupOutPeer(groupOutPeer) { group ⇒
      if (group.isPublic) {
        f(group)
      } else {
        DBIO.successful(Error(RpcError(400, "GROUP_IS_NOT_PUBLIC", "The group is not public.", false, None)))
      }
    }
  }

  def genInviteUrl(baseUrl: String, token: String = "") = s"$baseUrl/join/$token"

  def withGroupOutPeerF[R <: RpcResponse](groupOutPeer: ApiGroupOutPeer)(f: FullGroup ⇒ Future[RpcError Xor R])(
    implicit
    ec:     ExecutionContext,
    system: ActorSystem
  ) =
    DbExtension(system).db.run(withGroupOutPeer(groupOutPeer)(fg ⇒ DBIO.from(f(fg))))

  def withGroupOutPeer[R <: RpcResponse](groupOutPeer: ApiGroupOutPeer)(f: FullGroup ⇒ DBIO[RpcError Xor R])(implicit ec: ExecutionContext): DBIO[RpcError Xor R] = {
    GroupRepo.findFull(groupOutPeer.groupId) flatMap {
      case Some(group) ⇒
        if (group.accessHash != groupOutPeer.accessHash) {
          DBIO.successful(Error(InvalidAccessHash))
        } else {
          f(group)
        }
      case None ⇒
        DBIO.successful(Error(CommonRpcErrors.GroupNotFound))
    }
  }

  def withGroupOutPeers[R <: RpcResponse](groupOutPeers: Seq[ApiGroupOutPeer])(f: ⇒ DBIO[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[RpcError Xor R] = {
    val checkOptsFutures = groupOutPeers map {
      case ApiGroupOutPeer(groupId, accessHash) ⇒
        DBIO.from(ACLUtils.checkOutPeer(ApiOutPeer(ApiPeerType.Group, groupId, accessHash), client.authId) map (Some(_)))
    }

    renderCheckResult(checkOptsFutures, f)
  }

  def withGroupOutPeersF[R <: RpcResponse](groupOutPeers: Seq[ApiGroupOutPeer])(f: ⇒ Future[RpcError Xor R])(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): Future[RpcError Xor R] =
    DbExtension(actorSystem).db.run(withGroupOutPeers(groupOutPeers)(DBIO.from(f)))

  private def checkUserPeer(userId: Int, accessHash: Long)(
    implicit
    client:      AuthorizedClientData,
    actorSystem: ActorSystem,
    ec:          ExecutionContext
  ): DBIO[Option[Boolean]] = {
    for {
      userOpt ← UserRepo.find(userId)
    } yield {
      userOpt map (u ⇒ ACLUtils.userAccessHash(client.authId, u.id, u.accessSalt) == accessHash)
    }
  }

  private def renderCheckResult[R <: RpcResponse](checkOptsActions: Seq[DBIO[Option[Boolean]]], f: ⇒ DBIO[RpcError Xor R])(implicit ec: ExecutionContext): DBIO[RpcError Xor R] = {
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
