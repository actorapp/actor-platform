package im.actor.server.group

import im.actor.api.rpc.files
import im.actor.api.rpc.messaging._

object GroupServiceMessages {
  def groupCreated = ApiServiceMessage("Group created", Some(ApiServiceExGroupCreated))
  def userInvited(userId: Int) = ApiServiceMessage("User invited to the group", Some(ApiServiceExUserInvited(userId)))
  def userJoined = ApiServiceMessage("User joined the group", Some(ApiServiceExUserJoined))
  def userLeft = ApiServiceMessage("User left the group", Some(ApiServiceExUserLeft))
  def userKicked(userId: Int) = ApiServiceMessage("User kicked from the group", Some(ApiServiceExUserKicked(userId)))
  def changedTitle(title: String) = ApiServiceMessage("Group title changed", Some(ApiServiceExChangedTitle(title)))
  def changedTopic(topic: Option[String]) = ApiServiceMessage("Group topic changed", Some(ApiServiceExChangedTopic(topic)))
  def changedAbout(about: Option[String]) = ApiServiceMessage("Group about changed", Some(ApiServiceExChangedAbout(about)))
  def changedAvatar(avatar: Option[files.ApiAvatar]) = ApiServiceMessage(
    "Group avatar changed",
    Some(ApiServiceExChangedAvatar(avatar))
  )
}
