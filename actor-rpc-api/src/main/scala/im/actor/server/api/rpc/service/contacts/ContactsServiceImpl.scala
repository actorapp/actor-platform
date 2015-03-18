package im.actor.server.api.rpc.service.contacts

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, contacts._, misc._
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
  }

  override def handleImportContacts(phones: Vector[PhoneToImport], emails: Vector[EmailToImport])(implicit clientData: ClientData): Future[HandlerResult[ResponseImportContacts]] = throw new NotImplementedError()
  override def handleGetContacts(contactsHash: String)(implicit clientData: ClientData): Future[HandlerResult[ResponseGetContacts]] = throw new NotImplementedError()
  override def handleRemoveContact(userId: Int, accessHash: Long)(implicit clientData: ClientData): Future[HandlerResult[ResponseSeq]] = throw new NotImplementedError()
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

  private def addContactSendUpdate(currentUserId: Int, userId: Int, phoneNumber: Long, name: Option[String], accessSalt: String)(implicit clientData: ClientData, timeout: Timeout) = {
    val update = UpdateContactsAdded(Vector(userId))
    val header = UpdateContactsAdded.header
    val serializedData = update.toByteArray

    for {
      _ <- persist.contact.UserContact.createOrRestore(currentUserId, userId, phoneNumber, name, accessSalt)
      otherAuthIds <- persist.AuthId.findByUserId(currentUserId).map(_.view.filter(_.id != clientData.authId))
      _ <- DBIO.sequence(
        otherAuthIds.map { authId =>
          persist.sequence.SeqUpdate.create(models.sequence.SeqUpdate(authId.id, header, serializedData))
        }
      )
      ownUpdate = models.sequence.SeqUpdate(clientData.authId, header, serializedData)
      _ <- persist.sequence.SeqUpdate.create(ownUpdate)
      ownSeq <- DBIO.from(pushUpdateGetSeq(seqUpdManagerRegion, clientData.authId, update).map(_.value))
    } yield {
      otherAuthIds foreach (authId => pushUpdate(seqUpdManagerRegion, authId.id, update))

      (ownSeq, ownUpdate.ref.toByteArray)
    }
  }
}
