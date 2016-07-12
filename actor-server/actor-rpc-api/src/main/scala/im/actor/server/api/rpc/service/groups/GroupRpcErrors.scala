package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.RpcError

// format: OFF
object GroupRpcErrors {
  val AlreadyInvited        = RpcError(400, "USER_ALREADY_INVITED", "You are already invited to this group.",                              false, None)
  val AlreadyJoined         = RpcError(400, "USER_ALREADY_JOINED",  "You are already a member of this group.",                             false, None)
  val NotAMember            = RpcError(403, "FORBIDDEN",            "You are not a group member.",                                         false, None)
  val InvalidTitle          = RpcError(400, "GROUP_TITLE_INVALID",  "Invalid group title.",                                                false, None)
  val TopicTooLong          = RpcError(400, "GROUP_TOPIC_TOO_LONG", "Group topic is too long. It should be no longer then 255 characters", false, None)
  val AboutTooLong          = RpcError(400, "GROUP_ABOUT_TOO_LONG", "Group about is too long. It should be no longer then 255 characters", false, None)
  val UserAlreadyAdmin      = RpcError(400, "USER_ALREADY_ADMIN",   "User is already admin of this group",                                 false, None)
  val BlockedByUser         = RpcError(403, "BLOCKED_BY_USER",      "User blocked you, unable to invite him.",                             false, None)
  val GroupIdAlreadyExists  = RpcError(400, "GROUP_ALREADY_EXISTS", "Group with such id already exists",                                   false, None)
  val InvalidInviteToken    = RpcError(403, "INVALID_INVITE_TOKEN", "No correct token provided.",                                          false, None)
  val GroupNotPublic        = RpcError(400, "GROUP_IS_NOT_PUBLIC",  "The group is not public.",                                            false, None)
}
// format: ON
