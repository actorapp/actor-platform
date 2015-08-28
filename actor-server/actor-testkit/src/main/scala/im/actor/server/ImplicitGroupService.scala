package im.actor.server

import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }

trait ImplicitGroupService extends ImplicitPresenceRegions {
  val groupInviteConfig = GroupInviteConfig("https://actor.im")

  implicit val groupService: GroupsServiceImpl = new GroupsServiceImpl(groupInviteConfig)
}