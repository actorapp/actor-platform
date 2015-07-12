package im.actor.server.activation.gate

sealed trait CodeResponse
final case class CodeHash(hash: String) extends CodeResponse
final case class CodeError(message: String) extends CodeResponse