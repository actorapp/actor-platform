package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.RpcError

object GroupRpcErrors {
  val YouAlreadyAMember = RpcError(400, "USER_ALREADY_INVITED", "You are already a member of this group.", false, None)
  val NotAMember = RpcError(403, "FORBIDDEN", "You are not a group member.", false, None)
  val WrongGroupTitle = RpcError(400, "WRONG_GROUP_TITLE", "Can't create group with such title.", false, None)
  val TopicTooLong = RpcError(400, "GROUP_TOPIC_TOO_LONG", "Group topic is too long. It should be no longer then 255 characters", false, None)
  val AboutTooLong = RpcError(400, "GROUP_ABOUT_TOO_LONG", "Group about is too long. It should be no longer then 255 characters", false, None)
  val UserAlreadyAdmin = RpcError(400, "USER_ALREADY_ADMIN", "User is already admin of this group", false, None)
  val BlockedByUser = RpcError(403, "BLOCKED_BY_USER", "User blocked you, unable to invite him.", false, None)
}
