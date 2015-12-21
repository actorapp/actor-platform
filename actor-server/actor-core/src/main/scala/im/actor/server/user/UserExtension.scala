package im.actor.server.user

import akka.actor._
import akka.util.Timeout
import im.actor.server.hook._
import im.actor.server.sequence.SeqUpdatesExtension

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

sealed trait UserExtension extends Extension

object UserFields {
  val TimeZone = "TimeZone"
  val PreferredLanguages = "PreferredLanguages"
}

final class UserExtensionImpl(actorSystem: ActorSystem) extends UserExtension with UserOperations {
  UserProcessor.register()

  implicit val system = actorSystem

  import system.dispatcher

  lazy val processorRegion: UserProcessorRegion = UserProcessorRegion.start()(system)
  lazy val viewRegion: UserViewRegion = UserViewRegion(processorRegion.ref)
  override lazy val seqUpdExt = SeqUpdatesExtension(system)

  implicit val timeout: Timeout = Timeout(20.seconds)

  val hooks = new UserHooksControl()
}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def lookup = UserExtension

  override def createExtension(system: ExtendedActorSystem) = new UserExtensionImpl(system)
}

final class UserHooksControl(implicit ec: ExecutionContext) extends HooksControl {
  val afterAuth = new HooksStorage3[UserHook.AfterAuthHook, Unit, Int, Int, String]
  val beforeEmailContactRegistered = new HooksStorage2[UserHook.BeforeEmailContactRegisteredHook, Unit, Int, String]
}

object UserHook {

  abstract class AfterAuthHook(system: ActorSystem) extends Hook3[Unit, Int, Int, String] {
    override def run(userId: Int, appId: Int, deviceTitle: String): Future[Unit]
  }

  abstract class BeforeEmailContactRegisteredHook(system: ActorSystem) extends Hook2[Unit, Int, String] {
    def run(userId: Int, email: String): Future[Unit]
  }
}
