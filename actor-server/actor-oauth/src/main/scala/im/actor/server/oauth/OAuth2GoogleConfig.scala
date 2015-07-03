package im.actor.server.oauth

import com.typesafe.config.Config

sealed trait OAuth2Config {
  def authUri: String
  def tokenUri: String
  def profileUri: String
  def clientId: String
  def clientSecret: String
}

case class OAuth2GoogleConfig(
  authUri:      String,
  tokenUri:     String,
  profileUri:   String,
  clientId:     String,
  clientSecret: String,
  scope:        String
) extends OAuth2Config

object OAuth2GoogleConfig {
  def load(config: Config): OAuth2GoogleConfig =
    OAuth2GoogleConfig(
      config.getString("auth-uri"),
      config.getString("token-uri"),
      config.getString("profile-uri"),
      config.getString("client-id"),
      config.getString("client-secret"),
      config.getString("scope")
    )
}