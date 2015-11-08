package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiDialogShort, ResponseLoadGroupedDialogs, ApiTextMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType, ApiOutPeer }
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.{ GroupsServiceHelpers, ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }
import org.scalatest.Inside._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.util.Random

final class GroupedDialogsSpec
  extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with GroupsServiceHelpers {
  "LoadGroupedDialogs" should "load groups and privates" in loadGrouped

  private implicit val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))
  private val service = MessagingServiceImpl()

  def loadGrouped() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, _, _, _) = createUser()
    val (user3, _, _, _) = createUser()

    implicit val clientData = ClientData(authId1, 1, Some(AuthData(user1.id, authSid1)))

    val group = createGroup("Some group", Set(user3.id))

    val user2Peer = Await.result(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, user2.id), authId1), 5.seconds)
    val groupPeer = ApiOutPeer(ApiPeerType.Group, group.groupPeer.groupId, group.groupPeer.accessHash)

    whenReady(Future.sequence(Seq(
      service.handleSendMessage(user2Peer, Random.nextLong, ApiTextMessage("Hi there", Vector.empty, None)),
      service.handleSendMessage(groupPeer, Random.nextLong, ApiTextMessage("Hi all there", Vector.empty, None))
    ))) { _ ⇒
      whenReady(service.handleLoadGroupedDialogs()) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadGroupedDialogs(dgroups, users, groups)) ⇒
            dgroups.length shouldBe 2

            users.map(_.id).toSet shouldBe Set(user1.id, user2.id, user3.id)

            groups.map(_.id).toSet shouldBe Set(group.groupPeer.groupId)

            val (gs, ps) = dgroups.foldLeft(IndexedSeq.empty[ApiDialogShort], IndexedSeq.empty[ApiDialogShort]) {
              case ((gs, ps), dg) ⇒
                dg.key match {
                  case "groups"   ⇒ (dg.dialogs, ps)
                  case "privates" ⇒ (gs, dg.dialogs)
                  case unknown    ⇒ throw new RuntimeException(s"Unknown dialog group key $unknown")
                }
            }

            inside(gs) {
              case Vector(g) ⇒ g.peer.id shouldBe group.groupPeer.groupId
            }

            inside(ps) {
              case Vector(p) ⇒ p.peer.id shouldBe user2.id
            }
        }
      }
    }
  }
}