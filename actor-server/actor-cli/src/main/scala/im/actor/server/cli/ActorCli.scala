package im.actor.server.cli

import java.net.InetAddress

import akka.actor.{ ActorPath, ActorSystem }
import akka.cluster.client.{ ClusterClient, ClusterClientSettings }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import kamon.Kamon

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.reflect.ClassTag

private case class Config(
  command:            String             = "help",
  createBot:          CreateBot          = CreateBot(),
  updateIsAdmin:      UpdateIsAdmin      = UpdateIsAdmin(),
  httpApiTokenCreate: HttpApiTokenCreate = HttpApiTokenCreate()
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
private[cli] case object UpdateIsAdminResponse extends UpdateIsAdminResponse {
  def apply(): UpdateIsAdminResponse = this
}

private[cli] case class HttpApiTokenCreate(isAdmin: Boolean = false)
private case class HttpApiTokenCreateResponse(token: String)

private object Commands {
  val Help = "help"
  val CreateBot = "create-bot"
  val AdminGrant = "admin-grant"
  val AdminRevoke = "admin-revoke"
  val MigrateUserSequence = "migrate-user-sequence"
  val HttpApiTokenCreate = "http-api-token-create"
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
    cmd(Commands.MigrateUserSequence) action { (_, c) ⇒
      c.copy(command = Commands.MigrateUserSequence)
    }
    cmd(Commands.HttpApiTokenCreate) action { (_, c) ⇒
      c.copy(command = Commands.HttpApiTokenCreate)
    } children (
      opt[Unit]("admin") abbr "a" optional () action { (x, c) ⇒
        c.copy(httpApiTokenCreate = c.httpApiTokenCreate.copy(isAdmin = true))
      }
    )
  }

  parser.parse(args, Config()) foreach { config ⇒
    val handlers = new CliHandlers
    val migrationHandlers = new MigrationHandlers

    config.command match {
      case Commands.Help ⇒
        cmd(Future.successful(parser.showUsage))
      case Commands.CreateBot ⇒
        cmd(handlers.createBot(config.createBot))
      case Commands.AdminGrant | Commands.AdminRevoke ⇒
        cmd(handlers.updateIsAdmin(config.updateIsAdmin))
      case Commands.MigrateUserSequence ⇒
        cmd(migrationHandlers.userSequence(), 2.hours)
    }

    def cmd(f: Future[Unit], timeout: Duration = 10.seconds): Unit = {
      try {
        Await.result(f, timeout)
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

  protected lazy val system = {
    Kamon.start()
    ActorSystem("actor-cli", config)
  }

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
