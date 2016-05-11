package im.actor.api.rpc

import im.actor.concurrent.FutureResult

trait FutureResultRpc extends FutureResult[RpcError]

object FutureResultRpc extends FutureResultRpc