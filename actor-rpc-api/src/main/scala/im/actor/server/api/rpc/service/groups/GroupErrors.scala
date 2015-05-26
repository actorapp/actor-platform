package im.actor.server.api.rpc.service.groups

import im.actor.api.rpc.RpcError

object GroupErrors {
  val UserAlreadyInvited = RpcError(400, "USER_ALREADY_INVITED", "User is already a member of the group.", false, None)
}
