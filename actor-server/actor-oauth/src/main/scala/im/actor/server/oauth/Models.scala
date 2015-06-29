package im.actor.server.oauth

import java.time.{ ZoneOffset, LocalDateTime }

case class Token(
  accessToken:  String,
  tokenType:    String,
  expiresIn:    Long,
  refreshToken: Option[String],
  createdAt:    LocalDateTime  = LocalDateTime.now(ZoneOffset.UTC)
)

case class Profile(
  email:      String,
  familyName: Option[String],
  name:       Option[String],
  givenName:  Option[String],
  picture:    Option[String],
  gender:     Option[String],
  locale:     Option[String]
)