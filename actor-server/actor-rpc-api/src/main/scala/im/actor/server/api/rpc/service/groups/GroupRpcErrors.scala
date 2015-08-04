package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.RpcError

object GroupRpcErrors {
  val UserAlreadyInvited = RpcError(400, "USER_ALREADY_INVITED", "User is already a member of the group.", false, None)
  val WrongGroupTitle = RpcError(400, "WRONG_GROUP_TITLE", "Can't create group with such title.", false, None)
  val TopicTooLong = RpcError(400, "GROUP_TOPIC_TOO_LONG", "Group topic is too long. It should be no longer then 255 characters", false, None)
  val AboutTooLong = RpcError(400, "GROUP_ABOUT_TOO_LONG", "Group about is too long. It should be no longer then 255 characters", false, None)
  val UserAlreadyAdmin = RpcError(400, "USER_ALREADY_ADMIN", "User is already admin of this group", false, None)
}
