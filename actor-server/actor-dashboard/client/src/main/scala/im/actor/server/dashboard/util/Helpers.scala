package im.actor.server.dashboard.util

import org.scalajs.dom.ext.AjaxException

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{ Failure, Success }

object Helpers {

  val headers: Map[String, String] = Map("Content-Type" → "application/json;charset=UTF-8")

  def withQPromise[T](future: Future[T]) = {
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

    val defer = js.Dynamic.global.Q.defer()

    future onComplete {
      case Success(result) ⇒
        defer.resolve(result.asInstanceOf[js.Any])
      case Failure(err: AjaxException) ⇒
        defer.reject(JSON.parse(err.xhr.responseText))
      case Failure(err) ⇒
        defer.reject("Internal error")
    }
    defer.promise
  }

}