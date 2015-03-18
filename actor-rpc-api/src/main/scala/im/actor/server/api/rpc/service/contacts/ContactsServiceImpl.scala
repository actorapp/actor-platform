package im.actor.server.api.rpc.service.contacts

import java.security.MessageDigest

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scodec.bits.BitVector
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, contacts._, misc._, users.UpdateUserLocalNameChanged
import im.actor.server.api.util
import im.actor.server.models
import im.actor.server.persist

class ContactsServiceImpl(
  seqUpdManagerRegion: ActorRef
)(implicit
  db: Database,
  actorSystem: ActorSystem)
    extends ContactsService {
  import im.actor.server.push.SeqUpdatesManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val timeout = Timeout(5.seconds)

  object Errors {
    val CantAddSelf = RpcError(401, "OWN_USER_ID", "User id cannot be equal to self.", false, None)
    val ContactAlreadyExists = RpcError(400, "CONTACT_ALREADY_EXISTS", "Contact already exists.", false, None)
    val ContactNotFound = RpcError(404, "CONTACT_NOT_FOUND", "Contact not found.", false, None)
  }

  private[service] def hashIds(ids: Seq[Int]): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val uids = ids.to[immutable.SortedSet].mkString(",")
    BitVector(md.digest(uids.getBytes)).toHex
  }

  override def handleImportContacts(phones: Vector[PhoneToImport], emails: Vector[EmailToImport])(implicit clientData: ClientData): Future[HandlerResult[ResponseImportContacts]] = throw new NotImplementedError()
  override def handleGetContacts(contactsHash: String)(implicit clientData: ClientData): Future[HandlerResult[ResponseGetContacts]] = {
    val authorizedAction = requireAuth.map { clientUserId =>
      val action = persist.contact.UserContact.findContactIdsAll(clientUserId).map(hashIds).flatMap { hash =>
        if (contactsHash == hash) {
          DBIO.successful(Ok(ResponseGetContacts(Vector.empty[users.User], isNotChanged = true)))
        } else {
          for {
            userIdsNames <- persist.contact.UserContact.findContactIdsWithLocalNames(clientUserId)
            userIds = userIdsNames.map(_._1).toSet
            users <- persist.User.findByIds(userIds)
            namesMap = immutable.Map(userIdsNames: _*)
            // TODO: #perf optimize (so much requests!)
            userStructs <- DBIO.sequence(users.map( user =>
              util.User.struct(user, namesMap.get(user.id).getOrElse(None), clientData.authId)))
          } yield {
            Ok(ResponseGetContacts(
              users = userStructs.toVector,
              isNotChanged = false
            ))
          }
        }
      }

      action.transactionally
    }

    db.run(toDBIOAction(authorizedAction))
  }
  override def handleRemoveContact(userId: Int, accessHash: Long)(implicit clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth.map { clientUserId =>
      persist.contact.UserContact.find(ownerUserId = clientUserId, contactUserId = userId).flatMap {
        case Some(contact) =>
          if (accessHash == util.ACL.userAccessHash(clientData.authId, userId, contact.accessSalt)) {
            for {
              _ <- persist.contact.UserContact.delete(clientUserId, userId)
              _ <- broadcastUserUpdate(seqUpdManagerRegion, clientUserId, UpdateUserLocalNameChanged(userId, None))
              seqstate <- broadcastUserUpdate(seqUpdManagerRegion, clientUserId, UpdateContactsRemoved(Vector(userId)))
            } yield {
              Ok(ResponseSeq(seqstate._1, seqstate._2))
            }
          } else {
            DBIO.successful(Error(CommonErrors.InvalidAccessHash))
          }
        case None => DBIO.successful(Error(Errors.ContactNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }
  override def handleAddContact(userId: Int, accessHash: Long)(implicit clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth.map { clientUserId =>
      val action = (for {
        optUser <- persist.User.find(userId).headOption
        optNumber <- optUser.map(user => persist.UserPhone.findByUserId(user.id).headOption).getOrElse(DBIO.successful(None))
      } yield {
        (optUser, optNumber map (_.number))
      }).flatMap {
        case (Some(user), Some(userPhoneNumber)) =>
          if (accessHash == util.ACL.userAccessHash(clientData.authId, user.id, user.accessSalt)) {
            persist.contact.UserContact.find(ownerUserId = clientUserId, contactUserId = userId).flatMap {
              case None =>
                addContactSendUpdate(clientUserId, user.id, userPhoneNumber, None, user.accessSalt) map {
                  case (seq, state) => Ok(ResponseSeq(seq, state))
                }
              case Some(contact) =>
                DBIO.successful(Error(Errors.ContactAlreadyExists))
            }
          } else DBIO.successful(Error(CommonErrors.InvalidAccessHash))
        case (None, _) => DBIO.successful(Error(CommonErrors.UserNotFound))
        case (_, None) => DBIO.successful(Error(CommonErrors.UserPhoneNotFound))
      }

      action.transactionally
    }

    db.run(toDBIOAction(authorizedAction))
  }
  override def handleSearchContacts(request: String)(implicit clientData: ClientData): Future[HandlerResult[ResponseSearchContacts]] = throw new NotImplementedError()

  private def addContactSendUpdate(
    clientUserId: Int,
    userId: Int,
    phoneNumber: Long,
    name: Option[String],
    accessSalt: String
  )(implicit clientData: ClientData) = {
    for {
      _ <- persist.contact.UserContact.createOrRestore(clientUserId, userId, phoneNumber, name, accessSalt)
      seqstate <- broadcastUserUpdate(seqUpdManagerRegion, clientUserId, UpdateContactsAdded(Vector(userId)))
    } yield seqstate
  }
}
