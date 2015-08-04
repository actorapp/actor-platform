package im.actor.server.group

import im.actor.server.group.GroupQueries.GetIntegrationTokenResponse

private[group] trait GroupQueryHandlers extends GroupCommandHelpers {
  this: GroupProcessor ⇒

  def getIntegrationToken(group: Group, userId: Int): Unit =
    withGroupMember(group, userId) { _ ⇒
      sender() ! GetIntegrationTokenResponse(group.bot.map(_.token))
    }

}
