package im.actor.server.api.http.info

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.CirceSupport
import im.actor.config.ActorConfig
import im.actor.server.api.http.HttpHandler
import im.actor.server.api.http.json.{ Endpoints, JsonEncoders, ServerInfo }

import scala.collection.JavaConversions._

private[http] final class AboutHttpHandler()(implicit system: ActorSystem) extends HttpHandler
  with CirceSupport
  with JsonEncoders {

  private lazy val about = getServerInfo

  def routes: Route = path("about") {
    get {
      complete(OK → about)
    }
  }

  private def getServerInfo: ServerInfo = {
    val publicEndPoints =
      (ActorConfig.load().getStringList("public-endpoints") foldLeft Endpoints.empty) {
        case (Endpoints(tcp, tls, ws, wss), el) ⇒
          el match {
            case e if e.startsWith("tcp") ⇒ Endpoints(e :: tcp, tls, ws, wss)
            case e if e.startsWith("tls") ⇒ Endpoints(tcp, e :: tls, ws, wss)
            case e if e.startsWith("ws")  ⇒ Endpoints(tcp, tls, e :: ws, wss)
            case e if e.startsWith("wss") ⇒ Endpoints(tcp, tls, ws, e :: wss)
          }
      }
    ServerInfo(ActorConfig.projectName, publicEndPoints)
  }

}