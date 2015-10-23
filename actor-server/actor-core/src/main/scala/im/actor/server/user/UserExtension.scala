package im.actor.server.user

import akka.actor._
import akka.util.Timeout
import im.actor.server.hook._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

sealed trait UserExtension extends Extension

final class UserExtensionImpl(actorSystem: ActorSystem) extends UserExtension with UserOperations {
  UserProcessor.register()

  implicit val system = actorSystem

  import system.dispatcher

  lazy val processorRegion: UserProcessorRegion = UserProcessorRegion.start()(system)
  lazy val viewRegion: UserViewRegion = UserViewRegion(processorRegion.ref)

  implicit val timeout: Timeout = Timeout(20.seconds)

  val hooks = new UserHooksControl()
}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def lookup = UserExtension

  override def createExtension(system: ExtendedActorSystem) = new UserExtensionImpl(system)
}

final class UserHooksControl(implicit ec: ExecutionContext) extends HooksControl {
  val beforeContactRegistered = new HooksStorage1[UserHook.BeforeContactRegisteredHook, User]
}

object UserHook {

  abstract class BeforeContactRegisteredHook(system: ActorSystem) extends Hook1[User] {
    def run(user: User): Future[Unit]
  }

}