package im.actor.server.api.util

import scala.concurrent.ExecutionContext
import scala.collection.immutable
import scalaz._

import akka.actor._
import slick.dbio.{ DBIO, Effect }

import im.actor.api.rpc._
import im.actor.api.rpc.peers._
import im.actor.server.api.util
import im.actor.server.{ models, persist }

object PeerUtils {
  def withOutPeer[R <: RpcResponse](
    clientUserId: Int,
    outPeer: OutPeer
  )(
    f: => DBIO[RpcError \/ R]
  )(implicit client: AuthorizedClientData, actorSystem: ActorSystem, ec: ExecutionContext): DBIO[RpcError \/ R] = {
    outPeer.`type` match {
      case PeerType.Private =>
        (for {
          optUser <- persist.User.find(outPeer.id).headOption
          usererrOrUser <- validUser(optUser)
          hasherrOrUser <- DBIO.successful(usererrOrUser.map(validUserAccessHash(outPeer.accessHash, _)))
        } yield hasherrOrUser).flatMap {
          case Error(err) => DBIO.successful(Error(err))
          case _          => f
        }
    }
  }

  def withUserOutPeers[R <: RpcResponse](userOutPeers: immutable.Seq[UserOutPeer])
                                                  (f: => DBIO[RpcError \/ R])
                                                  (implicit
                                                   client: AuthorizedClientData,
                                                   actorSystem: ActorSystem,
                                                   ec: ExecutionContext): DBIO[RpcError \/ R] = {
    val checkOptsFutures = userOutPeers map {
      case UserOutPeer(userId, accessHash) =>
        checkUserPeer(userId, accessHash)
    }

    renderCheckResult(checkOptsFutures, f)
  }

  private def checkUserPeer(userId: Int, accessHash: Long)
                           (implicit
                            client: AuthorizedClientData,
                            actorSystem: ActorSystem,
                            ec: ExecutionContext): DBIO[Option[Boolean]] = {
    for {
      userOpt <- persist.User.find(userId).headOption
    } yield {
      userOpt map (u => ACL.userAccessHash(client.authId, u.id, u.accessSalt) == accessHash)
    }
  }
/*
  private def checkGroupPeer(groupId: Int, accessHash: Long): DBIO[Option[Boolean]] = {
    for {
      groupOpt <- persist.Group.find(groupId)
    } yield {
      groupOpt map (_.accessHash == accessHash)
    }
  }*/

  private def validUser(optUser: Option[models.User]) = {
    optUser match {
      case Some(user) =>
        DBIO.successful(\/-(user))
      case None => DBIO.successful(Error(CommonErrors.UserNotFound))
    }
  }

  private def validUserAccessHash(accessHash: Long, user: models.User)(implicit client: BaseClientData, actorSystem: ActorSystem) = {
    if (accessHash == util.ACL.userAccessHash(client.authId, user)) {
      \/-(user)
    } else {
      Error(CommonErrors.InvalidAccessHash)
    }
  }

  private def renderCheckResult[R <: RpcResponse](checkOptsActions: Seq[DBIO[Option[Boolean]]], f: => DBIO[RpcError \/ R])
                                                 (implicit ec: ExecutionContext): DBIO[RpcError \/ R] = {
    DBIO.sequence(checkOptsActions) flatMap { checkOpts =>
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
