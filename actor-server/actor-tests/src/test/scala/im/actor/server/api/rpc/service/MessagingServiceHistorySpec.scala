package im.actor.server.api.rpc.service

import java.time.Instant

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.groups.{ UpdateGroupInviteObsolete, UpdateGroupUserInvitedObsolete }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiPeerType }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }

import scala.concurrent.Future
import scala.util.Random

final class MessagingServiceHistorySpec extends BaseAppSuite with GroupsServiceHelpers
  with ImplicitSessionRegion
  with ImplicitAuthService
  with ImplicitSequenceService
  with SeqUpdateMatchers {
  behavior of "MessagingServiceHistoryService"

  "Private messaging" should "load history" in s.privat

  it should "load dialogs" in s.dialogs // TODO: remove this test's dependency on previous example

  it should "mark messages received and send updates" in s.historyPrivate.markReceived

  it should "mark messages read and send updates" in s.historyPrivate.markRead

  it should "be correct counter after read" in s.historyPrivate.counterAfterRead

  "Group messaging" should "mark messages received and send updates" in s.historyGroup.markReceived

  it should "mark messages read and send updates" in s.historyGroup.markRead

  it should "Load all history in public groups" in s.public

  private val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit private lazy val service = messaging.MessagingServiceImpl()
  implicit private lazy val groupsService = new GroupsServiceImpl(groupInviteConfig)

  private object s {
    lazy val (user1, authId1, authSid1, _) = createUser()
    lazy val sessionId1 = createSessionId()

    lazy val (user2, authId2, authSid2, _) = createUser()
    lazy val sessionId2 = createSessionId()

    lazy val clientData1 = ClientData(authId1, sessionId1, Some(AuthData(user1.id, authSid1, 42)))
    lazy val clientData2 = ClientData(authId2, sessionId2, Some(AuthData(user2.id, authSid2, 42)))

    lazy val user1Model = getUserModel(user1.id)
    lazy val user1AccessHash = ACLUtils.userAccessHash(authId2, user1.id, user1Model.accessSalt)
    lazy val user1Peer = peers.ApiOutPeer(ApiPeerType.Private, user1.id, user1AccessHash)

    lazy val user2Model = getUserModel(user2.id)
    lazy val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, user2Model.accessSalt)
    lazy val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

    def privat() = {
      val step = 100L

      val (message1Date, message2Date, message3Date) = {
        implicit val clientData = clientData1

        val message1Date = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 1", Vector.empty, None), None, None))(_.toOption.get.date)

        Thread.sleep(step)

        val message2Date = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 2", Vector.empty, None), None, None))(_.toOption.get.date)

        Thread.sleep(step)

        val message3Date = whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 3", Vector.empty, None), None, None))(_.toOption.get.date)

        Thread.sleep(step)

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 4", Vector.empty, None), None, None))(_ ⇒ ())

        (message1Date, message2Date, message3Date)
      }

      Thread.sleep(300)

      {
        implicit val clientData = clientData1
        // this should not affect `handleLoadHistory` for this user
        whenReady(service.handleMessageRead(user2Peer, message3Date))(identity)
      }

      Thread.sleep(300)

      {
        implicit val clientData = clientData2

        whenReady(service.handleMessageReceived(user1Peer, message2Date)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseVoid) ⇒
          }
        }

        whenReady(service.handleMessageRead(user1Peer, message1Date)) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseVoid) ⇒
          }
        }
      }

      Thread.sleep(1000)

      {
        implicit val clientData = clientData1

        whenReady(service.handleLoadHistory(user2Peer, message3Date, None, 100, Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(_) ⇒
          }
          val respBody = resp.toOption.get

          respBody.users.length should ===(0)
          respBody.history.length should ===(3)
          respBody.history.map(_.state) should ===(Seq(Some(ApiMessageState.Sent), Some(ApiMessageState.Received), Some(ApiMessageState.Read)))
        }
      }
    }

    def dialogs() = {
      {
        implicit val clientData = clientData1

        // Archiving should not hide from LoadDialogs
        whenReady(service.handleArchiveChat(user2Peer))(identity)

        whenReady(service.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(_) ⇒
          }

          val respBody = resp.toOption.get

          respBody.dialogs.length should ===(1)
          val dialog = respBody.dialogs.head
          dialog.unreadCount should ===(0)
          respBody.users.length should ===(2)
        }
      }

      {
        implicit val clientData = clientData2

        whenReady(service.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
          resp should matchPattern {
            case Ok(_) ⇒
          }

          val respBody = resp.toOption.get

          respBody.dialogs.length should ===(1)
          val dialog = respBody.dialogs.head
          dialog.unreadCount should ===(3)
          respBody.users.length should ===(1)
        }
      }
    }

    def public() = {
      val groupId = Random.nextInt
      val (pubUser, _, authSid, _) = createUser()
      val accessHash = whenReady(GroupExtension(system).create(groupId, pubUser.id, authSid, "Public group", Random.nextLong, Set.empty))(_.accessHash)
      whenReady(GroupExtension(system).makePublic(groupId, "Public group description"))(identity)

      val groupOutPeer = ApiGroupOutPeer(groupId, accessHash)

      val firstMessage = ApiTextMessage("First", Vector.empty, None)
      val secondMessage = ApiTextMessage("Second", Vector.empty, None)

      {
        implicit val clientData = clientData1
        whenReady(groupsService.handleEnterGroupObsolete(groupOutPeer))(identity)
        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), firstMessage, None, None))(identity)
      }

      {
        implicit val clientData = clientData2
        whenReady(groupsService.handleEnterGroupObsolete(groupOutPeer))(identity)
        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), secondMessage, None, None))(identity)

        Thread.sleep(2000)

        whenReady(service.handleLoadHistory(groupOutPeer.asOutPeer, 0, None, 100, Vector.empty)) { resp ⇒
          val history = resp.toOption.get.history
          //history does not contain message about group creation, as group was not created by Zero user
          history.length shouldEqual 4
          history.map(_.message) should contain allOf (firstMessage, secondMessage)
        }
      }
    }

    object historyPrivate {
      val (user1, authId1, authSid1, _) = createUser()

      def markReceived() = {
        val (user2, authId2, authSid2, _) = createUser()
        val sessionId = createSessionId()

        val clientData1 = ClientData(authId1, sessionId1, Some(AuthData(user1.id, authSid1, 42)))
        val clientData2 = ClientData(authId2, sessionId2, Some(AuthData(user2.id, authSid2, 42)))

        val user1AccessHash = ACLUtils.userAccessHash(authId2, user1.id, getUserModel(user1.id).accessSalt)
        val user1Peer = peers.ApiOutPeer(ApiPeerType.Private, user1.id, user1AccessHash)

        val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, getUserModel(user2.id).accessSalt)
        val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

        val startDate = {
          implicit val clientData = clientData1

          val startDate = System.currentTimeMillis()

          val sendMessages = Future.sequence(Seq(
            service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 1", Vector.empty, None), None, None),
            futureSleep(1500).flatMap(_ ⇒ service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 2", Vector.empty, None), None, None)),
            futureSleep(3000).flatMap(_ ⇒ service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 3", Vector.empty, None), None, None))
          ))

          whenReady(sendMessages)(_ ⇒ ())

          startDate
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageReceived(user1Peer, startDate + 2000)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(100) // Let peer managers write to db

          whenReady(dialogExt.getDialogInfo(user1.id, Peer(PeerType.Private, user2.id))) { info ⇒
            info.lastReceivedDate.toEpochMilli should be < startDate + 3000
            info.lastReceivedDate.toEpochMilli should be > startDate + 1000
          }
        }

        {
          implicit val clientData = clientData1
          expectUpdatesUnordered(
            classOf[UpdateChatGroupsChanged],
            classOf[UpdateMessageSent],
            classOf[UpdateMessageSent],
            classOf[UpdateMessageSent],
            classOf[UpdateMessageReceived]
          )(emptyCheck)
        }
      }

      def markRead() = {
        val (user1, authId1, authSid1, _) = createUser()
        val (user2, authId21, authSid21, _) = createUser()
        val sessionId = createSessionId()

        val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
        val clientData21 = ClientData(authId21, sessionId, Some(AuthData(user2.id, authSid21, 42)))

        val (authId22, authSid22) = createAuthId(user2.id)

        val clientData22 = ClientData(authId22, sessionId, Some(AuthData(user2.id, authSid22, 42)))

        val user1AccessHash = ACLUtils.userAccessHash(authId21, user1.id, getUserModel(user1.id).accessSalt)
        val user1Peer = peers.ApiOutPeer(ApiPeerType.Private, user1.id, user1AccessHash)

        val (date1, date2, date3) = {
          implicit val clientData = clientData1

          sendMessageToUser(user2.id, ApiTextMessage("Hi Shiva 1", Vector.empty, None))
          val date1 = Instant.now()
          Thread.sleep(100)

          sendMessageToUser(user2.id, ApiTextMessage("Hi Shiva 2", Vector.empty, None))
          val date2 = Instant.now()
          Thread.sleep(100)

          sendMessageToUser(user2.id, ApiTextMessage("Hi Shiva 3", Vector.empty, None))
          val date3 = Instant.now()

          (date1, date2, date3)
        }

        {
          implicit val clientData = clientData21

          whenReady(service.handleMessageRead(user1Peer, date2.plusMillis(1).toEpochMilli)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(100) // Let peer managers write to db

          whenReady(dialogExt.getDialogInfo(user1.id, Peer(PeerType.Private, user2.id))) { info ⇒
            info.lastReadDate.toEpochMilli should be < date3.toEpochMilli
            info.lastReadDate.toEpochMilli should be > date1.toEpochMilli
          }

          whenReady(service.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
            val dialog = resp.toOption.get.dialogs.head

            dialog.unreadCount shouldEqual 1
          }
        }

        {
          implicit val clientData = clientData1
          expectUpdatesUnordered(
            classOf[UpdateChatGroupsChanged],
            classOf[UpdateMessageSent],
            classOf[UpdateMessageSent],
            classOf[UpdateMessageSent],
            classOf[UpdateMessageRead]
          )(emptyCheck)
        }

        {
          implicit val clientData = clientData21
          expectUpdatesUnordered(
            classOf[UpdateChatGroupsChanged],
            classOf[UpdateMessage],
            //classOf[UpdateCountersChanged],
            classOf[UpdateMessage],
            //classOf[UpdateCountersChanged],
            classOf[UpdateMessage],
            //classOf[UpdateCountersChanged],
            classOf[UpdateMessageReadByMe],
            classOf[UpdateCountersChanged]
          )(emptyCheck)
        }

        {
          //UpdateMessageReadByMe sent to user2 second device
          implicit val clientData = clientData22
          expectUpdatesUnordered(
            classOf[UpdateChatGroupsChanged],
            classOf[UpdateMessage],
            //classOf[UpdateCountersChanged],
            classOf[UpdateMessage],
            //classOf[UpdateCountersChanged],
            classOf[UpdateMessage],
            //classOf[UpdateCountersChanged],
            classOf[UpdateMessageReadByMe],
            classOf[UpdateCountersChanged]
          )(emptyCheck)
        }
      }

      def counterAfterRead() = {
        val (user1, authId1, authSid1, _) = createUser()
        val (user2, authId21, authSid21, _) = createUser()
        val sessionId = createSessionId()

        val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
        val clientData21 = ClientData(authId21, sessionId, Some(AuthData(user2.id, authSid21, 42)))

        val user1AccessHash = ACLUtils.userAccessHash(authId21, user1.id, getUserModel(user1.id).accessSalt)
        val user1Peer = peers.ApiOutPeer(ApiPeerType.Private, user1.id, user1AccessHash)

        val user2AccessHash = ACLUtils.userAccessHash(authId1, user2.id, getUserModel(user2.id).accessSalt)
        val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

        val startDate = {
          implicit val clientData = clientData1

          whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi Shiva 1", Vector.empty, None), None, None)) { resp ⇒
            val seqStateDate = resp.toOption.get
            seqStateDate.date
          }
        }

        {
          implicit val clientData = clientData21

          Thread.sleep(300)

          val ResponseSeq(seq, state) = {
            whenReady(sequenceService.handleGetState(Vector.empty)) { resp ⇒
              resp.toOption.get
            }
          }

          whenReady(service.handleMessageRead(user1Peer, startDate)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          expectUpdate(seq, classOf[UpdateCountersChanged]) { upd ⇒
            val globalCounter = upd.counters.globalCounter
            globalCounter shouldEqual Some(0)
          }
        }
      }
    }

    object historyGroup {
      def markReceived() = {
        val (user1, authId1, authSid1, _) = createUser()
        val (user2, authId2, authSid2, _) = createUser()
        val sessionId = createSessionId()

        val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
        val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

        val groupOutPeer = {
          implicit val clientData = clientData1
          createGroup("Fun group", Set(user2.id)).groupPeer
        }

        val startDate = System.currentTimeMillis()

        val (date1, date2, date3) = {
          implicit val clientData = clientData1

          sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("Hi Shiva 1", Vector.empty, None))
          val date1 = Instant.now()
          Thread.sleep(100)

          sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("Hi Shiva 2", Vector.empty, None))
          val date2 = Instant.now()
          Thread.sleep(100)

          sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("Hi Shiva 1", Vector.empty, None))
          val date3 = Instant.now()

          (date1, date2, date3)
        }

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageReceived(groupOutPeer.asOutPeer, date2.plusMillis(1).toEpochMilli)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(100) // Let peer managers write to db

          whenReady(dialogExt.getDialogInfo(user1.id, Peer(PeerType.Group, groupOutPeer.groupId))) { info ⇒
            info.lastReceivedDate.toEpochMilli should be < date3.toEpochMilli
            info.lastReceivedDate.toEpochMilli should be > date1.toEpochMilli
          }
        }

        {
          implicit val clientData = clientData1
          expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
          expectUpdate(classOf[UpdateGroupUserInvitedObsolete])(identity)
          expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
          expectUpdate(classOf[UpdateMessageSent])(identity)
          expectUpdate(classOf[UpdateMessageSent])(identity)
          expectUpdate(classOf[UpdateMessageSent])(identity)
          expectUpdate(classOf[UpdateMessageReceived])(identity)
        }
      }

      def markRead() = {
        val (user1, authId1, authSid1, _) = createUser()
        val (user2, authId2, authSid2, _) = createUser()
        val sessionId = createSessionId()

        val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
        val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

        val groupOutPeer = {
          implicit val clientData = clientData1
          createGroup("Fun group", Set(user2.id)).groupPeer
        }

        val (date1, date2, date3) = {
          implicit val clientData = clientData1

          sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("Hi Shiva 1", Vector.empty, None))
          val date1 = Instant.now()
          Thread.sleep(100)

          sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("Hi Shiva 2", Vector.empty, None))
          val date2 = Instant.now()
          Thread.sleep(100)

          sendMessageToGroup(groupOutPeer.groupId, ApiTextMessage("Hi Shiva 3", Vector.empty, None))
          val date3 = Instant.now()

          (date1, date2, date3)
        }

        Thread.sleep(300)

        {
          implicit val clientData = clientData2

          whenReady(service.handleMessageRead(groupOutPeer.asOutPeer, date2.plusMillis(1).toEpochMilli)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseVoid) ⇒
            }
          }

          Thread.sleep(300)

          whenReady(dialogExt.getDialogInfo(user1.id, Peer(PeerType.Group, groupOutPeer.groupId))) { info ⇒
            info.lastReadDate.toEpochMilli should be < date3.toEpochMilli
            info.lastReadDate.toEpochMilli should be > date1.toEpochMilli
          }

          whenReady(service.handleLoadDialogs(Long.MaxValue, 100, Vector.empty)) { resp ⇒
            val dialog = resp.toOption.get.dialogs.head
            dialog.unreadCount shouldEqual 1
          }
        }

        {
          implicit val clientData = clientData1
          expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
          expectUpdate(classOf[UpdateGroupUserInvitedObsolete])(identity)
          expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)
          expectUpdate(classOf[UpdateMessageSent])(identity)
          expectUpdate(classOf[UpdateMessageSent])(identity)
          expectUpdate(classOf[UpdateMessageSent])(identity)
          expectUpdate(classOf[UpdateMessageRead])(identity)
          expectUpdate(classOf[UpdateMessage])(identity)
          expectUpdate(classOf[UpdateCountersChanged])(identity)
        }

        {
          implicit val clientData = clientData2
          expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
          expectUpdate(classOf[UpdateGroupInviteObsolete])(identity)

          expectUpdate(classOf[UpdateCountersChanged])(identity)
          expectUpdate(classOf[UpdateMessage])(identity)

          expectUpdate(classOf[UpdateCountersChanged])(identity)
          expectUpdate(classOf[UpdateMessage])(identity)

          expectUpdate(classOf[UpdateCountersChanged])(identity)
          expectUpdate(classOf[UpdateMessage])(identity)

          expectUpdate(classOf[UpdateMessageSent])(identity) //sent message with GroupServiceMessages.userJoined

          expectUpdate(classOf[UpdateMessageReadByMe])(identity)
          expectUpdate(classOf[UpdateCountersChanged])(identity)
        }
      }
    }

  }

}
