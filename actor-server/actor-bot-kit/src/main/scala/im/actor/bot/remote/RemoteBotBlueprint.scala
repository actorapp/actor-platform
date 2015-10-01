package im.actor.bot.remote

import akka.stream.scaladsl._
import upickle.default._

object RemoteBotBlueprint {
  import im.actor.bot.BotMessages._

  val flow =
    Flow[String]
      .map(read[BotUpdate])
}

