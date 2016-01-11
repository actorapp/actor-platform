package im.actor.server.cli

import scala.concurrent.Future

private[cli] trait HttpHandlers {
  this: CliHandlers ⇒

  def createToken(rq: HttpTokenCreate): Future[Unit] = {
    for (resp ← request(HttpService, rq)) yield {
      println(s"Token created: ${resp.token}")
    }
  }
}
