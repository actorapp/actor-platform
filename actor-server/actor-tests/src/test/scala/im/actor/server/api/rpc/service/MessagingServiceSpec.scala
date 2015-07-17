package im.actor.server.api.rpc.service

import scala.concurrent.Future
import scala.util.Random

import akka.contrib.pattern.DistributedPubSubMediator
import akka.testkit.TestProbe
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.google.protobuf.CodedInputStream

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ Peer, PeerType, UserOutPeer }
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.Events
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, UserEntity }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.ACLUtils
import im.actor.server.{ ImplicitFileStorageAdapter, BaseAppSuite, persist }

class MessagingServiceSpec extends BaseAppSuite with GroupsServiceHelpers with ImplicitFileStorageAdapter {
  behavior of "MessagingService"

  "Messaging" should "send messages" in s.privat.sendMessage

  it should "not repeat message sending with same authId and RandomId" in s.privat.cached

  it should "send group messages" in s.group.sendMessage

  it should "not send messages when user is not in group" in s.group.restrictAlienUser

  it should "publish messages in PubSub" in s.pubsub.publish

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  object s {
    implicit val ec = system.dispatcher

    implicit val sessionRegion = buildSessionRegionProxy()
    implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
    implicit val socialManagerRegion = SocialManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()
    implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
    implicit val privatePeerManagerRegion = UserEntity.startRegion()
    implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

    val groupInviteConfig = GroupInviteConfig("http://actor.im")
    val sequenceConfig = SequenceServiceConfig.load.toOption.get

    implicit val service = messaging.MessagingServiceImpl(mediator)
    implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
    val sequenceService = new SequenceServiceImpl(sequenceConfig)
    val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
    implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
    implicit val authService = buildAuthService()

    object privat {

      def sendMessage() = {
        val (user, user1AuthId1, _) = createUser()
        val user1AuthId2 = createAuthId(user.id)

        val sessionId = createSessionId()
        implicit val clientData = ClientData(user1AuthId1, sessionId, Some(user.id))

        val (user2, user2AuthId, _) = createUser()
        val user2Model = getUserModel(user2.id)
        val user2AccessHash = ACLUtils.userAccessHash(user1AuthId1, user2.id, user2Model.accessSalt)
        val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

        val randomId = Random.nextLong()

        whenReady(service.handleSendMessage(user2Peer, randomId, TextMessage("Hi Shiva", Vector.empty, None))) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSeqDate(1000, _, _)) ⇒
          }
        }

        whenReady(db.run(persist.sequence.SeqUpdate.find(user1AuthId1))) { updates ⇒
          updates.length shouldEqual 1

          val update = updates.head
          update.header shouldEqual UpdateMessageSent.header
          val seqUpdate = UpdateMessageSent.parseFrom(CodedInputStream.newInstance(update.serializedData)).right.toOption.get
          seqUpdate.peer shouldEqual Peer(PeerType.Private, user2.id)
          seqUpdate.randomId shouldEqual randomId
        }

        whenReady(db.run(persist.sequence.SeqUpdate.find(user1AuthId2))) { updates ⇒
          updates.length shouldEqual 1

          val update = updates.head
          update.header shouldEqual UpdateMessage.header
          val seqUpdate = UpdateMessage.parseFrom(CodedInputStream.newInstance(update.serializedData)).right.toOption.get
          seqUpdate.peer shouldEqual Peer(PeerType.Private, user2.id)
          seqUpdate.randomId shouldEqual randomId
          seqUpdate.senderUserId shouldEqual user.id
        }

