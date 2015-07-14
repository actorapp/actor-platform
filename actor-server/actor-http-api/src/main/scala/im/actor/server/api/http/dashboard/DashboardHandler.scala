package im.actor.server.api.http.dashboard

import akka.http.scaladsl.model.StatusCodes

import scala.concurrent.ExecutionContext
import scala.util.Success

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import cats.data.Xor
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import slick.driver.PostgresDriver.api._

import im.actor.server.api.http.RoutesHandler
import im.actor.server.email._
import im.actor.server.persist
import im.actor.server.dashboard.models._

class DashboardHandler(implicit db: Database, system: ActorSystem, materializer: Materializer, emailSender: EmailSender) extends RoutesHandler {

  implicit val ec: ExecutionContext = system.dispatcher

  import PlayJsonSupport._
  import JsonFormatters._

  override def routes: Route = pathPrefix("dashboard") { options(complete("")) ~ authRoutes ~ usersRoutes }

  // format: OFF
  private def authRoutes: Route =
    pathPrefix("auth") {
      post {
        path("start") {
          entity(as[UserEmail]) { userEmail =>
            onSuccess(Auth.start(userEmail.email)) {
              case Xor.Left(fail) => complete(fail)
              case Xor.Right(_) => complete(StatusCodes.Accepted)
            }
          }
        } ~
        path("login") {
          entity(as[LoginForm]) { form =>
            onSuccess(Auth.login(form.email, form.passcode)) {
              case Xor.Left(fail) => complete(fail)
              case Xor.Right(token) => complete(StatusCodes.OK -> token)
            }
          }
        }
      } ~
      get {
        validateAuthToken {
          path("logout") {
            parameter(("email", "authToken")) { (email, authToken) =>
              onSuccess(Auth.logout(email, authToken)) {
                case Xor.Left(fail) => complete(fail)
                case Xor.Right(_) => complete(StatusCodes.OK)
              }
            }
          }
        }
      }
    }

  private def usersRoutes: Route = validateAuthToken {
      path("users" / IntNumber) { userId =>
        put {
          entity(as[UpdateForm]) { updateForm =>
            onSuccess(Users.update(userId, updateForm)) {
              case Xor.Left(fail) => complete(fail)
              case Xor.Right(_) => complete(StatusCodes.Accepted)
            }
          }
        } ~
        delete {
          onSuccess(Users.delete(userId)) {
            case Xor.Left(fail) => complete(fail)
            case Xor.Right(_) => complete(StatusCodes.Accepted)
          }
        } ~
        get {
          onSuccess(Users.get(userId)) {
            case Xor.Left(fail) => complete(fail)
            case Xor.Right(user) => complete(StatusCodes.OK -> user)
          }
        }
      } ~
      path("users") {
        post {
          entity(as[UserForm]) { userForm =>
            onSuccess(Users.create(userForm)) {
              case Xor.Left(fail) => complete(fail)
              case Xor.Right(user) => complete(StatusCodes.Created -> user)
            }
          }
        } ~
        get {
          parameters(("page".as[Int] ? 1, "perPage".as[Int] ? 50)) { (page, perPage) =>
            onSuccess(Users.list(page, perPage)) { users =>
              complete(users)
            }
          }
        }
      }
    }
  // format: ON

  def validateAuthToken: Directive0 =
    Directive { inner ⇒
      parameter("authToken") { token ⇒
        onComplete(db.run(persist.DashboardSession.exists(token))) {
          case Success(b) if b ⇒ inner(())
          case _               ⇒ reject(AuthorizationFailedRejection)
        }
      }
    }

}