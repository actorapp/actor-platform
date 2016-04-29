package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
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

  "Dialogs" should "appear in bottom on new incoming message" in incomingGoBottom
  it should "appear in bottom on new outgoing message" in outgoingGoBottom

  "Hidden dialogs" should "appear on new message" in appearHidden
  it should "appear when peer sends message to dialog" in appearHidden2
  it should "appear on show" in appearShown

  "Favourited dialogs" should "appear on favourite" in appearFavourite
  it should "not be in grouped dialogs if no favourites left" in noGroupInFavAbsent

  "Archived dialogs" should "be loaded by desc order" in archived

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
      whenReady(service.handleLoadGroupedDialogs()) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadGroupedDialogs(dgroups, users, groups, _, _)) ⇒
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

  def incomingGoBottom() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val (eve, eveAuthId, eveAuthSid, _) = createUser()

    val aliceClient = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClient = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))
    val eveClient = ClientData(eveAuthId, 1, Some(AuthData(eve.id, eveAuthSid, 42)))

    {
      implicit val clientData = eveClient
      sendMessageToUser(alice.id, ApiTextMessage("Hi, I am Eve", Vector.empty, None))
    }

    Thread.sleep(1)

    {
      implicit val clientData = bobClient
      sendMessageToUser(alice.id, ApiTextMessage("Hi, I am Bob", Vector.empty, None))
    }

    {
      implicit val clientData = aliceClient
      val dgs = getDialogGroups()
      val privates = dgs(DialogExtension.groupKey(DialogGroupType.DirectMessages))
      privates.size should equal(2)
      privates.head.peer.id should equal(eve.id)
      privates.last.peer.id should equal(bob.id)
    }

    {
      implicit val clientData = eveClient
      sendMessageToUser(alice.id, ApiTextMessage("Hi, I am Eve", Vector.empty, None))
    }

    {
      implicit val clientData = aliceClient
      val privates = getDialogGroups(DialogGroupType.DirectMessages)
      privates.head.peer.id should equal(eve.id)
    }
  }

  def outgoingGoBottom() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()
    val (eve, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

    prepareDialogs(bob, eve)

    inside(getDialogGroups(DialogGroupType.DirectMessages)) {
      case privates ⇒
        privates.head.peer.id should equal(bob.id)
    }

    sendMessageToUser(eve.id, textMessage("Grrr"))

    inside(getDialogGroups(DialogGroupType.DirectMessages)) {
      case privates ⇒
        privates.head.peer.id should equal(bob.id)
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
    inside(getDialogGroups(DialogGroupType.DirectMessages)) {
      case Vector(d1, d2) ⇒
        d1.peer.id should equal(eve.id)
        d2.peer.id should equal(bob.id)
    }
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
      inside(getDialogGroups(DialogGroupType.DirectMessages)) {
        case Vector(d1, d2) ⇒
          d1.peer.id should equal(eve.id)
          d2.peer.id should equal(bob.id)
      }
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
    inside(getDialogGroups(DialogGroupType.DirectMessages)) {
      case Vector(d1, d2) ⇒
        d1.peer.id should equal(eve.id)
        d2.peer.id should equal(bob.id)
    }
  }

  def appearFavourite() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, _, _, _) = createUser()

    implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobPeer = getOutPeer(bob.id, aliceAuthId)
    sendMessageToUser(bob.id, textMessage("Hi Bob!"))

    prepareDialogs(bob)
    whenReady(service.handleFavouriteDialog(bobPeer))(identity)

    whenReady(service.handleLoadGroupedDialogs()) { resp ⇒
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
}