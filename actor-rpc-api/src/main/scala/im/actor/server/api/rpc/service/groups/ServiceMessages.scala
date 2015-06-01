package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.files
import im.actor.api.rpc.messaging._

object ServiceMessages {
  def groupCreated = ServiceMessage("Group created", Some(ServiceExGroupCreated))
  def userInvited(userId: Int) = ServiceMessage("User invited to the group", Some(ServiceExUserInvited(userId)))
  def userJoined = ServiceMessage("User joined the group", Some(ServiceExUserJoined))
  def userLeft(userId: Int) = ServiceMessage("User left the group", Some(ServiceExUserLeft))
  def userKicked(userId: Int) = ServiceMessage("User kicked from the group", Some(ServiceExUserKicked(userId)))
  def changedTitle(title: String) = ServiceMessage("Group title changed", Some(ServiceExChangedTitle(title)))
  def changedAvatar(avatar: Option[files.Avatar]) = ServiceMessage(
    "Group avatar changed",
    Some(ServiceExChangedAvatar(avatar))
  )
}
