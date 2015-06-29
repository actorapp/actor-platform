package im.actor.server.models

import java.time.{ ZoneOffset, LocalDateTime }

@SerialVersionUID(1L)
case class AuthCode(transactionHash: String, code: String, createdAt: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC))