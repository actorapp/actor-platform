package im.actor.server.api.rpc.service.users

import akka.actor._
import akka.util.Timeout
import cats.data.Xor
import im.actor.api.rpc._
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.api.rpc.users.{ ResponseLoadFullUsers, UsersService }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.persist.UserRepo
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.user.UserExtension
import im.actor.util.misc.StringUtils
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object UserErrors {
  val NameInvalid = RpcError(400, "NAME_INVALID", "Invalid name. Valid nickname should not be empty and should consist of printable characters", false, None)
}

final class UsersServiceImpl(implicit actorSystem: ActorSystem) extends UsersService {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(10.seconds)

  private val db: Database = DbExtension(actorSystem).db
  private val userExt = UserExtension(actorSystem)

  override def doHandleEditUserLocalName(userId: Int, accessHash: Long, name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    authorized(clientData) { implicit client ⇒
      StringUtils.validName(name) match {
        case Xor.Right(validName) ⇒
          db.run(UserRepo.find(userId)) flatMap {
            case Some(user) ⇒
              if (accessHash == ACLUtils.userAccessHash(client.authId, user)) {
                val seqstateF = db.run(UserContactRepo.find(client.userId, userId)) flatMap {
                  case Some(contact) ⇒
                    userExt.editLocalName(client.userId, userId, Some(validName))
                  case None ⇒
                    userExt.addContact(client.userId, userId, Some(validName), None, None)
                }

                for {
                  seqstate ← seqstateF
                } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
              } else {
                Future.successful(Error(CommonRpcErrors.InvalidAccessHash))
              }
            case None ⇒ Future.successful(Error(CommonRpcErrors.UserNotFound))
          }
        case Xor.Left(err) ⇒ Future.successful(Error(UserErrors.NameInvalid))
      }
    }
  }

  /**
   * Loading Full User information
   *
   * @param userPeers User's peers to load. Should be non-empty
   */
  override protected def doHandleLoadFullUsers(
    userPeers:  IndexedSeq[ApiUserOutPeer],
    clientData: ClientData
  ): Future[HandlerResult[ResponseLoadFullUsers]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeersF(userPeers) {
        for {
          fullUsers ← Future.sequence(userPeers map (u ⇒ userExt.getApiFullStruct(u.userId, client.userId, client.authId)))
        } yield Ok(ResponseLoadFullUsers(fullUsers.toVector))
      }
    }
}
