package im.actor.server.api

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.io.IO
import akka.stream.scaladsl.Flow
import akka.stream.FlowMaterializer
import scala.concurrent.duration._

object FrontendServer {
  implicit val askTimeout = 5.seconds

}
