package im.actor.server.api.rpc.service.contacts

import java.security.MessageDigest

import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.model.{ User, UserEmail, UserPhone }
import im.actor.server.persist.contact.{ UnregisteredEmailContactRepo, UnregisteredPhoneContactRepo, UserContactRepo }
import im.actor.server.persist.{ UserEmailRepo, UserPhoneRepo, UserRepo }
import im.actor.server.user.UserCommands.ContactToAdd

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.util.Timeout
import scodec.bits.BitVector
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._
import im.actor.api.rpc._
import im.actor.api.rpc.contacts._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.api.rpc.users.ApiUser
import im.actor.server.db.DbExtension
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import im.actor.server.social.{ SocialExtension, SocialManager, SocialManagerRegion }
import im.actor.server.user._
import im.actor.util.misc.PhoneNumberUtils

class ContactsServiceImpl(implicit actorSystem: ActorSystem)
  extends ContactsService {

  import SocialManager._
  import PeerHelpers._
  import DBIOResultRpc._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val timeout = Timeout(5.seconds)

  private val db: Database = DbExtension(actorSystem).db
  private val userExt = UserExtension(actorSystem)
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)
  private implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region

  object ContactsRpcErrors {
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

  override def doHandleImportContacts(
    phones:        IndexedSeq[ApiPhoneToImport],
    emails:        IndexedSeq[ApiEmailToImport],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseImportContacts]] =
    authorized(clientData) { implicit client ⇒
      val action = (for {
        contacts ← fromFuture(userExt.getContactRecordsSet(client.userId))
        (clientPhones, clientEmails) = contacts
        user ← fromDBIOOption(CommonRpcErrors.UserNotFound)(UserRepo.find(client.userId))
        optPhone ← fromDBIO(UserPhoneRepo.findByUserId(client.userId).headOption)
        optEmail ← fromDBIO(UserEmailRepo.findByUserId(client.userId).headOption)

        pUsersState ← fromDBIO(importPhones(user, optPhone, phones.filterNot(p ⇒ clientPhones.contains(p.phoneNumber)))(client))
        (pUsers, pSeqstate) = pUsersState

        eUsersState ← fromDBIO(importEmails(user, optEmail, emails.filterNot(e ⇒ clientEmails.contains(e.email)))(client))
        (eUsers, eSeqstate) = eUsersState

      } yield {
        val users = (pUsers ++ eUsers).toVector

        ResponseImportContacts(
          users = if (optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)) Vector.empty else users,
          eSeqstate.seq,
          eSeqstate.state.toByteArray,
          userPeers = users map (u ⇒ ApiUserOutPeer(u.id, u.accessHash))
        )
      }).value
      db.run(action)
    }

  override def doHandleGetContacts(
    contactsHash:  String,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseGetContacts]] =
    authorized(clientData) { implicit client ⇒
      val action = UserContactRepo.findContactIdsActive(client.userId).map(hashIds).flatMap { hash ⇒
        if (contactsHash == hash) {
          DBIO.successful(Ok(ResponseGetContacts(Vector.empty, isNotChanged = true, Vector.empty)))
        } else {
          for {
            userIds ← UserContactRepo.findContactIdsActive(client.userId)
            users ← DBIO.from(Future.sequence(userIds.map(userId ⇒
              userExt.getApiStruct(userId, client.userId, client.authId)))) map (_.toVector)
          } yield {
            Ok(ResponseGetContacts(
              users = if (optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)) Vector.empty else users,
              isNotChanged = false,
              userPeers = users map (u ⇒ ApiUserOutPeer(u.id, u.accessHash))
            ))
          }
        }
      }
      db.run(action)
    }

  override def doHandleRemoveContact(userId: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeerF(ApiUserOutPeer(userId, accessHash)) {
        for (seqstate ← userExt.removeContact(client.userId, userId))
          yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
      }
    }

  override def doHandleAddContact(userId: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      val action = (for {
        optUser ← UserRepo.find(userId)
        optNumber ← optUser.map(user ⇒ UserPhoneRepo.findByUserId(user.id).headOption).getOrElse(DBIO.successful(None))
      } yield {
        (optUser, optNumber map (_.number))
      }) flatMap {
        case (Some(user), optPhoneNumber) ⇒
          if (accessHash == ACLUtils.userAccessHash(clientData.authId, user.id, user.accessSalt)) {
            UserContactRepo.find(ownerUserId = client.userId, contactUserId = userId).flatMap {
              case None ⇒
                for {
                  seqstate ← DBIO.from(userExt.addContact(client.userId, user.id, None, optPhoneNumber, None))
                } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
              case Some(contact) ⇒
                DBIO.successful(Error(ContactsRpcErrors.ContactAlreadyExists))
            }
          } else DBIO.successful(Error(CommonRpcErrors.InvalidAccessHash))
        case (None, _) ⇒ DBIO.successful(Error(CommonRpcErrors.UserNotFound))
        case (_, None) ⇒ DBIO.successful(Error(CommonRpcErrors.UserPhoneNotFound))
      }
      db.run(action)
    }

  override def doHandleSearchContacts(query: String, optimizations: IndexedSeq[ApiUpdateOptimization.Value], clientData: ClientData): Future[HandlerResult[ResponseSearchContacts]] =
    authorized(clientData) { implicit client ⇒
      val action = (for {
        nicknameUsers ← findByNickname(query, client)
        emailUsers ← findByEmail(query, client)
        phoneUsers ← findByNumber(query, client)
      } yield {
        val users = nicknameUsers ++ phoneUsers ++ emailUsers
        users foreach (u ⇒ recordRelation(u.id, client.userId))
        ResponseSearchContacts(
          users = if (optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)) Vector.empty else users,
          userPeers = users map (u ⇒ ApiUserOutPeer(u.id, u.accessHash))
        )
      }).value
      db.run(action)
    }

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case UserErrors.ContactNotFound ⇒ ContactsRpcErrors.ContactNotFound
  }

  private def findByNickname(nickname: String, client: AuthorizedClientData): Result[Vector[ApiUser]] = {
    for {
      users ← fromDBIO(UserRepo.findByNickname(nickname) map (_.toList))
      structs ← fromFuture(Future.sequence(users map (user ⇒ userExt.getApiStruct(user.id, client.userId, client.authId))))
    } yield structs.toVector
  }

  private def findByEmail(email: String, client: AuthorizedClientData): Result[Vector[ApiUser]] = {
    for {
      userIds ← fromDBIO(UserRepo.findIdsByEmail(email) map (_.toList))
      structs ← fromFuture(Future.sequence(userIds map (id ⇒ userExt.getApiStruct(id, client.userId, client.authId))))
    } yield structs.toVector
  }

  private def findByNumber(rawNumber: String, client: AuthorizedClientData): Result[Vector[ApiUser]] = {
    for {
      clientUser ← fromDBIOOption(CommonRpcErrors.UserNotFound)(UserRepo.find(client.userId))
      contacts ← fromFuture(userExt.getContactRecordsSet(client.userId))
      (clientPhones, _) = contacts
      optPhone ← fromDBIO(UserPhoneRepo.findByUserId(client.userId).headOption map (_.filterNot(p ⇒ clientPhones.contains(p.number))))
      normalizedPhone ← point(PhoneNumberUtils.normalizeStr(rawNumber, clientUser.countryCode))

      contactUsers ← if (optPhone.map(_.number) == normalizedPhone) point(Vector.empty[ApiUser])
      else fromDBIO(DBIO.sequence(normalizedPhone.toVector.map { phone ⇒
        implicit val c = client
        for {
          userPhones ← UserPhoneRepo.findByPhoneNumber(phone)
          users ← DBIO.from(Future.sequence(userPhones.map(_.userId).toSet map { userId: Int ⇒ userExt.getApiStruct(userId, client.userId, client.authId) }))
        } yield {
          users.toVector
        }
      }) map (_.flatten))
    } yield contactUsers
  }

  private def importEmails(user: User, optOwnEmail: Option[UserEmail], emails: IndexedSeq[ApiEmailToImport])(implicit client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {
    //filtering out user's own email and making `Map` from emails to optional name
    val filtered: Map[String, Option[String]] = optOwnEmail
      .map(e ⇒ emails.filterNot(_.email == e.email)).getOrElse(emails)
      .map(e ⇒ e.email → e.name).toMap
    val filteredEmails = filtered.keySet

    for {
      //finding emails of users that are registered
      // but don't contain in user's contact list
      emailModels ← UserEmailRepo.findByEmails(filteredEmails)
      userContacts ← UserContactRepo.findContactIdsAll(user.id)
      newEmailContacts = emailModels.filter(e ⇒ !userContacts.contains(e.userId))

      //registering UserEmailContacts
      newEmailContactsM = newEmailContacts.map(e ⇒ e.email → e.userId).toMap
      emailsNamesUsers = newEmailContactsM.keySet.map(k ⇒ EmailNameUser(k, filtered(k), newEmailContactsM(k)))
      (users, seqstate) ← createEmailContacts(user.id, emailsNamesUsers)

      //creating unregistered contacts
      unregisteredEmails = filteredEmails -- emailModels.map(_.email)
      unregisteredEmailActions = unregisteredEmails.map { email ⇒
        UnregisteredEmailContactRepo.createIfNotExists(email, user.id, filtered(email))
      }
      _ ← DBIO.sequence(unregisteredEmailActions.toSeq)
    } yield (users, seqstate)
  }

  private def importPhones(user: User, optPhone: Option[UserPhone], phones: IndexedSeq[ApiPhoneToImport])(client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {
    val filteredPhones = optPhone.map(p ⇒ phones.filterNot(_.phoneNumber == p.number)).getOrElse(phones)

    val (phoneNumbers, phonesMap) = filteredPhones.foldLeft((Set.empty[Long], Map.empty[Long, Option[String]])) {
      case ((phonesAcc, mapAcc), ApiPhoneToImport(phone, nameOpt)) ⇒
        PhoneNumberUtils.normalizeLong(phone, user.countryCode) match {
          case Nil        ⇒ (phonesAcc, mapAcc + ((phone, nameOpt)))
          case normPhones ⇒ (phonesAcc ++ normPhones, mapAcc ++ ((phone, nameOpt) +: normPhones.map(_ → nameOpt)))
        }
    }

    val f = for {
      userPhones ← UserPhoneRepo.findByNumbers(phoneNumbers)
      ignoredContactsIds ← UserContactRepo.findContactIdsAll(user.id)
      uniquePhones = userPhones.filter(p ⇒ !ignoredContactsIds.contains(p.userId))
      usersPhones ← DBIO.sequence(uniquePhones map (p ⇒ UserRepo.find(p.userId) map (_.map((_, p.number))))) map (_.flatten) // TODO: #perf lots of sql queries
    } yield {
      usersPhones.foldLeft((immutable.Seq.empty[(User, Long, Option[String])], immutable.Set.empty[Int], immutable.Set.empty[Long])) {
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
          UnregisteredPhoneContactRepo.createIfNotExists(phoneNumber, user.id, phonesMap.getOrElse(phoneNumber, None))
        }

        for {
          _ ← DBIO.sequence(unregInsertActions)
          _ ← DBIO.successful(newContactIds.toSeq foreach (id ⇒ recordRelation(id, user.id)))
          (userStructs, seqstate) ← createPhoneContacts(user.id, usersPhonesNames)(client)
        } yield (userStructs, seqstate)
    }
  }

  private def createPhoneContacts(ownerUserId: Int, usersPhonesNames: Seq[(User, Long, Option[String])])(implicit client: AuthorizedClientData): DBIO[(Seq[ApiUser], SeqState)] = {
    UserContactRepo.findIds(ownerUserId, usersPhonesNames.map(_._1.id).toSet).flatMap { existingContactUserIds ⇒
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
      seqstate ← userExt.addContacts(client.userId, contactsToAdd)
      structs ← FutureExt.ftraverse(contactsToAdd)(c ⇒ userExt.getApiStruct(c.contactUserId, client.userId, client.authId))
    } yield (structs, seqstate)
  }
}
