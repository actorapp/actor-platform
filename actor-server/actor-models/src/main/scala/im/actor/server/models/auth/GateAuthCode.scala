package im.actor.server.models.auth

case class GateAuthCode(transactionHash: String, codeHash: String, isDeleted: Boolean = false)
