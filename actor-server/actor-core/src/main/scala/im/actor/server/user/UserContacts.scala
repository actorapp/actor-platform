package im.actor.server.user

import java.time.Instant

import akka.actor.{ ActorRefFactory, Props }
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.persistence.SnapshotMetadata
import akka.util.Timeout
import im.actor.api.rpc.users.UpdateUserLocalNameChanged
import im.actor.server.cqrs.{ Event, Processor, ProcessorState }
import im.actor.server.db.DbExtension
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import im.actor.server.user.UserCommands.EditLocalName

import scala.concurrent.{ ExecutionContext, Future }

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

  def editLocalNameSilently(authId: Long, contactUserId: Int, nameOpt: Option[String])(
    implicit
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] =
    (ref ? EditLocalName(userId, authId, contactUserId, nameOpt, supressUpdate = true)).mapTo[SeqState] map (_ ⇒ ())
}

private[user] final class UserContactsActor(userId: Int) extends Processor[UserContactsState] {
  import UserCommands._
  import UserQueries._
  import UserEvents._

  override def persistenceId: String = s"User_${userId}_Contacts"

  override def getInitialState: UserContactsState = UserContactsState()

  private val seqUpdExt = SeqUpdatesExtension(context.system)
  private val db = DbExtension(context.system).db

  override protected def handleCommand: Receive = {
    case EditLocalName(_, authId, contactUserId, nameOpt, supressUpdate) ⇒
      editLocalName(authId, contactUserId, nameOpt, supressUpdate)
  }

  override protected def handleQuery: QueryHandler = {
    case GetLocalName(_, contactUserId) ⇒
      FastFuture.successful(GetLocalNameResponse(state.localNames.get(contactUserId)))
  }

  private def editLocalName(authId: Long, contactUserId: Int, nameOpt: Option[String], supressUpdate: Boolean): Unit = {
    persist(LocalNameChanged(Instant.now(), contactUserId, nameOpt)) { e ⇒
      commit(e)

      db.run(UserContactRepo.updateName(userId, contactUserId, nameOpt))

      if (supressUpdate)
        reply(SeqState())
      else
        replyFuture(seqUpdExt.deliverClientUpdate(
          userId,
          authId,
          UpdateUserLocalNameChanged(contactUserId, nameOpt),
          pushRules = seqUpdExt.pushRules(isFat = false, None)
        ))
    }
  }
}
