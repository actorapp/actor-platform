package im.actor.server.email

import com.typesafe.config.Config

case class EmailConfig(
  hostname: String,
  smtpPort: Int,
  username: String,
  password: String,
  from:     String
)

object EmailConfig {
  def fromConfig(config: Config): EmailConfig =
    EmailConfig(
      config.getString("hostname"),
      config.getInt("smtp-port"),
      config.getString("username"),
      config.getString("password"),
      config.getString("from")
    )
}
