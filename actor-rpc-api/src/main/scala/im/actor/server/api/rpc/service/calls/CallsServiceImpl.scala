package im.actor.server.api.rpc.calls

import java.math.BigInteger
import java.security.MessageDigest

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.calls.{ CallsService, ResponseGetVoxUser, ResponseInitVoxSupport }
import im.actor.api.rpc.peers.UserOutPeer
import im.actor.server.{ models, persist }
import im.actor.server.util.ACLUtils
import im.actor.server.voximplant.VoxImplant

final class CallsServiceImpl(voximplant: VoxImplant)(implicit db: Database, actorSystem: ActorSystem) extends CallsService {
  import PeerHelpers._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleGetVoxUser(userPeer: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseGetVoxUser]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      withUserOutPeer(userPeer) {
        persist.voximplant.VoxUser.findByUserId(userPeer.userId) map {
          case Some(voxUser) ⇒ Ok(ResponseGetVoxUser(s"${voxUser.userName}@${voximplant.appName}"))
          case None          ⇒ Error(CommonErrors.UserNotFound)
        }
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleInitVoxSupport(clientData: ClientData): Future[HandlerResult[ResponseInitVoxSupport]] = {
    val authorizedAction = requireAuth(clientData) map { client ⇒
      persist.voximplant.VoxUser.findByUserId(client.userId) flatMap {
        case Some(voxUser) ⇒
          DBIO.successful(Ok(ResponseInitVoxSupport(voxUser.userName, genPassword(voxUser.salt))))
        case None ⇒

          val voxUsername = s"user_${client.userId}"
          val salt = ACLUtils.nextAccessSalt()
          val password = genPassword(salt)

          for {
            dbUserName ← persist.User.findName(client.userId) map (_.getOrElse(s"User ${client.userId}"))
            voxUserId ← DBIO.from(voximplant.addUser(voxUsername, password, dbUserName))
            _ ← DBIO.from(voximplant.bindUser(voxUserId))
            voxUser = models.voximplant.VoxUser(client.userId, voxUserId, voxUsername, dbUserName, salt)
            _ ← persist.voximplant.VoxUser.create(voxUser)
          } yield Ok(ResponseInitVoxSupport(voxUser.userName, password))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  private def genPassword(salt: String): String = {
    val secret = ACLUtils.secretKey()
    val seed = s"${salt}:${secret}"
    val md = MessageDigest.getInstance("SHA-256")
    md.update(seed.getBytes("UTF-8"))
    val bi = new BigInteger(1, md.digest())
    bi.toString(16)
  }
}
