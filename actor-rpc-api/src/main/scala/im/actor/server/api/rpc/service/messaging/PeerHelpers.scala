package im.actor.server.api.rpc.service.messaging

import scala.concurrent.ExecutionContext

import akka.actor._
import scalaz._, std.either._
import slick.dbio.{ DBIO, DBIOAction, Effect }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, peers._
import im.actor.server.api.util
import im.actor.server.models
import im.actor.server.persist

trait PeerHelpers {
  protected def withOutPeer[R <: RpcResponse, E <: Effect](
    clientUserId: Int,
    outPeer: OutPeer
  )(
    f: => DBIO[RpcError \/ R]
  )(implicit client: AuthorizedClientData, ec: ExecutionContext, s: ActorSystem): DBIO[RpcError \/ R] = {
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

  private def validUser(optUser: Option[models.User])(implicit s: ActorSystem) = {
    optUser match {
      case Some(user) =>
        DBIO.successful(\/-(user))
      case None => DBIO.successful(Error(CommonErrors.UserNotFound))
    }
  }

  private def validUserAccessHash(accessHash: Long, user: models.User)(implicit client: BaseClientData, s: ActorSystem) = {
    if (accessHash == util.ACL.userAccessHash(client.authId, user)) {
      \/-(user)
    } else {
      Error(CommonErrors.InvalidAccessHash)
    }
  }
}
