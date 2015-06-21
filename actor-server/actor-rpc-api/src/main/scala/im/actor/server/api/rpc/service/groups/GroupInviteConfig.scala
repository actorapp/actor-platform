package im.actor.server.api.rpc.service.groups

import com.typesafe.config.Config

case class GroupInviteConfig(baseUrl: String)

object GroupInviteConfig {
  def fromConfig(config: Config): GroupInviteConfig =
    GroupInviteConfig(config.getString("base-uri"))
}