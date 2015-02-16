package im.actor.server.api.frontend

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import com.typesafe.config.Config

object WS {
  def start(appConf: Config)(implicit system: ActorSystem, materializer: FlowMaterializer): Unit = {
    import system.dispatcher


  }
}
