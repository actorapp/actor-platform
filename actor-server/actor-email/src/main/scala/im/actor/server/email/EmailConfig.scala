package im.actor.server.email

import im.actor.config.ActorConfig

import scala.util.Try
import com.github.kxbmap.configs._
import com.typesafe.config.Config

case class EmailConfig(
  //sender part
  address: String,
  name:    String,
  prefix:  String,

  //smtp part
  host:     String,
  port:     Int,
  username: String,
  password: String,
  tls:      Boolean
)

object EmailConfig {
  def load(config: Config): Try[EmailConfig] =
    for {
      address ← config.get[Try[String]]("sender.address")
      name ← config.get[Try[String]]("sender.name")
      prefix ← config.get[Try[String]]("sender.prefix")

      host ← config.get[Try[String]]("smtp.host")
      port ← config.get[Try[Int]]("smtp.port")
      username ← config.get[Try[String]]("smtp.username")
      password ← config.get[Try[String]]("smtp.password")
      tls ← config.get[Try[Boolean]]("smtp.tls")
    } yield EmailConfig(address, name, prefix, host, port, username, password, tls)

  def load: Try[EmailConfig] = load(ActorConfig.load().getConfig("services.email"))
}