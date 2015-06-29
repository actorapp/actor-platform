package im.actor.server.voximplant

import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Try, Success, Failure }

import akka.actor.ActorSystem
import com.ning.http.client.Response
import com.typesafe.config.Config
import dispatch._
import play.api.libs.json._

case class VoxImplantConfig(account: String, apiKey: String, appName: String)

object VoxImplantConfig {
  def fromConfig(config: Config): VoxImplantConfig = {
    VoxImplantConfig(
      account = config.getString("account"),
      apiKey = config.getString("api-key"),
      appName = config.getString("app-name")
    )
  }
}

final class VoxImplant(config: VoxImplantConfig)(implicit system: ActorSystem) {
  val appName = config.appName

  implicit private val ec: ExecutionContext = system.dispatcher

  private val http = new Http()

  private val baseUrlStr = "https://api.voximplant.com/platform_api"
  private val addUserUrl = url(baseUrlStr + "/AddUser/")
  private val bindUserUrl = url(baseUrlStr + "/BindUser/")

  def addUser(userName: String, password: String, displayName: String): Future[Long] = {
    val request = addUserUrl.GET <<? Map(
      "account_name" → config.account,
      "api_key" → config.apiKey,
      "user_name" → userName,
      "user_display_name" → displayName,
      "user_password" → password
    )

    http(request) map { result ⇒
      val respObj = getResponseObject(result)
      (respObj \ ("user_id")).validate[Long].getOrElse {
        throw new Exception(s"Cannot find user_id in response: ${respObj}")
      }
    } andThen {
      case Failure(e) ⇒
        system.log.error(e, "Failed to create VoxImplant user")
    }
  }

  def bindUser(userId: Long): Future[Unit] = {
    val request = bindUserUrl.GET <<? Map(
      "account_name" → config.account,
      "api_key" → config.apiKey,
      "application_name" → appName,
      "user_id" → userId.toString
    )

    http(request) map { result ⇒
      getResponseObject(result)
      ()
    }
  }

  private def getResponseObject(result: Response): JsObject = {
    if (result.getStatusCode == 200) {
      Try(Json.parse(result.getResponseBody)) match {
        case Success(respObj: JsObject) ⇒
          if (respObj.keys.contains("error")) {
            throw new Exception(s"VoxImplant returned error: ${respObj}")
          } else {
            respObj
          }
        case Success(unexpected) ⇒
          throw new Exception(s"Got unexpected result: ${unexpected}")
        case Failure(e) ⇒
          throw new Exception(e)
      }
    } else {
      throw new Exception("Response was not 200")
    }
  }
}