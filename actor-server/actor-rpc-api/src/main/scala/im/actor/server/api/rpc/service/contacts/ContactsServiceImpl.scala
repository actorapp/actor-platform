package im.actor.server.api.rpc.service.contacts

import java.security.MessageDigest

import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.user.UserCommands.ContactToAdd

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.util.Timeout
import scodec.bits.BitVector
import shapeless._
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.contacts._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.users.{ UpdateUserLocalNameChanged, ApiUser }
import im.actor.server.db.DbExtension
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension, SeqUpdatesManager }
import im.actor.server.social.{ SocialExtension, SocialManager, SocialManagerRegion }
import im.actor.server.user._
import im.actor.util.misc.PhoneNumberUtils
import im.actor.server.{ models, persist }

import scalaz.EitherT

class ContactsServiceImpl(implicit actorSystem: ActorSystem)
  extends ContactsService {

  import ContactsUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val timeout = Timeout(5.seconds)

  private val db: Database = DbExtension(actorSystem).db
  private val userExt = UserExtension(actorSystem)
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)
  private implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region

  object Errors {
    val CantAddSelf = RpcError(401, "OWN_USER_ID", "User id cannot be equal to self.", false, None)
    val ContactAlreadyExists = RpcError(400, "CONTACT_ALREADY_EXISTS", "Contact already exists.", false, None)
    val ContactNotFound = RpcError(404, "CONTACT_NOT_FOUND", "Contact not found.", false, None)
  }

  case class EmailNameUser(email: String, name: Option[String], userId: Int)

  private[service] def hashIds(ids: Seq[Int]): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val uids = ids.to[immutable.SortedSet].mkString(",")
    BitVector(md.digest(uids.getBytes)).toHex
  }

  override def jhandleImportContacts(phones: IndexedSeq[ApiPhoneToImport], emails: IndexedSeq[ApiEmailToImport], clientData: ClientData): Future[HandlerResult[ResponseImportContacts]] = {
    val action =
      for {
        client ← authorizedClient(clientData)
        (clientPhones, clientEmails) ← fromFuture(userExt.getContactRecordsSet(client.userId))
        user ← fromDBIOOption(CommonErrors.UserNotFound)(persist.UserRepo.find(client.userId).headOption)
        optPhone ← fromDBIO(persist.UserPhoneRepo.findByUserId(client.userId).headOption)
        optEmail ← fromDBIO(persist.UserEmailRepo.findByUserId(client.userId).headOption)

        (pUsers, pSeqstate) ← fromDBIO(importPhones(user, optPhone, phones.filterNot(p ⇒ clientPhones.contains(p.phoneNumber)))(client))

        (eUsers, eSeqstate) ← fromDBIO(importEmails(user, optEmail, emails.filterNot(e ⇒ clientEmails.contains(e.email)))(client))
      } yield ResponseImportContacts((pUsers ++ eUsers).toVector, eSeqstate.seq, eSeqstate.state.toByteArray)

    db.run(action.run)
  }

  override def jhandleGetContacts(contactsHash: String, clientData: ClientData): Future[HandlerResult[ResponseGetContacts]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = persist.contact.UserContactRepo.findContactIdsActive(client.userId).map(hashIds).flatMap { hash ⇒
        if (contactsHash == hash) {
          DBIO.successful(Ok(ResponseGetContacts(Vector.empty[users.ApiUser], isNotChanged = true)))
        } else {
          for {
            userIds ← persist.contact.UserContactRepo.findContactIdsActive(client.userId)
            userStructs ← DBIO.from(Future.sequence(userIds.map(userId ⇒
              userExt.getApiStruct(userId, client.userId, client.authId))))
          } yield {
            Ok(ResponseGetContacts(
              users = userStructs.toVector,
              isNotChanged = false
            ))
          }
        }
      }

      action
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleRemoveContact(userId: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      persist.contact.UserContactRepo.find(ownerUserId = client.userId, contactUserId = userId).flatMap {
        case Some(contact) ⇒
          DBIO.from(userExt.getAccessHash(userId, clientData.authId)) flatMap { contactAccessHash ⇒
            if (accessHash == contactAccessHash) {
              for {
                _ ← deleteContact(client.userId, userId)
                _ ← DBIO.from(userExt.broadcastClientUpdate(UpdateUserLocalNameChanged(userId, None), None, isFat = false))
                seqstate ← DBIO.from(userExt.broadcastClientUpdate(UpdateContactsRemoved(Vector(userId)), None, isFat = false))
              } yield {
                Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
              }
            } else {
              DBIO.successful(Error(CommonErrors.InvalidAccessHash))
            }
          }
        case None ⇒ DBIO.successful(Error(Errors.ContactNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleAddContact(userId: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = (for {
        optUser ← persist.UserRepo.find(userId).headOption
        optNumber ← optUser.map(user ⇒ persist.UserPhoneRepo.findByUserId(user.id).headOption).getOrElse(DBIO.successful(None))
      } yield {
        (optUser, optNumber map (_.number))
      }) flatMap {
        case (Some(user), optPhoneNumber) ⇒
          if (accessHash == ACLUtils.userAccessHash(clientData.authId, user.id, user.accessSalt)) {
            persist.contact.UserContactRepo.find(ownerUserId = client.userId, contactUserId = userId).flatMap {
              case None ⇒
                for {
                  seqstate ← DBIO.from(userExt.addContact(client.userId, client.authId, user.id, None, optPhoneNumber, None))
                } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
              case Some(contact) ⇒
                DBIO.successful(Error(Errors.ContactAlreadyExists))
            }
          } else DBIO.successful(Error(CommonErrors.InvalidAccessHash))
        case (None, _) ⇒ DBIO.successful(Error(CommonErrors.UserNotFound))
        case (_, None) ⇒ DBIO.successful(Error(CommonErrors.UserPhoneNotFound))
      }

      action
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSearchContacts(query: String, clientData: ClientData): Future[HandlerResult[ResponseSearchContacts]] = {
    val action =
      for {
        client ← authorizedClient(clientData)
        nicknameUsers ← findByNickname(query, client)
        emailUsers ← findByEmail(query, client)
        phoneUsers ← findByNumber(query, client)
      } yield ResponseSearchContacts(nicknameUsers ++ phoneUsers ++ emailUsers)
    db.run(action.run)
  }

  private def findByNickname(nickname: String, client: AuthorizedClientData): EitherT[DBIO, RpcError, Vector[ApiUser]] = {
    for {
      users ← fromDBIO(persist.UserRepo.findByNickname(nickname) map (_.toList))
      structs ← fromFuture(Future.sequence(users map (user ⇒ userExt.getApiStruct(user.id, client.userId, client.authId))))
    } yield structs.toVector
  }

  private def findByEmail(email: String, client: AuthorizedClientData): EitherT[DBIO, RpcError, Vector[ApiUser]] = {
    for {
      userIds ← fromDBIO(persist.UserRepo.findIdsByEmail(email) map (_.toList))
      structs ← fromFuture(Future.sequence(userIds map (id ⇒ userExt.getApiStruct(id, client.userId, client.authId))))
    } yield structs.toVector
  }

  private def findByNumber(rawNumber: String, client: AuthorizedClientData): EitherT[DBIO, RpcError, Vector[ApiUser]] = {
    for {
      clientUser ← fromDBIOOption(CommonErrors.UserNotFound)(persist.UserRepo.find(client.userId).headOption)
      (clientPhones, _) ← fromFuture(userExt.getContactRecordsSet(client.userId))
      optPhone ← fromDBIO(persist.UserPhoneRepo.findByUserId(client.userId).headOption map (_.filterNot(p ⇒ clientPhones.contains(p.number))))
      normalizedPhone ← point(PhoneNumberUtils.normalizeStr(rawNumber, clientUser.countryCode))

      contactUsers ← if (optPhone.map(_.number) == normalizedPhone) point(Vector.empty[ApiUser])
      else fromDBIO(DBIO.sequence(normalizedPhone.toVector.map { phone ⇒
        implicit val c = client
        for {
          userPhones ← persist.UserPhoneRepo.findByPhoneNumber(phone)
          users ← DBIO.from(Future.sequence(userPhones.map(_.userId).toSet map { userId: Int ⇒ userExt.getApiStruct(userId, client.userId, client.authId) }))
        } yield {
          userPhones foreach (p ⇒ recordRelation(p.userId, client.userId))
          users.toVector
        }
      }) map (_.flatten))
    } yield contactUsers
  }

  private def importEmails(user: models.User, optOwnEmail: Option[models.UserEmail], emails: IndexedSeq[ApiEmailToImport])(implicit client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {
    //filtering out user's own email and making `Map` from emails to optional name
    val filtered: Map[String, Option[String]] = optOwnEmail
      .map(e ⇒ emails.filterNot(_.email == e.email)).getOrElse(emails)
      .map(e ⇒ e.email → e.name).toMap
    val filteredEmails = filtered.keySet

    for {
      //finding emails of users that are registered
      // but don't contain in user's contact list
      emailModels ← persist.UserEmailRepo.findByEmails(filteredEmails)
      userContacts ← persist.contact.UserContactRepo.findContactIdsAll(user.id)
      newEmailContacts = emailModels.filter(e ⇒ !userContacts.contains(e.userId))

      //registering UserEmailContacts
      newEmailContactsM = newEmailContacts.map(e ⇒ e.email → e.userId).toMap
      emailsNamesUsers = newEmailContactsM.keySet.map(k ⇒ EmailNameUser(k, filtered(k), newEmailContactsM(k)))
      (users, seqstate) ← createEmailContacts(user.id, emailsNamesUsers)

      //creating unregistered contacts
      unregisteredEmails = filteredEmails -- emailModels.map(_.email)
      unregisteredEmailActions = unregisteredEmails.map { email ⇒
        persist.contact.UnregisteredEmailContactRepo.createIfNotExists(email, user.id, filtered(email))
      }
      _ ← DBIO.sequence(unregisteredEmailActions.toSeq)
    } yield (users, seqstate)
  }

  private def importPhones(user: models.User, optPhone: Option[models.UserPhone], phones: IndexedSeq[ApiPhoneToImport])(client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {
    val filteredPhones = optPhone.map(p ⇒ phones.filterNot(_.phoneNumber == p.number)).getOrElse(phones)

    val (phoneNumbers, phonesMap) = filteredPhones.foldLeft((Set.empty[Long], Map.empty[Long, Option[String]])) {
      case ((phonesAcc, mapAcc), ApiPhoneToImport(phone, nameOpt)) ⇒
        PhoneNumberUtils.normalizeLong(phone, user.countryCode) match {
          case Nil        ⇒ (phonesAcc, mapAcc + ((phone, nameOpt)))
          case normPhones ⇒ (phonesAcc ++ normPhones, mapAcc ++ ((phone, nameOpt) +: normPhones.map(_ → nameOpt)))
        }
    }

    val f = for {
      userPhones ← persist.UserPhoneRepo.findByNumbers(phoneNumbers)
      ignoredContactsIds ← persist.contact.UserContactRepo.findContactIdsAll(user.id)
      uniquePhones = userPhones.filter(p ⇒ !ignoredContactsIds.contains(p.userId))
      usersPhones ← DBIO.sequence(uniquePhones map (p ⇒ persist.UserRepo.find(p.userId).headOption map (_.map((_, p.number))))) map (_.flatten) // TODO: #perf lots of sql queries
    } yield {
      usersPhones.foldLeft((immutable.Seq.empty[(models.User, Long, Option[String])], immutable.Set.empty[Int], immutable.Set.empty[Long])) {
        case ((usersPhonesNames, newContactIds, registeredPhones), (user, phone)) ⇒
          (usersPhonesNames :+ Tuple3(user, phone, phonesMap(phone)),
            newContactIds + user.id,
            registeredPhones + phone)
      }
    }

    f flatMap {
      case (usersPhonesNames, newContactIds, registeredPhoneNumbers) ⇒
        actorSystem.log.debug("Phone numbers: {}, registered: {}", phoneNumbers, registeredPhoneNumbers)

        // TODO: #perf do less queries
        val unregInsertActions = (phoneNumbers &~ registeredPhoneNumbers).toSeq map { phoneNumber ⇒
          persist.contact.UnregisteredPhoneContactRepo.createIfNotExists(phoneNumber, user.id, phonesMap.getOrElse(phoneNumber, None))
        }

        for {
          _ ← DBIO.sequence(unregInsertActions)
          _ ← DBIO.successful(newContactIds.toSeq foreach (id ⇒ recordRelation(id, user.id)))
          (userStructs, seqstate) ← createPhoneContacts(user.id, usersPhonesNames)(client)
        } yield (userStructs, seqstate)
    }
  }

  private def createPhoneContacts(ownerUserId: Int, usersPhonesNames: Seq[(models.User, Long, Option[String])])(implicit client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {

    persist.contact.UserContactRepo.findIds(ownerUserId, usersPhonesNames.map(_._1.id).toSet).flatMap { existingContactUserIds ⇒
      val contactsToAdd =
        usersPhonesNames.view
          .filterNot(p ⇒ existingContactUserIds.contains(p._1.id))
          .map {
            case (user, phone, localName) ⇒
              ContactToAdd(user.id, localName, Some(phone), None)
          }.force

      DBIO.from(addContactsGetStructs(contactsToAdd))
    }
  }

  private def createEmailContacts(ownerUserId: Int, contacts: Set[EmailNameUser])(implicit client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {
    val contactsToAdd = contacts.toSeq map { contact ⇒
      ContactToAdd(contact.userId, contact.name, None, Some(contact.email))
    }

    DBIO.from(addContactsGetStructs(contactsToAdd))
  }

  private def addContactsGetStructs(contactsToAdd: Seq[ContactToAdd])(implicit client: AuthorizedClientData): Future[(Seq[ApiUser], SeqState)] = {
    for {
      seqstate ← userExt.addContacts(client.userId, client.authId, contactsToAdd)
      structs ← FutureExt.ftraverse(contactsToAdd)(c ⇒ userExt.getApiStruct(c.contactUserId, client.userId, client.authId))
    } yield (structs, seqstate)
  }
}
