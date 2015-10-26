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
  val beforeEmailContactRegistered = new HooksStorage2[UserHook.BeforeEmailContactRegisteredHook, Int, String]
}

object UserHook {

  abstract class BeforeEmailContactRegisteredHook(system: ActorSystem) extends Hook2[Int, String] {
    def run(userId: Int, email: String): Future[Unit]
  }

}