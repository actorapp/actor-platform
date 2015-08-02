package im.actor.server.user

private[user] trait UserQueriesHandlers {
  self: UserView ⇒

  import UserQueries._

  protected def handleQuery(q: UserQuery, state: User): Unit =
    q match {
      case GetAuthIds(_) ⇒ getAuthIds(state)
    }

  protected def getAuthIds(state: User): Unit = {
    sender() ! GetAuthIdsResponse(state.authIds.toSeq)
  }
}
