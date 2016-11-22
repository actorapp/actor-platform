package im.actor.server.group

//TODO: make up to date with channels
object PushTexts {
  val Added = "User added"
  val Kicked = "User kicked"
  val Left = "User left"

  def invited(gt: GroupType) =
    if (gt.isChannel) {
      "You are invited to a channel"
    } else {
      "You are invited to a group"
    }

  def titleChanged(gt: GroupType) =
    if (gt.isChannel) {
      "Channel title changed"
    } else {
      "Group title changed"
    }

  def topicChanged(gt: GroupType) =
    if (gt.isChannel) {
      "Channel topic changed"
    } else {
      "Group topic changed"
    }
}
