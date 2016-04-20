package im.actor.server.user

import java.time.Instant

import akka.actor.{ ActorRefFactory, Props }
import akka.pattern.ask
import akka.persistence.SnapshotMetadata
import akka.util.Timeout
import im.actor.api.rpc.users.UpdateUserLocalNameChanged
import im.actor.server.cqrs.{ Event, Processor, ProcessorState }
import im.actor.server.db.DbExtension
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserCommands.EditLocalName

import scala.concurrent.Future

private final case class UserContactsState(localNames: Map[Int, String] = Map.empty) extends ProcessorState[UserContactsState] {
  import UserEvents._

  override def updated(e: Event): UserContactsState = e match {
    case LocalNameChanged(_, contactUserId, localNameOpt) ⇒
      localNameOpt match {
        case None ⇒ this
        case Some(localName) ⇒
          this.copy(localNames = localNames + (contactUserId → localName))
      }
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): UserContactsState = this
}

private[user] final class UserContacts(userId: Int)(implicit factory: ActorRefFactory) {
  val ref = factory.actorOf(Props(new UserContactsActor(userId)))

  def editLocalName(contactUserId: Int, nameOpt: Option[String], supressUpdate: Boolean = false)(implicit timeout: Timeout): Future[SeqState] =
    (ref ? EditLocalName(userId, contactUserId, nameOpt, supressUpdate)).mapTo[SeqState]
}

private[user] final class UserContactsActor(userId: Int) extends Processor[UserContactsState] {
  import UserCommands._
  import UserQueries._
  import UserEvents._

  override def persistenceId: String = s"User_${userId}_Contacts"

  override def getInitialState: UserContactsState = UserContactsState()

  private val userExt = UserExtension(context.system)
  private val db = DbExtension(context.system).db

  override protected def handleCommand: Receive = {
    case EditLocalName(_, contactUserId, nameOpt, supressUpdate) ⇒
      editLocalName(contactUserId, nameOpt, supressUpdate)
  }

  override protected def handleQuery: QueryHandler = {
    case GetLocalName(_, contactUserId) ⇒
      Future.successful(GetLocalNameResponse(state.localNames.get(contactUserId)))
  }

  private def editLocalName(contactUserId: Int, nameOpt: Option[String], supressUpdate: Boolean): Unit = {
    persist(LocalNameChanged(Instant.now(), contactUserId, nameOpt)) { e ⇒
      commit(e)

      db.run(UserContactRepo.updateName(userId, contactUserId, nameOpt))

      if (supressUpdate)
        reply(SeqState())
      else
        replyFuture(userExt.broadcastUserUpdate(
          userId,
          UpdateUserLocalNameChanged(contactUserId, nameOpt),
          pushText = None,
          isFat = false,
          reduceKey = None,
          deliveryId = None
        ))
    }
  }
}