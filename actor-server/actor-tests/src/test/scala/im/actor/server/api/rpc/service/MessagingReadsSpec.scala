package im.actor.server.api.rpc.service

import java.time.Instant

import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.messaging.{ ApiTextMessage, UpdateMessage, UpdateMessageReadByMe }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType }
import im.actor.api.rpc.{ AuthData, ClientData, PeersImplicits }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.sequence.SeqStateDate

class MessagingReadsSpec
  extends BaseAppSuite
  with ImplicitSequenceService
  with ImplicitAuthService
  with ImplicitSessionRegion
  with SeqUpdateMatchers
  with PeersImplicits {

  behavior of "Reads in messaging"

  it should "receive updates and update dialog data in database" in e1()

  it should "make read when user send read on his own message" in e2()

  implicit val service = messaging.MessagingServiceImpl()

  def e1(): Unit = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val client1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val client2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

    val user2AccessHash = ACLUtils.userAccessHash(client1.authId, user2.id, getUserModel(user2.id).accessSalt)
    val user2OutPeer = ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

    val user1AccessHash = ACLUtils.userAccessHash(client2.authId, user1.id, getUserModel(user1.id).accessSalt)
    val user1OutPeer = ApiOutPeer(ApiPeerType.Private, user1.id, user1AccessHash)

    val states_1_3 = {
      implicit val client = client1
      (1 to 3) map { i ⇒
        sendPrivateMessage(user2.id, ApiTextMessage(s"Hello $i", Vector.empty, None))
      }
    }

    {
      implicit val client = client2

      val lastDate = states_1_3.last.date

      expectUpdates(classOf[UpdateMessage]) {
        case Seq(_: UpdateMessage, _: UpdateMessage, _: UpdateMessage) ⇒
        case _ ⇒ fail("Unmatched UpdateMessage updates")
      }

      expectUpdates(classOf[UpdateCountersChanged]) {
        case counters @ Seq(c3: UpdateCountersChanged) ⇒
          val cs = List(c3) flatMap (_.counters.globalCounter)
          cs should contain theSameElementsAs List(3)
        case _ ⇒ fail("Unmatched UpdateCountersChanged updates")
      }

      val dialog = findPrivateDialog(user1.id)

      dialog.lastMessageDate shouldEqual Instant.ofEpochMilli(lastDate)
      dialog.lastReadDate shouldEqual Instant.ofEpochMilli(0)
      // dialog.ownerLastReadAt shouldEqual Instant.ofEpochMilli(0)

      val seq = whenReady(sequenceService.handleGetState(Vector.empty)) {
        _.toOption.get.seq
      }

      // read dialog
      whenReady(service.handleMessageRead(user1OutPeer, lastDate))(identity)

      expectUpdate(seq, classOf[UpdateMessageReadByMe])(identity)
      expectUpdate(seq, classOf[UpdateCountersChanged]) { upd ⇒
        upd.counters.globalCounter shouldBe defined
        val counter = upd.counters.globalCounter.get
        counter shouldEqual 0
      }

      val dialogAfter = findPrivateDialog(user1.id)

      dialogAfter.lastMessageDate shouldEqual Instant.ofEpochMilli(lastDate)
      //      dialog.lastReadAt shouldEqual new DateTime(lastDate) //why not?
      // dialogAfter.ownerLastReadAt shouldEqual new DateTime(lastDate)
    }
  }

  def e2() = {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()

    val sessionId = createSessionId()
    val client1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
    val client2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

    val user2AccessHash = ACLUtils.userAccessHash(client1.authId, user2.id, getUserModel(user2.id).accessSalt)
    val user2OutPeer = ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

    val user1AccessHash = ACLUtils.userAccessHash(client2.authId, user1.id, getUserModel(user1.id).accessSalt)
    val user1OutPeer = ApiOutPeer(ApiPeerType.Private, user1.id, user1AccessHash)

    {
      implicit val client = client1
      sendPrivateMessage(user2.id, ApiTextMessage("User 1 sends message", Vector.empty, None))
    }

    {
      implicit val client = client2
      sendPrivateMessage(user1.id, ApiTextMessage("User replies to this message", Vector.empty, None))
    }

    {
      implicit val client = client1
      sendPrivateMessage(user2.id, ApiTextMessage("And user 1 sends two more messages. This one", Vector.empty, None))
      val SeqStateDate(_, _, messageDate) =
        sendPrivateMessage(user2.id, ApiTextMessage("And this one. His client will send read on this message", Vector.empty, None))

      val dialog = findPrivateDialog(user2.id)

      dialog.lastMessageDate shouldEqual Instant.ofEpochMilli(messageDate)
      dialog.lastReadDate shouldEqual Instant.ofEpochMilli(0)
      //dialog.ownerLastReadAt shouldEqual new DateTime(0)

      val currentSeq = whenReady(sequenceService.handleGetState(Vector.empty)) { _.toOption.get.seq }

      whenReady(service.handleMessageRead(user2OutPeer, messageDate))(identity)

      expectUpdate(currentSeq, classOf[UpdateMessageReadByMe])(identity)
      expectUpdate(currentSeq, classOf[UpdateCountersChanged]) { upd ⇒
        upd.counters.globalCounter shouldBe defined
        val counter = upd.counters.globalCounter.get
        counter shouldEqual 0
      }

      val dialogAfter = findPrivateDialog(user2.id)

      dialogAfter.lastMessageDate shouldEqual Instant.ofEpochMilli(messageDate)
      //      dialog.lastReadAt shouldEqual new DateTime(lastDate) //why not?
      //dialogAfter.ownerLastReadAt shouldEqual new DateTime(messageDate)
    }
  }

}
