package im.actor.server.model

import java.time.LocalDateTime

/**
 * Model representing OAuth2 token.
 * @param id unique identifier
 * @param userId resource identifier of user, to whom this token was generated (possibly email, or user name)
 * @param accessToken  The access token issued
 * @param tokenType  The type of the token issued
 * @param expiresIn  The lifetime in seconds of the access token
 * @param refreshToken  The refresh token, which can be used to obtain new access tokens. Contains only in first response.
 * @param createdAt Date when `OAuth2Token` was registered on server
 */
case class OAuth2Token(
  id:           Long,
  userId:       String,
  accessToken:  String,
  tokenType:    String,
  expiresIn:    Long,
  refreshToken: Option[String],
  createdAt:    LocalDateTime
)

