package im.actor.server.cli

import java.net.InetAddress

import akka.actor.{ ActorPath, ActorSystem }
import akka.cluster.client.{ ClusterClient, ClusterClientSettings }
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import kamon.Kamon

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.reflect.ClassTag

private case class Config(
  command:       String         = "help",
  createBot:     CreateBot      = CreateBot(),
  updateIsAdmin: UpdateIsAdmin  = UpdateIsAdmin(),
  httpToken:     HttpToken      = HttpToken(),
  key:           Key            = Key(),
  host:          Option[String] = None // remote actor system host
)

private[cli] trait Request {
  type Response
}

private[cli] case class Key(create: Boolean = true, path: String = "actor-key")

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

private[cli] case class HttpToken(command: String = "create", create: HttpTokenCreate = HttpTokenCreate())

private[cli] case class HttpTokenCreate(isAdmin: Boolean = false) extends Request {
  override type Response = HttpTokenCreateResponse
}
private case class HttpTokenCreateResponse(token: String)

private object Commands {
  val Help = "help"
  val CreateBot = "create-bot"
  val AdminGrant = "admin-grant"
  val AdminRevoke = "admin-revoke"
  val MigrateUserSequence = "migrate-user-sequence"
  val HttpToken = "http-token"
  val Key = "key"
}

object ActorCli extends App {
  private val parser = new scopt.OptionParser[Config]("actor-cli") {
    cmd(Commands.Help) text "Show this help" action { (_, c) ⇒
      c.copy(command = Commands.Help)
    }
    cmd(Commands.CreateBot) action { (_, c) ⇒
      c.copy(command = Commands.CreateBot)
    } children (
      opt[String]("host") abbr "h" optional () action { (x, c) ⇒
        c.copy(host = Some(x))
      },
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
      opt[String]("host") abbr "h" optional () action { (x, c) ⇒
        c.copy(host = Some(x))
      },
      opt[Int]("userId") abbr "u" required () action { (x, c) ⇒
        c.copy(updateIsAdmin = UpdateIsAdmin(x, isAdmin = true))
      }
    )
    cmd(Commands.AdminRevoke) action { (_, c) ⇒
      c.copy(command = Commands.AdminRevoke)
    } children (
      opt[String]("host") abbr "h" optional () action { (x, c) ⇒
        c.copy(host = Some(x))
      },
      opt[Int]("userId") abbr "u" required () action { (x, c) ⇒
        c.copy(updateIsAdmin = UpdateIsAdmin(x, isAdmin = false))
      }
    )
    cmd(Commands.MigrateUserSequence) action { (_, c) ⇒
      c.copy(command = Commands.MigrateUserSequence)
    } children (
      opt[String]("host") abbr "h" optional () action { (x, c) ⇒
        c.copy(host = Some(x))
      }
    )
    cmd(Commands.HttpToken) action { (_, c) ⇒
      c.copy(command = Commands.HttpToken)
    } children (
      cmd("create") action { (_, c) ⇒
        c.copy(httpToken = c.httpToken.copy(command = "create"))
      } children (
        opt[String]("host") abbr "h" optional () action { (x, c) ⇒
          c.copy(host = Some(x))
        },
        opt[Unit]("admin") abbr "a" optional () action { (x, c) ⇒
          c.copy(httpToken = c.httpToken.copy(create = c.httpToken.create.copy(isAdmin = true)))
        }
      )
    )
    cmd(Commands.Key) action { (_, c) ⇒
      c.copy(command = Commands.Key)
    } children (
      opt[String]("host") abbr "h" optional () action { (x, c) ⇒
        c.copy(host = Some(x))
      },
      opt[Unit]("create") abbr "c" required () action { (x, c) ⇒
        c.copy(key = c.key.copy(create = true))
      },
      opt[String]("out") abbr "o" optional () action { (x, c) ⇒
        c.copy(key = c.key.copy(path = x))
      }
    )
  }

  parser.parse(args, Config()) foreach { config ⇒
    val handlers = new CliHandlers(config.host)
    val migrationHandlers = new MigrationHandlers
    val securityHandlers = new SecurityHandlers

    config.command match {
      case Commands.Help ⇒
        cmd(FastFuture.successful(parser.showUsage))
      case Commands.CreateBot ⇒
        cmd(handlers.createBot(config.createBot))
      case Commands.AdminGrant | Commands.AdminRevoke ⇒
        cmd(handlers.updateIsAdmin(config.updateIsAdmin))
      case Commands.MigrateUserSequence ⇒
        cmd(migrationHandlers.userSequence(), 2.hours)
      case Commands.Key ⇒
        cmd(securityHandlers.createKey(config.key.path))
      case Commands.HttpToken ⇒
        config.httpToken.command match {
          case "create" ⇒ cmd(handlers.createToken(config.httpToken.create))
        }
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

final class CliHandlers(host: Option[String]) extends BotHandlers with UsersHandlers with HttpHandlers {
  protected val BotService = "bots"
  protected val UsersService = "users"
  protected val HttpService = "http"

  protected val config = ConfigFactory.parseResources("cli.conf").resolve()

  protected lazy val system = {
    Kamon.start()
    ActorSystem("actor-cli", config)
  }

  protected lazy val remoteHost = host.getOrElse(InetAddress.getLocalHost.getHostAddress)

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
