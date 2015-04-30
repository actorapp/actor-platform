package im.actor.server.dashboard.controllers.utils

import Db._
import im.actor.server.persist
import play.api.mvc._
import play.api.mvc.Results._
import slick.driver.PostgresDriver.api._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object AuthAction extends ActionBuilder[Request] {

  def invokeBlock[A](request: Request[A], block: (Request[A]) ⇒ Future[Result]) = {
    val email = request.getQueryString("email")
    val token = request.getQueryString("auth-token")
    val query = persist.Manager.managers.filter { manager ⇒
      List(
        email.map(manager.email === _),
        token.map(manager.authToken === _)
      ).collect({ case Some(criteria) ⇒ criteria }).reduceLeftOption(_ && _).getOrElse(false: Rep[Boolean])
    } map { _.id }
    db.run {
      query.length.result.map { count ⇒ if (count > 0) block(request) else Future(Unauthorized) }
    } flatMap identity
  }

}
