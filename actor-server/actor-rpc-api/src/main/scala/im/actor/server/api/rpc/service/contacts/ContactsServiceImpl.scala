package im.actor.server.api.rpc.service.contacts

import java.security.MessageDigest

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.util.Timeout
import scodec.bits.BitVector
import slick.dbio
import slick.dbio.DBIO
import slick.dbio.Effect.{ Write, Read }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.contacts._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.users.{ UpdateUserLocalNameChanged, User }
import im.actor.server
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ UserUtils, ContactsUtils, ACLUtils, PhoneNumber }
import im.actor.server.{ models, persist }

class ContactsServiceImpl(
  implicit
  val seqUpdManagerRegion: SeqUpdatesManagerRegion,
  val socialManagerRegion: SocialManagerRegion,
  db:                      Database,
  actorSystem:             ActorSystem
)
  extends ContactsService {

  import ContactsUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserUtils._

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

  override def jhandleImportContacts(phones: Vector[PhoneToImport], emails: Vector[EmailToImport], clientData: ClientData): Future[HandlerResult[ResponseImportContacts]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      persist.UserPhone.findByUserId(client.userId).head.flatMap { currentUserPhone ⇒
        persist.User.find(client.userId).head.flatMap { currentUser ⇒
          val filteredPhones = phones.view.filterNot(_.phoneNumber == currentUserPhone.number)

          val (phoneNumbers, phonesMap) = filteredPhones.foldLeft((Set.empty[Long], Map.empty[Long, Option[String]])) {
            case ((phonesAcc, mapAcc), PhoneToImport(phone, nameOpt)) ⇒
              PhoneNumber.normalizeLong(phone, currentUser.countryCode) match {
                case Some(normPhone) ⇒ ((phonesAcc + normPhone), mapAcc ++ Seq((phone, nameOpt), (normPhone, nameOpt)))
                case None            ⇒ (phonesAcc, mapAcc + ((phone, nameOpt)))
              }
          }

          val f = for {
            userPhones ← persist.UserPhone.findByNumbers(phoneNumbers)
            ignoredContactsIds ← persist.contact.UserContact.findIds_all(client.userId)
            uniquePhones = userPhones.filter(p ⇒ !ignoredContactsIds.contains(p.userId))
            usersPhones ← DBIO.sequence(uniquePhones map (p ⇒ persist.User.find(p.userId).headOption map (_.map((_, p.number))))) map (_.flatten) // TODO: #perf lots of sql queries
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
                persist.contact.UnregisteredContact.createIfNotExists(phoneNumber, client.userId, phonesMap.get(phoneNumber).getOrElse(None))
              }

              DBIO.sequence(unregInsertActions).flatMap { _ ⇒
                if (usersPhonesNames.nonEmpty) {
                  for {
                    userStructs ← createAllUserContacts(client.userId, usersPhonesNames)
                    seqstate ← broadcastClientUpdate(UpdateContactsAdded(newContactIds.toVector), None)
                  } yield {
                    newContactIds.toSeq foreach (id ⇒ recordRelation(id, client.userId))

                    Ok(ResponseImportContacts(userStructs.toVector, seqstate._1, seqstate._2))
                  }
                } else {
                  DBIO.successful(Ok(ResponseImportContacts(immutable.Vector.empty, 0, Array.empty)))
                }
              }
          }
        }
      }
    }

    db.run(toDBIOAction(action)) // TODO: transactionally
  }

  override def jhandleGetContacts(contactsHash: String, clientData: ClientData): Future[HandlerResult[ResponseGetContacts]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = persist.contact.UserContact.findContactIdsAll(client.userId).map(hashIds).flatMap { hash ⇒
        if (contactsHash == hash) {
          DBIO.successful(Ok(ResponseGetContacts(Vector.empty[users.User], isNotChanged = true)))
        } else {
          for {
            userIdsNames ← persist.contact.UserContact.findContactIdsWithLocalNames(client.userId)
            userIds = userIdsNames.map(_._1).toSet
            users ← persist.User.findByIds(userIds)
            namesMap = immutable.Map(userIdsNames: _*)
            // TODO: #perf optimize (so much requests!)
            userStructs ← DBIO.sequence(users.map(user ⇒
              userStruct(user, namesMap.get(user.id).getOrElse(None), clientData.authId)))
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

  override def jhandleRemoveContact(userId: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      persist.contact.UserContact.find(ownerUserId = client.userId, contactUserId = userId).flatMap {
        case Some(contact) ⇒
          if (accessHash == ACLUtils.userAccessHash(clientData.authId, userId, contact.accessSalt)) {
            for {
              _ ← persist.contact.UserContact.delete(client.userId, userId)
              _ ← broadcastClientUpdate(UpdateUserLocalNameChanged(userId, None), None)
              seqstate ← broadcastClientUpdate(UpdateContactsRemoved(Vector(userId)), None)
            } yield {
              Ok(ResponseSeq(seqstate._1, seqstate._2))
            }
          } else {
            DBIO.successful(Error(CommonErrors.InvalidAccessHash))
          }
        case None ⇒ DBIO.successful(Error(Errors.ContactNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleAddContact(userId: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = (for {
        optUser ← persist.User.find(userId).headOption
        optNumber ← optUser.map(user ⇒ persist.UserPhone.findByUserId(user.id).headOption).getOrElse(DBIO.successful(None))
      } yield {
        (optUser, optNumber map (_.number))
      }).flatMap {
        case (Some(user), Some(userPhoneNumber)) ⇒
          if (accessHash == ACLUtils.userAccessHash(clientData.authId, user.id, user.accessSalt)) {
            persist.contact.UserContact.find(ownerUserId = client.userId, contactUserId = userId).flatMap {
              case None ⇒
                for {
                  _ ← addContact(user.id, userPhoneNumber, None, user.accessSalt)
                  seqstate ← broadcastClientUpdate(UpdateContactsAdded(Vector(user.id)), None, isFat = true)
                } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
              case Some(contact) ⇒
                DBIO.successful(Error(Errors.ContactAlreadyExists))
            }
          } else DBIO.successful(Error(CommonErrors.InvalidAccessHash))
        case (None, _) ⇒ DBIO.successful(Error(CommonErrors.UserNotFound))
        case (_, None) ⇒ DBIO.successful(Error(CommonErrors.UserPhoneNotFound))
      }

      action.transactionally
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSearchContacts(rawNumber: String, clientData: ClientData): Future[HandlerResult[ResponseSearchContacts]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      getClientUserPhoneUnsafe.flatMap {
        case (clientUser, clientPhone) ⇒
          PhoneNumber.normalizeStr(rawNumber, clientUser.countryCode) match {
            case Some(phoneNumber) ⇒
              val filteredPhones = Set(phoneNumber).filter(_ != clientPhone.number)

              for {
                userPhones ← persist.UserPhone.findByNumbers(filteredPhones)
                users ← getUserStructs(userPhones.map(_.userId).toSet)
              } yield {
                userPhones foreach (p ⇒ recordRelation(p.userId, client.userId))

                Ok(ResponseSearchContacts(users.toVector))
              }
            case None ⇒
              DBIO.successful(Ok(ResponseSearchContacts(Vector.empty)))
          }
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  private def createAllUserContacts(ownerUserId: Int, usersPhonesNames: immutable.Seq[(models.User, Long, Option[String])])(implicit client: AuthorizedClientData): dbio.DBIOAction[immutable.Seq[User], NoStream, Read with Write with Read with Read with Read with Read] = {
    persist.contact.UserContact.findIds(ownerUserId, usersPhonesNames.map(_._1.id).toSet).flatMap { existingContactUserIds ⇒
      val actions = usersPhonesNames map {
        case (user, phone, localName) ⇒
          val userContact = models.contact.UserContact(
            ownerUserId = ownerUserId,
            contactUserId = user.id,
            phoneNumber = phone,
            name = localName,
            accessSalt = user.accessSalt,
            isDeleted = false
          )

          val action = if (existingContactUserIds.contains(user.id)) {
            persist.contact.UserContact.insertOrUpdate(userContact)
          } else {
            persist.contact.UserContact.createOrRestore(
              ownerUserId = userContact.ownerUserId,
              contactUserId = userContact.contactUserId,
              phoneNumber = userContact.phoneNumber,
              name = userContact.name,
              accessSalt = userContact.accessSalt
            )
          }

          for {
            _ ← action
            userStruct ← userStruct(user, localName, client.authId)
          } yield {
            userStruct
          }
      }

      DBIO.sequence(actions)
    }
  }
}
