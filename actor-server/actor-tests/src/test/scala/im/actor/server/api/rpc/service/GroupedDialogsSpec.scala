package im.actor.server.api.rpc.service

import cats.data.Xor
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType }
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server._
import im.actor.server.dialog.{ DialogExtension, DialogGroupType }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.util.Random

final class GroupedDialogsSpec
  extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with GroupsServiceHelpers
  with MessagingSpecHelpers {
  "LoadGroupedDialogs" should "load groups and privates" in loadGrouped

  "Hidden dialogs" should "appear on new message" in appearHidden
  it should "appear when peer sends message to dialog" in appearHidden2
  it should "appear on show" in appearShown

  "Favourited dialogs" should "appear on favourite" in appearFavourite
  it should "not be in grouped dialogs if no favourites left" in noGroupInFavAbsent

  "Archived dialogs" should "be loaded by desc order" in archived

  "Deleted dialogs" should "not appear in dialog list, and should mark messages as deleted in db" in deleted

  private implicit lazy val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))
  private implicit lazy val service = MessagingServiceImpl()

  def loadGrouped() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, _, _, _) = createUser()
    val (user3, _, _, _) = createUser()

    implicit val clientData = ClientData(authId1, 1, Some(AuthData(user1.id, authSid1, 42)))
    val group = createGroup("Some group", Set(user3.id))

    val user2Peer = Await.result(ACLUtils.getOutPeer(ApiPeer(ApiPeerType.Private, user2.id), authId1), 5.seconds)
    val groupPeer = ApiOutPeer(ApiPeerType.Group, group.groupPeer.groupId, group.groupPeer.accessHash)

    whenReady(Future.sequence(Seq(
      service.handleSendMessage(user2Peer, Random.nextLong, ApiTextMessage("Hi there", Vector.empty, None), None, None),
      service.handleSendMessage(groupPeer, Random.nextLong, ApiTextMessage("Hi all there", Vector.empty, None), None, None)
    ))) { _ ⇒
      whenReady(service.handleLoadGroupedDialogs(Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadGroupedDialogs(dgroups, users, groups, _, _, _, _)) ⇒
            dgroups.length shouldBe 2

            dgroups.map(_.key) should be(Seq(
              DialogExtension.groupKey(DialogGroupType.Groups),
              DialogExtension.groupKey(DialogGroupType.DirectMessages)
            ))

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

            users.map(_.id).toSet shouldBe Set(user1.id, user2.id, user3.id)

            groups.map(_.id).toSet shouldBe Set(group.groupPeer.groupId)
        }
      }
    }
  }

  def appearHidden() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()
    val (eve, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)
    //    sendMessageToUser(bob.id, textMessage("Hi Bob!"))

    prepareDialogs(bob, eve)
    whenReady(service.handleHideDialog(bobPeer))(identity)
    inside(getDialogGroups(DialogGroupType.DirectMessages)) {
      case Vector(d) ⇒ d.peer.id should equal(eve.id)
    }

    sendMessageToUser(bob.id, textMessage("Hi Bob!"))
    getDialogGroups(DialogGroupType.DirectMessages).map(_.peer.id).toSet should equal(Set(eve.id, bob.id))
  }

  def appearHidden2() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val (eve, _, _, _) = createUser()

    val aliceCD = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobCD = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

    val bobPeer = getOutPeer(bob.id, aliceAuthId)

    {
      implicit val cd = aliceCD
      prepareDialogs(bob, eve)

      whenReady(service.handleHideDialog(bobPeer))(identity)
      inside(getDialogGroups(DialogGroupType.DirectMessages)) {
        case Vector(d) ⇒ d.peer.id should equal(eve.id)
      }
    }

    {
      implicit val cd = bobCD
      sendMessageToUser(alice.id, textMessage("Hi Alice!"))
    }

    {
      implicit val cd = aliceCD
      getDialogGroups(DialogGroupType.DirectMessages).map(_.peer.id).toSet should equal(Set(eve.id, bob.id))
    }
  }

  def appearShown() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()
    val (eve, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)

    prepareDialogs(bob, eve)

    whenReady(service.handleHideDialog(bobPeer))(identity)
    inside(getDialogGroups(DialogGroupType.DirectMessages)) {
      case Vector(d) ⇒ d.peer.id should equal(eve.id)
    }

    whenReady(service.handleShowDialog(bobPeer))(identity)
    getDialogGroups(DialogGroupType.DirectMessages).map(_.peer.id).toSet should equal(Set(eve.id, bob.id))
  }

  def appearFavourite() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)
    sendMessageToUser(bob.id, textMessage("Hi Bob!"))

    prepareDialogs(bob)
    whenReady(service.handleFavouriteDialog(bobPeer))(identity)

    whenReady(service.handleLoadGroupedDialogs(Vector.empty)) { resp ⇒
      resp.toOption.get.dialogs.map(_.key).head should be(DialogExtension.groupKey(DialogGroupType.Favourites))
    }

    inside(getDialogGroups(DialogGroupType.Favourites)) {
      case Vector(d) ⇒ d.peer.id should equal(bob.id)
    }
  }

  def noGroupInFavAbsent() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)
    sendMessageToUser(bob.id, textMessage("Hi Bob!"))

    prepareDialogs(bob)
    whenReady(service.handleFavouriteDialog(bobPeer))(identity)

    getDialogGroups().get(DialogExtension.groupKey(DialogGroupType.Favourites)) should not be empty

    whenReady(service.handleUnfavouriteDialog(bobPeer))(identity)

    getDialogGroups().get(DialogExtension.groupKey(DialogGroupType.Favourites)) shouldBe empty
  }

  def archived() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()
    val (eve, _, _, _) = createUser()
    val (kira, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)
    val evePeer = getOutPeer(eve.id, aliceAuthId)
    val kiraPeer = getOutPeer(kira.id, aliceAuthId)

    prepareDialogs(bob, eve, kira)
    whenReady(service.handleArchiveChat(bobPeer))(identity)
    whenReady(service.handleArchiveChat(evePeer))(identity)
    whenReady(service.handleArchiveChat(kiraPeer))(identity)

    val offset1 = whenReady(service.handleLoadArchived(None, 1, Vector.empty)) { resp ⇒
      val okResp = resp.toOption.get
      okResp.dialogs.size shouldBe 1
      okResp.dialogs.head.peer.id shouldBe kiraPeer.id
      okResp.nextOffset
    }

    val offset2 = whenReady(service.handleLoadArchived(offset1, 1, Vector.empty)) { resp ⇒
      val okResp = resp.toOption.get
      okResp.dialogs.size shouldBe 1
      okResp.dialogs.head.peer.id shouldBe evePeer.id
      okResp.nextOffset
    }

    whenReady(service.handleLoadArchived(offset2, 1, Vector.empty)) { resp ⇒
      val okResp = resp.toOption.get
      okResp.dialogs.size shouldBe 1
      okResp.dialogs.head.peer.id shouldBe bobPeer.id
      okResp.nextOffset
    }
  }

  def deleted() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()
    val (charlie, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)
    val charliePeer = getOutPeer(charlie.id, aliceAuthId)

    prepareDialogs(bob, charlie)

    val mobileBefore = loadDialogs()
    mobileBefore should have length 2

    val groupBefore = getDialogGroups()
    groupBefore("privates") should have length 2

    whenReady(service.handleLoadHistory(charliePeer, 0L, None, 100, Vector.empty)) { resp ⇒
      inside(resp) {
        case Xor.Right(histResp) ⇒ histResp.history should have length 1
      }
    }

    whenReady(service.handleDeleteChat(charliePeer)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseSeq(_, _)) ⇒
      }
    }

    whenReady(service.handleLoadHistory(charliePeer, 0L, None, 100, Vector.empty)) { resp ⇒
      inside(resp) {
        case Xor.Right(histResp) ⇒ histResp.history shouldBe empty
      }
    }

    val mobileAfter = loadDialogs()
    mobileAfter should have length 1
    mobileAfter.head.peer.id shouldEqual bobPeer.id

    val groupAfter = getDialogGroups()
    groupAfter("privates") should have length 1
  }
}