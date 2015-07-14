package im.actor.server.dashboard

import im.actor.server.dashboard.util.Helpers._
import im.actor.server.dashboard.util.Resource
import im.actor.server.dashboard.models._

import org.scalajs.dom.ext.Ajax
import upickle._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class Users(baseUri: String, authToken: String) extends Resource(baseUri) {

  implicit def optionReadWriter[T: Reader: Writer] = ReadWriter[Option[T]]({
    case Some(value) ⇒ writeJs(value)
    case None        ⇒ Js.Null
  }, {
    case Js.Null ⇒ None
    case js      ⇒ Some(readJs[T](js))
  })

  def get(id: Int) = withQPromise {
    for {
      xhr ← Ajax.get(
        url = makeUrl(s"/users/$id?authToken=$authToken"),
        responseType = "application/json"
      )
    } yield xhr.responseText
  }

  def create(name: String, phone: String, email: String) = withQPromise {
    for {
      xhr ← Ajax.post(
        url = makeUrl(s"/users?authToken=$authToken"),
        data = write(UserForm(name, Option(phone), Option(email))),
        headers = headers,
        responseType = "application/json"
      )
    } yield xhr.responseText
  }

  def update(id: Int, newName: String) = withQPromise {
    for {
      xhr ← Ajax.put(
        url = makeUrl(s"/users/$id?authToken=$authToken"),
        data = write(UpdateForm(newName)),
        headers = headers,
        responseType = "application/json"
      )
    } yield write(Ok("ok"))
  }

  def delete(id: Int) = withQPromise {
    for {
      xhr ← Ajax.delete(
        url = makeUrl(s"/users/$id?authToken=$authToken"),
        responseType = "application/json"
      )
    } yield write(Ok("ok"))
  }

  def list(page: Int, perPage: Int) = withQPromise {
    for {
      xhr ← Ajax.get(
        url = makeUrl(s"/users?authToken=$authToken&page=$page&perPage=$perPage"),
        responseType = "application/json"
      )
    } yield xhr.responseText
  }

}