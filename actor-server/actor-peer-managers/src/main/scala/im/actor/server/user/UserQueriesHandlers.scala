package im.actor.server.user

import akka.actor.Actor

private[user] trait UserQueriesHandlers {
  self: Actor ⇒

  import UserQueries._

  protected def handleQuery(q: UserQuery, state: User): Unit =
    q match {
      case GetAuthIds(_) ⇒ getAuthIds(state)
    }

  protected def getAuthIds(state: User): Unit = {
    sender() ! GetAuthIdsResponse(state.authIds.toSeq)
  }
}
