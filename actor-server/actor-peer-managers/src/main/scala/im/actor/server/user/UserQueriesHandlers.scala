package im.actor.server.user

private[user] trait UserQueriesHandlers {
  self: UserOfficeActor ⇒

  import UserQueries._

  protected def getAuthIds(state: User): Unit = {
    sender() ! GetAuthIdsResponse(state.authIds.toSeq)
  }
}
