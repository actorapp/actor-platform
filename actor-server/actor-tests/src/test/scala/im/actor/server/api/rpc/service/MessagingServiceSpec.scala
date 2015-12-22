package im.actor.server.api.rpc.service

import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.testkit.TestProbe
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.pubsub.PeerMessage

import scala.concurrent.Future
import scala.util.Random

class MessagingServiceSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with ImplicitSequenceService
  with ImplicitSessionRegion
  with ImplicitAuthService
  with SeqUpdateMatchers {
  behavior of "MessagingService"

  "Private Messaging" should "send messages" in s.privat.sendMessage

  it should "not repeat message sending with same authId and RandomId" in s.privat.cached

  "Group Messaging" should "send messages" in s.group.sendMessage

  it should "not send messages when user is not in group" in s.group.restrictAlienUser

  it should "publish messages in PubSub" in s.pubsub.publish

  it should "not repeat message sending with same authId and RandomId" in s.group.cached

  object s {
    implicit val ec = system.dispatcher

    val groupInviteConfig = GroupInviteConfig("http://actor.im")

    implicit val service = messaging.MessagingServiceImpl()
    implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)

    object privat {

      def sendMessage() = {
        val (user1, user1AuthId1, user1AuthSid1, _) = createUser()
        val (user1AuthId2, user1AuthSid2) = createAuthId(user1.id)

        val (user2, user2AuthId, user2AuthSid, _) = createUser()
        val user2Model = getUserModel(user2.id)
        val user2AccessHash = ACLUtils.userAccessHash(user1AuthId1, user2.id, user2Model.accessSalt)
        val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

        val sessionId = createSessionId()
        val clientData11 = ClientData(user1AuthId1, sessionId, Some(AuthData(user1.id, user1AuthSid1)))
        val clientData12 = ClientData(user1AuthId2, sessionId, Some(AuthData(user1.id, user1AuthSid2)))
        val clientData2 = ClientData(user2AuthId, sessionId, Some(AuthData(user2.id, user2AuthSid)))

        val randomId = Random.nextLong()

        {
          implicit val clienData = clientData11

          whenReady(service.handleSendMessage(user2Peer, randomId, ApiTextMessage("Hi Shiva", Vector.empty, None))) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseSeqDate(2, _, _)) ⇒
            }
          }

          expectUpdate(classOf[UpdateMessageSent]) { upd ⇒
            upd.peer shouldEqual ApiPeer(ApiPeerType.Private, user2.id)
            upd.randomId shouldEqual randomId
          }
        }

        {
          implicit val clientData = clientData12

          expectUpdate(classOf[UpdateMessage]) { upd ⇒
            upd.peer shouldEqual ApiPeer(ApiPeerType.Private, user2.id)
            upd.randomId shouldEqual randomId
            upd.senderUserId shouldEqual user1.id
          }
        }

        {
          implicit val clientData = clientData2

          expectUpdates(classOf[UpdateChatGroupsChanged], classOf[UpdateMessage], classOf[UpdateCountersChanged]) {
            case Seq(upd: UpdateMessage) ⇒
              upd.peer shouldEqual ApiPeer(ApiPeerType.Private, user1.id)
              upd.randomId shouldEqual randomId
              upd.senderUserId shouldEqual user1.id
          }
        }
      }

      def cached(): Unit = {
        val (user1, user1AuthId, user1AuthSid, _) = createUser()
        val (user2, user2AuthId, user2AuthSid, _) = createUser()

        val clientData1 = ClientData(user1AuthId, createSessionId(), Some(AuthData(user1.id, user1AuthSid)))
        val clientData2 = ClientData(user2AuthId, createSessionId(), Some(AuthData(user2.id, user2AuthSid)))

        val user2Model = getUserModel(user2.id)
        val user2AccessHash = ACLUtils.userAccessHash(user1AuthId, user2.id, user2Model.accessSalt)
        val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

        {
          implicit val clientData = clientData1

          val randomId = Random.nextLong()
          val text = "Hi Shiva"
          val actions = Future.sequence(List(
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None))
          ))

          whenReady(actions) { resps ⇒
            resps foreach (_ should matchPattern { case Ok(ResponseSeqDate(2, _, _)) ⇒ })
          }

          expectUpdate(classOf[UpdateMessageSent])(identity)
        }

        {
          implicit val clientData = clientData2
          expectUpdate(classOf[UpdateChatGroupsChanged])(identity)
          expectUpdate(classOf[UpdateMessage])(identity)
          expectUpdate(classOf[UpdateCountersChanged]) { upd ⇒
            upd.counters.globalCounter shouldEqual Some(1)
          }
        }
      }
    }

    object group {
      val (user1, user1AuthId1, user1AuthSid1, _) = createUser()
      val (user1AuthId2, user1AuthSid2) = createAuthId(user1.id)
      val (user2, user2AuthId, user2AuthSid, _) = createUser()
      val sessionId = createSessionId()

      val clientData11 = ClientData(user1AuthId1, sessionId, Some(AuthData(user1.id, user1AuthSid1)))
      val clientData12 = ClientData(user1AuthId2, sessionId, Some(AuthData(user1.id, user1AuthSid2)))
      val clientData2 = ClientData(user2AuthId, sessionId, Some(AuthData(user2.id, user2AuthSid)))

      val groupResponse = {
        implicit val clientData = clientData11
        createGroup("Fun group", Set(user2.id))
      }

      val groupSeq = groupResponse.seq
      val groupState = groupResponse.state
      val groupOutPeer = groupResponse.groupPeer

      def sendMessage() = {
        val randomId = Random.nextLong()

        {
          implicit val clientData = clientData11

          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, randomId, ApiTextMessage("Hi again", Vector.empty, None))) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseSeqDate(4, _, _)) ⇒
            }
          }

          expectUpdate(seq = groupSeq, classOf[UpdateMessageSent]) { upd ⇒
            upd.peer shouldEqual ApiPeer(ApiPeerType.Group, groupOutPeer.groupId)
            upd.randomId shouldEqual randomId
          }
        }

        {
          implicit val clientData = clientData12

          expectUpdate(classOf[UpdateMessage]) { upd ⇒
            upd.peer shouldEqual ApiPeer(ApiPeerType.Group, groupOutPeer.groupId)
            upd.randomId shouldEqual randomId
            upd.senderUserId shouldEqual user1.id
          }
        }

        {
          implicit val clientData = clientData2

          expectUpdate(classOf[UpdateMessage]) { upd ⇒
            upd.peer shouldEqual ApiPeer(ApiPeerType.Group, groupOutPeer.groupId)
            upd.randomId shouldEqual randomId
            upd.senderUserId shouldEqual user1.id
          }
        }
      }

      def restrictAlienUser() = {
        val (alien, authIdAlien, authSidAlien, _) = createUser()

        val alienClientData = ClientData(user1AuthId1, sessionId, Some(AuthData(alien.id, authSidAlien)))

        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage("Hi again", Vector.empty, None))(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleEditGroupTitle(groupOutPeer, 4L, "Loosers")(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        val (user3, authId3, _, _) = createUser()
        val user3OutPeer = ApiUserOutPeer(user3.id, 11)

        whenReady(groupsService.handleInviteUser(groupOutPeer, 4L, user3OutPeer)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        val fileLocation = ApiFileLocation(1L, 1L)
        whenReady(groupsService.handleEditGroupAvatar(groupOutPeer, 5L, fileLocation)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleRemoveGroupAvatar(groupOutPeer, 5L)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleLeaveGroup(groupOutPeer, 5L)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

      }

      def cached(): Unit = {
        val (user1, user1AuthId, user1AuthSid, _) = createUser()
        val (user2, user2AuthId, user2AuthSid, _) = createUser()
        val sessionId = createSessionId()
        val clientData1 = ClientData(user1AuthId, sessionId, Some(AuthData(user1.id, user1AuthSid)))
        val clientData2 = ClientData(user2AuthId, sessionId, Some(AuthData(user2.id, user2AuthSid)))

        val group2OutPeer = {
          implicit val clientData = clientData1
          createGroup("Fun group 2", Set(user2.id)).groupPeer
        }

        {
          implicit val clientData = clientData1

          val randomId = Random.nextLong()
          val text = "Hi Shiva"
          val actions = Future.sequence(List(
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None)),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None))
          ))

          whenReady(actions) { resps ⇒
            resps foreach (_ should matchPattern { case Ok(ResponseSeqDate(4, _, _)) ⇒ })
          }

          expectUpdate(classOf[UpdateMessageSent])(identity)
        }

        {
          implicit val clientData = clientData2
          expectUpdate(classOf[UpdateMessage])(identity)
          expectUpdate(classOf[UpdateCountersChanged])(identity)
        }
      }
    }

    object pubsub {

      import DistributedPubSubMediator._

      val mediator = DistributedPubSub(system).mediator

      val (user, authId, authSid, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid)))

      val (user2, _, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

      def publish() = {
        val probe = TestProbe()

        val topics = Seq(
          s"messaging.messages.private.${user.id}",
          s"messaging.messages.private.${user2.id}"
        )

        topics foreach { topic ⇒
          mediator.tell(Subscribe(topic, Some("testProbe"), probe.ref), probe.ref)
          probe.expectMsg(SubscribeAck(Subscribe(topic, Some("testProbe"), probe.ref)))
        }

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi PubSub", Vector.empty, None))) { resp ⇒
          probe.expectMsgClass(classOf[PeerMessage])
          probe.expectMsgClass(classOf[PeerMessage])
        }
      }
    }

  }

}
