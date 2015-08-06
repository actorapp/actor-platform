package im.actor.server.api.rpc.service.users

import akka.util.Timeout

import im.actor.api.rpc.contacts.UpdateContactsAdded

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

import akka.actor._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.users.{ UpdateUserLocalNameChanged, UsersService }
import im.actor.server.db.DbExtension
import im.actor.server.persist
import im.actor.server.push.{ SeqUpdatesExtension, SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.user.{ UserExtension, UserOffice, UserViewRegion }
import im.actor.server.util.{ ContactsUtils, ACLUtils }

final class UsersServiceImpl(implicit actorSystem: ActorSystem) extends UsersService {
  import ContactsUtils._
  import SeqUpdatesManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(10.seconds)

  private implicit val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)
  private implicit val userViewRegion: UserViewRegion = UserExtension(actorSystem).viewRegion

  override def jhandleEditUserLocalName(userId: Int, accessHash: Long, name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      persist.User.find(userId).headOption flatMap {
        case Some(user) ⇒
          if (accessHash == ACLUtils.userAccessHash(client.authId, user)) {
            val action = persist.contact.UserContact.find(client.userId, userId) flatMap {
              case Some(contact) ⇒
                persist.contact.UserContact.updateName(client.userId, userId, Some(name))
              case None ⇒
                for {
                  userPhone ← persist.UserPhone.findByUserId(user.id).head
                  _ ← addContact(userId, userPhone.number, Some(name), user.accessSalt)
                  _ ← DBIO.from(UserOffice.broadcastClientUpdate(UpdateContactsAdded(Vector(userId)), None, isFat = true))
                } yield ()
            }

            for {
              _ ← action
              seqstate ← DBIO.from(UserOffice.broadcastClientUpdate(UpdateUserLocalNameChanged(userId, Some(name)), None, isFat = false))
            } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
          } else {
            DBIO.successful(Error(CommonErrors.InvalidAccessHash))
          }
        case None ⇒ DBIO.successful(Error(CommonErrors.UserNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }
}
