package im.actor.server.model.auth

case class GateAuthCode(transactionHash: String, codeHash: String, isDeleted: Boolean = false)
