package im.actor.server.api.frontend

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import com.typesafe.config.Config

object Ws {
  def start(appConf: Config)(implicit system: ActorSystem, materializer: FlowMaterializer): Unit = {
    import system.dispatcher


  }
}
