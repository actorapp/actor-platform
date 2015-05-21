package im.actor.server.ilectro

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory

class ILectro(implicit system: ActorSystem) {
  private implicit val ec = system.dispatcher
  private implicit val meterializer = ActorFlowMaterializer()
  private implicit val http = Http()

  private implicit val config = ILectroConfig(ConfigFactory.load().getConfig("actor-ilectro-lib"))

  val users = new Users()
  val lists = new Lists()

}
