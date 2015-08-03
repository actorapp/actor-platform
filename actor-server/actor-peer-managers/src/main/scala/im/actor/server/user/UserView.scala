package im.actor.server.user

import scala.concurrent.duration._

import akka.actor.{ ActorLogging, PoisonPill, Props, ReceiveTimeout }
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.{ Update, PersistentView }

private[user] object UserView {
  def props = Props(classOf[UserView])
}

private[user] final class UserView extends PersistentView with UserQueriesHandlers with ActorLogging {

  import UserEvents._

  private val userId = self.path.name.toInt

  override def persistenceId = UserOffice.persistenceIdFor(userId)
  override def viewId = persistenceId

  context.setReceiveTimeout(15.minutes)

  self ! Update(await = true)

  def receive = {
    case e: Created ⇒
      unstashAll()
      context become created(User(e))
    case query ⇒
      log.warning("Received query to a non-created user: {}", query)
      stash()
      self ! Update(await = true)
  }

  def created(u: User): Receive = {
    case e: UserEvent   ⇒ context become created(u.updated(e))
    case q: UserQuery   ⇒ handleQuery(q, u)
    case ReceiveTimeout ⇒ context.parent ! Passivate(stopMessage = PoisonPill)
  }

}
