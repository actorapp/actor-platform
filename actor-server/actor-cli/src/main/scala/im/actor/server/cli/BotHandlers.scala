package im.actor.server.cli

import scala.concurrent.Future

private[cli] trait BotHandlers {
  this: CliHandlers ⇒

  def createBot(rq: CreateBot): Future[Unit] = {
    for (resp ← request(BotService, rq))
      yield println(s"Bot user created, token: ${resp.token}")
  }
}
