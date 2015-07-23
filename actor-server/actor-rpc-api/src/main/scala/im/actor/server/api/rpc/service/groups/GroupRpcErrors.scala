package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.RpcError

object GroupRpcErrors {
  val UserAlreadyInvited = RpcError(400, "USER_ALREADY_INVITED", "User is already a member of the group.", false, None)
  val WrongGroupTitle = RpcError(400, "WRONG_GROUP_TITLE", "Can't create group with such title.", false, None)
}
