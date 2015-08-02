package im.actor.server.user

import scala.concurrent.duration._

import akka.actor.{ PoisonPill, Props, ReceiveTimeout }
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.PersistentView

private[user] object UserView {
  def props = Props(classOf[UserView])
}

private[user] final class UserView extends PersistentView with UserQueriesHandlers {

  import UserEvents._

  private val userId = self.path.name.toInt

  override def persistenceId = UserOffice.persistenceIdFor(userId)
  override def viewId = UserOffice.persistenceIdFor(userId)

  context.setReceiveTimeout(15.minutes)

  def receive = {
    case e: Created if isPersistent ⇒
      context become created(User(e))
  }

  def created(u: User): Receive = {
    case e: UserEvent if isPersistent ⇒ context become created(u.updated(e))
    case q: UserQuery                 ⇒ handleQuery(q, u)
    case ReceiveTimeout               ⇒ context.parent ! Passivate(stopMessage = PoisonPill)
  }
}
