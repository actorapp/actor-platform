package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.{ CommonRpcErrors, RpcError }

// format: OFF
object GroupRpcErrors {
  val AlreadyInvited        = RpcError(400, "USER_ALREADY_INVITED",   "You are already invited to this group.",                              false, None)
  val AlreadyJoined         = RpcError(400, "USER_ALREADY_JOINED",    "You are already a member of this group.",                             false, None)
  val NotAMember            = RpcError(403, "FORBIDDEN",              "You are not a group member.",                                         false, None)
  val InvalidTitle          = RpcError(400, "GROUP_TITLE_INVALID",    "Invalid group title.",                                                false, None)
  val TopicTooLong          = RpcError(400, "GROUP_TOPIC_TOO_LONG",   "Group topic is too long. It should be no longer then 255 characters", false, None)
  val AboutTooLong          = RpcError(400, "GROUP_ABOUT_TOO_LONG",   "Group about is too long. It should be no longer then 255 characters", false, None)
  val UserAlreadyAdmin      = RpcError(400, "USER_ALREADY_ADMIN",     "User is already admin of this group",                                 false, None)
  val UserAlreadyNotAdmin   = RpcError(400, "USER_ALREADY_NOT_ADMIN", "User is already notadmin of this group",                              false, None)
  val BlockedByUser         = RpcError(403, "BLOCKED_BY_USER",        "User blocked you, unable to invite him.",                             false, None)
  val GroupIdAlreadyExists  = RpcError(400, "GROUP_ALREADY_EXISTS",   "Group with such id already exists.",                                  false, None)
  val InvalidInviteUrl      = RpcError(403, "INVALID_INVITE_URL",     "Invalid invite url!",                                                 false, None)
  val InvalidInviteToken    = RpcError(403, "INVALID_INVITE_TOKEN",   "Invalid invite token!",                                               false, None)
  val InvalidInviteGroup    = RpcError(403, "INVALID_INVITE_GROUP",   "Invalid group name provided!",                                        false, None)
  val GroupNotPublic        = RpcError(400, "GROUP_IS_NOT_PUBLIC",    "The group is not public.",                                            false, None)
  val CantLeaveGroup        = RpcError(403, "CANT_LEAVE_GROUP",       "You can't leave this group!",                                         false, None)
  val CantJoinGroup         = RpcError(403, "CANT_JOIN_GROUP",        "You can't join this group!",                                          false, None)
  val CantGrantToBot        = RpcError(400, "CANT_GRANT_TO_BOT",      "Can't grant this permissions to bot",                                 false, None)
  val UserIsBanned          = RpcError(403, "USER_IS_BANNED",         "You can't join this group.",                                          false, None)

  val InvalidShortName      = RpcError(400, "GROUP_SHORT_NAME_INVALID",
    "Invalid group short name. Valid short name should contain from 5 to 32 characters, and may consist of latin characters, numbers and underscores", false, None)
  val ShortNameTaken        = RpcError(400, "GROUP_SHORT_NAME_TAKEN", "This short name already belongs to other user or group, we are sorry!", false, None)
  val NoPermission          = CommonRpcErrors.forbidden("You have no permission to execute this action")
}
// format: ON
