package im.actor.server.cli

import java.net.InetAddress

import akka.actor.{ ActorPath, ActorSystem }
import akka.cluster.client.{ ClusterClient, ClusterClientSettings }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.reflect.ClassTag

private case class Config(
  command:       String        = "help",
  createBot:     CreateBot     = CreateBot(),
  updateIsAdmin: UpdateIsAdmin = UpdateIsAdmin()
)

private[cli] trait Request {
  type Response
}

private[cli] case class CreateBot(
  username: String  = "",
  name:     String  = "",
  isAdmin:  Boolean = false
) extends Request {
  override type Response = CreateBotResponse
}

private[cli] case class CreateBotResponse(token: String)

private[cli] case class UpdateIsAdmin(
  userId:  Int     = 0,
  isAdmin: Boolean = false
) extends Request {
  override type Response = UpdateIsAdminResponse
}

private[cli] sealed trait UpdateIsAdminResponse
private[cli] case object UpdateIsAdminResponse extends UpdateIsAdminResponse

private object Commands {
  val Help = "help"
  val CreateBot = "create-bot"
  val AdminGrant = "admin-grant"
  val AdminRevoke = "admin-revoke"
}

object ActorCli extends App {
  private val parser = new scopt.OptionParser[Config]("actor-cli") {
    cmd(Commands.Help) text "Show this help" action { (_, c) ⇒
      c.copy(command = Commands.Help)
    }
    cmd(Commands.CreateBot) action { (_, c) ⇒
      c.copy(command = Commands.CreateBot)
    } children (
      opt[String]("username") abbr "u" required () action { (x, c) ⇒
        c.copy(createBot = c.createBot.copy(username = x))
      },
      opt[String]("name") abbr "n" required () action { (x, c) ⇒
        c.copy(createBot = c.createBot.copy(name = x))
      },
      opt[Unit]("admin") abbr "a" optional () action { (x, c) ⇒
        c.copy(createBot = c.createBot.copy(isAdmin = true))
      }
    )
    cmd(Commands.AdminGrant) action { (_, c) ⇒
      c.copy(command = Commands.AdminGrant)
    } children (
      opt[Int]("userId") abbr "u" required () action { (x, c) ⇒
        c.copy(updateIsAdmin = UpdateIsAdmin(x, isAdmin = true))
      }
    )
    cmd(Commands.AdminRevoke) action { (_, c) ⇒
      c.copy(command = Commands.AdminRevoke)
    } children (
      opt[Int]("userId") abbr "u" required () action { (x, c) ⇒
        c.copy(updateIsAdmin = UpdateIsAdmin(x, isAdmin = false))
      }
    )
  }

  parser.parse(args, Config()) foreach { config ⇒
    val handlers = new CliHandlers

    cmd(config.command match {
      case Commands.Help ⇒
        Future.successful(parser.showUsage)
      case Commands.CreateBot ⇒
        handlers.createBot(config.createBot)
      case Commands.AdminGrant | Commands.AdminRevoke ⇒
        handlers.updateIsAdmin(config.updateIsAdmin)
    })

    def cmd(f: Future[Unit]): Unit = {
      try {
        Await.result(f, 10.seconds)
      } finally {
        handlers.shutdown()
      }
    }
  }
}

final class CliHandlers extends BotHandlers with UsersHandlers {
  protected val BotService = "bots"
  protected val UsersService = "users"

  protected val config = ConfigFactory.parseResources("cli.conf").resolve()

  protected lazy val system = ActorSystem("actor-cli", config)

  protected lazy val remoteHost = InetAddress.getLocalHost.getHostAddress

  protected lazy val initialContacts = Set(ActorPath.fromString(s"akka.tcp://actor-server@$remoteHost:2552/system/receptionist"))

  protected lazy val client = system.actorOf(ClusterClient.props(ClusterClientSettings(system).withInitialContacts(initialContacts)))

  protected implicit lazy val ec: ExecutionContext = system.dispatcher

  protected implicit val timeout: Timeout = Timeout(10.seconds)

  def shutdown(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, timeout.duration)
  }

  protected def request[T <: Request: ClassTag](service: String, request: T): Future[T#Response] =
    (client ? ClusterClient.Send(s"/user/cli/$service", request, localAffinity = false)) map (_.asInstanceOf[T#Response])
}