        whenReady(db.run(persist.sequence.SeqUpdate.find(user2AuthId))) { updates ⇒
          updates.length shouldEqual 1

          val update = updates.head
          update.header shouldEqual UpdateMessage.header
          val seqUpdate = UpdateMessage.parseFrom(CodedInputStream.newInstance(update.serializedData)).right.toOption.get
          seqUpdate.peer shouldEqual Peer(PeerType.Private, user.id)
          seqUpdate.randomId shouldEqual randomId
          seqUpdate.senderUserId shouldEqual user.id
        }
      }

      def cached(): Unit = {
        val (user1, user1AuthId1, _) = createUser()
        val (user2, user2AuthId, _) = createUser()

        implicit val clientData1 = ClientData(user1AuthId1, createSessionId(), Some(user1.id))
        val clientData2 = ClientData(user2AuthId, createSessionId(), Some(user2.id))

        val user2Model = getUserModel(user2.id)
        val user2AccessHash = ACLUtils.userAccessHash(user1AuthId1, user2.id, user2Model.accessSalt)
        val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

        val randomId = Random.nextLong()
        val actions = Future.sequence(List(
          service.handleSendMessage(user2Peer, randomId, TextMessage("Hi Shiva", Vector.empty, None)),
          service.handleSendMessage(user2Peer, randomId, TextMessage("Hi Shiva", Vector.empty, None)),
          service.handleSendMessage(user2Peer, randomId, TextMessage("Hi Shiva", Vector.empty, None)),
          service.handleSendMessage(user2Peer, randomId, TextMessage("Hi Shiva", Vector.empty, None)),
          service.handleSendMessage(user2Peer, randomId, TextMessage("Hi Shiva", Vector.empty, None))
        ))

        whenReady(actions) { resps ⇒
          resps foreach (_ should matchPattern { case Ok(ResponseSeqDate(1000, _, _)) ⇒ })
        }

        whenReady(sequenceService.jhandleGetDifference(0, Array.empty, clientData1)) { result ⇒
          val respOption = result.toOption
          respOption shouldBe defined
          val resp = respOption.get

          val updates = resp.updates
          updates.length shouldEqual 1

          val message = UpdateMessageSent.parseFrom(CodedInputStream.newInstance(updates.last.update))
          message should matchPattern { case Right(_: UpdateMessageSent) ⇒ }
        }

        whenReady(sequenceService.jhandleGetDifference(0, Array.empty, clientData2)) { result ⇒
          val respOption = result.toOption
          respOption shouldBe defined
          val resp = respOption.get

          val updates = resp.updates
          updates.length shouldEqual 1

          val message = UpdateMessage.parseFrom(CodedInputStream.newInstance(updates.last.update))
          message should matchPattern { case Right(_: UpdateMessage) ⇒ }
        }
      }
    }

    object group {
      val (user1, user1AuthId1, _) = createUser()
      val user1AuthId2 = createAuthId(user1.id)

      val (user2, user2AuthId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(user1AuthId1, sessionId, Some(user1.id))

      val groupOutPeer = createGroup("Fun group", Set(user2.id)).groupPeer

      def sendMessage() = {
        val randomId = Random.nextLong()

        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, randomId, TextMessage("Hi again", Vector.empty, None))) { resp ⇒
          resp should matchPattern {
            case Ok(ResponseSeqDate(1001, _, _)) ⇒
          }
        }

        whenReady(db.run(persist.sequence.SeqUpdate.findLast(user1AuthId1))) { updateOpt ⇒
          val update = updateOpt.get
          update.header should ===(UpdateMessageSent.header)

          val seqUpdate = UpdateMessageSent.parseFrom(CodedInputStream.newInstance(update.serializedData)).right.toOption.get
          seqUpdate.peer shouldEqual Peer(PeerType.Group, groupOutPeer.groupId)
          seqUpdate.randomId shouldEqual randomId
        }

        whenReady(db.run(persist.sequence.SeqUpdate.findLast(user1AuthId2))) { updateOpt ⇒
          val update = updateOpt.get
          update.header should ===(UpdateMessage.header)

          val seqUpdate = UpdateMessage.parseFrom(CodedInputStream.newInstance(update.serializedData)).right.toOption.get
          seqUpdate.peer shouldEqual Peer(PeerType.Group, groupOutPeer.groupId)
          seqUpdate.randomId shouldEqual randomId
          seqUpdate.senderUserId shouldEqual user1.id
        }

        whenReady(db.run(persist.sequence.SeqUpdate.findLast(user2AuthId))) { updateOpt ⇒
          val update = updateOpt.get
          update.header should ===(UpdateMessage.header)

          val seqUpdate = UpdateMessage.parseFrom(CodedInputStream.newInstance(update.serializedData)).right.toOption.get
          seqUpdate.peer shouldEqual Peer(PeerType.Group, groupOutPeer.groupId)
          seqUpdate.randomId shouldEqual randomId
          seqUpdate.senderUserId shouldEqual user1.id
        }
      }

      def restrictAlienUser() = {
        val (alien, authIdAlien, _) = createUser()

        val alienClientData = ClientData(user1AuthId1, sessionId, Some(alien.id))

        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), TextMessage("Hi again", Vector.empty, None))(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        whenReady(groupsService.handleEditGroupTitle(groupOutPeer, 4L, "Loosers")(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        val (user3, authId3, _) = createUser()
        val user3OutPeer = UserOutPeer(user3.id, 11)

        whenReady(groupsService.handleInviteUser(groupOutPeer, 4L, user3OutPeer)(alienClientData)) { resp ⇒
          resp should matchNotAuthorized
        }

        val fileLocation = FileLocation(1L, 1L)
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
    }

    object pubsub {
      import DistributedPubSubMediator._

      val (user, authId, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(user.id))

      val (user2, _, _) = createUser()
      val user2Model = getUserModel(user2.id)
      val user2AccessHash = ACLUtils.userAccessHash(authId, user2.id, user2Model.accessSalt)
      val user2Peer = peers.OutPeer(PeerType.Private, user2.id, user2AccessHash)

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

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), TextMessage("Hi PubSub", Vector.empty, None))) { resp ⇒
          probe.expectMsgClass(classOf[Events.PeerMessage])
          probe.expectMsgClass(classOf[Events.PeerMessage])
        }
      }
    }
  }
}
