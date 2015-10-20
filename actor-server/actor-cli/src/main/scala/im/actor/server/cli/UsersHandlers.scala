package im.actor.server.cli

import scala.concurrent.Future

private[cli] trait UsersHandlers {
  this: CliHandlers ⇒

  def updateIsAdmin(rq: UpdateIsAdmin): Future[Unit] = {
    for (resp ← request(UsersService, rq))
      yield if (rq.isAdmin)
      println("Admin granted")
    else
      println("Admin revoked")
  }
}
