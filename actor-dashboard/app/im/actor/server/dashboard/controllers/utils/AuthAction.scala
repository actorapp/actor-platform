package im.actor.server.dashboard.controllers.utils

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Results._
import play.api.mvc._
import slick.driver.PostgresDriver.api._

import im.actor.server.dashboard.controllers.utils.Db._
import im.actor.server.persist

object AuthAction extends ActionBuilder[Request] {

  def invokeBlock[A](request: Request[A], block: (Request[A]) ⇒ Future[Result]) = {
    val token = request.getQueryString("auth-token")
    val query = persist.Manager.managers.filter { manager ⇒
      token.map { manager.authToken === _ } getOrElse (false: Rep[Boolean])
    } map { _.id }
    db.run {
      query.length.result.map { count ⇒ if (count > 0) block(request) else Future(Unauthorized) }
    } flatMap identity
  }

}
