package im.actor.server.model

import java.time.{ ZoneOffset, LocalDateTime }

@SerialVersionUID(1L)
case class AuthCode(
  transactionHash: String,
  code:            String,
  attempts:        Int           = 0,
  createdAt:       LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
  isDeleted:       Boolean       = false
)