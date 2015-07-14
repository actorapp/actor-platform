package im.actor.server.dashboard

import im.actor.server.dashboard.models.{ LoginForm, UserEmail }
import im.actor.server.dashboard.util.Resource
import im.actor.server.dashboard.util.Helpers._
import org.scalajs.dom.ext.Ajax
import upickle._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js.annotation.{ JSExport, JSExportAll }

@JSExport
class App(hostUri: String) extends Resource(s"$hostUri/dashboard") {
  val dashboardUri = s"$hostUri/dashboard"

  @JSExport
  def startAuth(email: String) = withQPromise {
    for {
      xhr ← Ajax.post(
        url = makeUrl(s"/auth/start"),
        data = write(UserEmail(email)),
        headers = headers,
        responseType = "application/json"
      )
    } yield write(Ok("ok"))
  }

  @JSExport
  def authorize(email: String, token: String) = withQPromise {
    Future.successful(new AuthorizedApp(email, dashboardUri, token))
  }

  @JSExport
  def login(email: String, passcode: String) = withQPromise {
    for {
      xhr ← Ajax.post(
        url = makeUrl("/auth/login"),
        data = write(LoginForm(email, passcode)),
        headers = headers,
        responseType = "application/json"
      )
    } yield new AuthorizedApp(email, dashboardUri, read[AuthToken](xhr.responseText).authToken)
  }
}

@JSExportAll
class AuthorizedApp(email: String, baseUri: String, val authToken: String) extends Resource(baseUri) {

  def logout() = withQPromise {
    for {
      xhr ← Ajax.get(
        url = makeUrl(s"/auth/logout?email=$email&authToken=$authToken"),
        responseType = "application/json"
      )
    } yield write(Message("Logged out"))
  }

  val users = new Users(baseUri, authToken)

}