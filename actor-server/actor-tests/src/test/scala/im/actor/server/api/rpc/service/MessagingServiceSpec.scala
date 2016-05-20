package im.actor.server.api.rpc.service

import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.testkit.TestProbe
import cats.data.Xor
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.api.rpc.sequence.{ ApiUpdateContainer, ResponseGetDifference }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingRpcErors
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.pubsub.PeerMessage

import scala.concurrent.Future
import scala.util.Random

class MessagingServiceSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with ImplicitSequenceService
  with ImplicitSessionRegion
  with ImplicitAuthService
  with SeqUpdateMatchers
  with MessageParsing {
  behavior of "MessagingService"

  "Private Messaging" should "send messages" in s.privat.sendMessage

  it should "not repeat message sending with same authId and RandomId" in s.privat.cached

  "Group Messaging" should "send messages" in s.group.sendMessage

  it should "not send messages when user is not in group" in s.group.restrictAlienUser

  it should "publish messages in PubSub" in s.pubsub.publish

  it should "not repeat message sending with same authId and RandomId" in s.group.cached

  it should "allow to edit last own message in public group" in s.group.editPublic

  "Any Messaging" should "keep original order of sent messages" in s.generic.rightOrder

  it should "allow user to edit own message" in s.generic.editOwnMessage

  it should "not allow user to edit alien messages" in s.generic.notEditAlienMessage

  it should "keep randomId unique inside single dialog" in s.generic.uniqueRandomId

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
        val clientData11 = ClientData(user1AuthId1, sessionId, Some(AuthData(user1.id, user1AuthSid1, 42)))
        val clientData12 = ClientData(user1AuthId2, sessionId, Some(AuthData(user1.id, user1AuthSid2, 42)))
        val clientData2 = ClientData(user2AuthId, sessionId, Some(AuthData(user2.id, user2AuthSid, 42)))

        val randomId = Random.nextLong()

        {
          implicit val clienData = clientData11

          whenReady(service.handleSendMessage(user2Peer, randomId, ApiTextMessage("Hi Shiva", Vector.empty, None), None, None)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseSeqDate(_, _, _)) ⇒
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

          expectUpdatesUnordered(classOf[UpdateChatGroupsChanged], classOf[UpdateMessage], classOf[UpdateCountersChanged]) {
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

        val clientData1 = ClientData(user1AuthId, createSessionId(), Some(AuthData(user1.id, user1AuthSid, 42)))
        val clientData2 = ClientData(user2AuthId, createSessionId(), Some(AuthData(user2.id, user2AuthSid, 42)))

        val user2Model = getUserModel(user2.id)
        val user2AccessHash = ACLUtils.userAccessHash(user1AuthId, user2.id, user2Model.accessSalt)
        val user2Peer = peers.ApiOutPeer(ApiPeerType.Private, user2.id, user2AccessHash)

        {
          implicit val clientData = clientData1

          val randomId = Random.nextLong()
          val text = "Hi Shiva"
          val actions = Future.sequence(List(
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(user2Peer, randomId, ApiTextMessage(text, Vector.empty, None), None, None)
          ))

          whenReady(actions) { resps ⇒
            resps foreach (_ should matchPattern { case Ok(ResponseSeqDate(_, _, _)) ⇒ })
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

      val clientData11 = ClientData(user1AuthId1, sessionId, Some(AuthData(user1.id, user1AuthSid1, 42)))
      val clientData12 = ClientData(user1AuthId2, sessionId, Some(AuthData(user1.id, user1AuthSid2, 42)))
      val clientData2 = ClientData(user2AuthId, sessionId, Some(AuthData(user2.id, user2AuthSid, 42)))

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

          whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, randomId, ApiTextMessage("Hi again", Vector.empty, None), None, None)) { resp ⇒
            resp should matchPattern {
              case Ok(ResponseSeqDate(_, _, _)) ⇒
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

        val alienClientData = ClientData(user1AuthId1, sessionId, Some(AuthData(alien.id, authSidAlien, 42)))

        whenReady(service.handleSendMessage(groupOutPeer.asOutPeer, Random.nextLong(), ApiTextMessage("Hi again", Vector.empty, None), None, None)(alienClientData)) { resp ⇒
          resp should matchForbidden
        }

        whenReady(groupsService.handleEditGroupTitle(groupOutPeer, 4L, "Loosers", Vector.empty)(alienClientData)) { resp ⇒
          resp should matchForbidden
        }

        val (user3, authId3, _, _) = createUser()
        val user3OutPeer = ApiUserOutPeer(user3.id, 11)

        whenReady(groupsService.handleInviteUser(groupOutPeer, 4L, user3OutPeer, Vector.empty)(alienClientData)) { resp ⇒
          resp should matchForbidden
        }

        val fileLocation = ApiFileLocation(1L, 1L)
        whenReady(groupsService.handleEditGroupAvatar(groupOutPeer, 5L, fileLocation, Vector.empty)(alienClientData)) { resp ⇒
          resp should matchForbidden
        }

        whenReady(groupsService.handleRemoveGroupAvatar(groupOutPeer, 5L, Vector.empty)(alienClientData)) { resp ⇒
          resp should matchForbidden
        }

        whenReady(groupsService.handleLeaveGroup(groupOutPeer, 5L, Vector.empty)(alienClientData)) { resp ⇒
          resp should matchForbidden
        }

      }

      def cached(): Unit = {
        val (user1, user1AuthId, user1AuthSid, _) = createUser()
        val (user2, user2AuthId, user2AuthSid, _) = createUser()
        val sessionId = createSessionId()
        val clientData1 = ClientData(user1AuthId, sessionId, Some(AuthData(user1.id, user1AuthSid, 42)))
        val clientData2 = ClientData(user2AuthId, sessionId, Some(AuthData(user2.id, user2AuthSid, 42)))

        val group2OutPeer = {
          implicit val clientData = clientData1
          createGroup("Fun group 2", Set(user2.id)).groupPeer
        }

        {
          implicit val clientData = clientData1

          val randomId = Random.nextLong()
          val text = "Hi Shiva"
          val actions = Future.sequence(List(
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None), None, None),
            service.handleSendMessage(group2OutPeer.asOutPeer, randomId, ApiTextMessage(text, Vector.empty, None), None, None)
          ))

          whenReady(actions) { resps ⇒
            resps foreach (_ should matchPattern { case Ok(ResponseSeqDate(_, _, _)) ⇒ })
          }

          expectUpdate(classOf[UpdateMessageSent])(identity)
        }

        {
          implicit val clientData = clientData2
          expectUpdate(classOf[UpdateMessage])(identity)
          expectUpdate(classOf[UpdateCountersChanged])(identity)
        }
      }

      def editPublic(): Unit = {
        val sessionId = createSessionId()
        val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
        val (bob, bobAuthId, bobAuthSid, _) = createUser()
        val aliceClient = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))
        val bobClient = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))

        val WrongText = "hello everrryoine"
        val CorrectText = "Hello everyone!"

        val groupPeer = {
          implicit val cd = aliceClient
          val peer = createPubGroup("Test public group", "Group to test message edit", Set(bob.id)).groupPeer
          ApiOutPeer(ApiPeerType.Group, peer.groupId, peer.accessHash)
        }

        val messRandomId = {
          implicit val cd = aliceClient
          sendMessageToGroup(groupPeer.id, ApiTextMessage(WrongText, Vector.empty, None))
        }

        // Alice can edit her own message
        {
          implicit val cd = aliceClient
          whenReady(service.handleUpdateMessage(groupPeer, messRandomId, ApiTextMessage(CorrectText, Vector.empty, None))) { resp ⇒
            resp should matchPattern {
              case Xor.Right(ResponseSeqDate(_, _, _)) ⇒
            }
          }
          expectUpdate(classOf[UpdateMessageContentChanged]) { upd ⇒
            upd.randomId shouldEqual messRandomId
            upd.peer shouldEqual groupPeer.asPeer
            inside(parseMessage(upd.message.toByteArray)) {
              case Right(ApiTextMessage(CorrectText, _, _)) ⇒
            }
          }
        }

        {
          implicit val cd = bobClient
          expectUpdate(classOf[UpdateMessageContentChanged]) { upd ⇒
            upd.randomId shouldEqual messRandomId
            upd.peer shouldEqual groupPeer.asPeer
            inside(parseMessage(upd.message.toByteArray)) {
              case Right(ApiTextMessage(CorrectText, _, _)) ⇒
            }
          }
        }

        val aliceSeq = getCurrentSeq(aliceClient)
        val bobSeq = getCurrentSeq(bobClient)

        // Bob can't edit alice's message
        {
          implicit val cd = bobClient
          whenReady(service.handleUpdateMessage(groupPeer, messRandomId, ApiTextMessage("Som other text for message", Vector.empty, None))) { resp ⇒
            resp should matchForbidden
          }
          expectNoUpdate(bobSeq, classOf[UpdateMessageContentChanged])
        }

        {
          implicit val cd = aliceClient
          expectNoUpdate(aliceSeq, classOf[UpdateMessageContentChanged])
        }

      }
    }

    object pubsub {

      import DistributedPubSubMediator._

      val mediator = DistributedPubSub(system).mediator

      val (user, authId, authSid, _) = createUser()
      val sessionId = createSessionId()
      implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

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

        whenReady(service.handleSendMessage(user2Peer, Random.nextLong(), ApiTextMessage("Hi PubSub", Vector.empty, None), None, None)) { resp ⇒
          probe.expectMsgClass(classOf[PeerMessage])
          probe.expectMsgClass(classOf[PeerMessage])
        }
      }
    }

    object generic {

      def rightOrder() = {
        val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
        val (bob, bobAuthId, bobAuthSid, _) = createUser()

        val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
        val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

        val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
        val bobOutPeer = whenReady(ACLUtils.getOutPeer(bobPeer, aliceAuthId))(identity)

        def sendMessageToAlice(text: String): Future[ResponseSeqDate] = {
          implicit val clientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))
          service.handleSendMessage(aliceOutPeer, ACLUtils.randomLong(), textMessage(text), None, None) map (_.toOption.get)
        }

        val toAlice = for (i ← 1 to 100) yield sendMessageToAlice(i.toString)

        toAlice foreach { whenReady(_)(identity) }

        {
          implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

          whenReady(service.handleLoadHistory(bobOutPeer, 0L, None, Int.MaxValue, Vector.empty)) { resp ⇒
            inside(resp) {
              case Ok(ResponseLoadHistory(history, _, _, _, _)) ⇒
                val textMessages = history map { e ⇒
                  val parsed = parseMessage(e.message.toByteArray)
                  inside(parsed) {
                    case Right(_: ApiTextMessage) ⇒
                  }
                  val message = parsed.right.get
                  message shouldBe an[ApiTextMessage]
                  message.asInstanceOf[ApiTextMessage]
                }
                checkMessageOrder(textMessages)
            }
          }

          whenReady(sequenceService.handleGetDifference(0, Array.empty, Vector.empty)) { resp ⇒
            inside(resp) {
              case Ok(diff: ResponseGetDifference) ⇒
                val textMessages = diff.updates collect {
                  case ApiUpdateContainer(UpdateMessage.header, bytes) ⇒
                    val parsed = UpdateMessage.parseFrom(bytes)
                    parsed should matchPattern {
                      case Right(_) ⇒
                    }
                    val message = parsed.right.get.message
                    message shouldBe an[ApiTextMessage]
                    message.asInstanceOf[ApiTextMessage]
                }
                checkMessageOrder(textMessages)
            }
          }

        }

        def checkMessageOrder(textMessages: IndexedSeq[ApiTextMessage]) = {
          textMessages should have length 100
          (textMessages foldLeft 0) {
            case (acc, el) ⇒
              val intValue = el.text.toInt
              if (intValue > acc) {} else { fail(s"order of elements was wrong: ${textMessages map (_.text) mkString ", "}") }
              intValue
          }
        }

      }

      def notEditAlienMessage() = {
        val sessionId = createSessionId()
        val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
        val (bob, bobAuthId, bobAuthSid, _) = createUser()

        val aliceCD = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))
        val bobCD = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))

        val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
        val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

        val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
        val bobOutPeer = whenReady(ACLUtils.getOutPeer(bobPeer, aliceAuthId))(identity)

        val messRandomId = {
          implicit val cd = aliceCD
          sendMessageToUser(bob.id, ApiTextMessage("hello bob", Vector.empty, None))
        }

        {
          implicit val cd = bobCD
          whenReady(service.handleUpdateMessage(aliceOutPeer, messRandomId, ApiTextMessage("XXXXXXXXX", Vector.empty, None))) { resp ⇒
            resp should matchForbidden
          }
          expectNoUpdate(0, classOf[UpdateMessageContentChanged])
        }

        {
          implicit val cd = aliceCD
          expectNoUpdate(0, classOf[UpdateMessageContentChanged])
        }

        val messages = for {
          a ← HistoryMessageRepo.findNewest(alice.id, bobPeer.asModel)
          b ← HistoryMessageRepo.findNewest(bob.id, alicePeer.asModel)
        } yield List(a, b).flatten

        whenReady(db.run(messages)) { messages ⇒
          messages should have length 2
          messages foreach { mess ⇒
            inside(parseMessage(mess.messageContentData)) {
              case Right(ApiTextMessage("hello bob", _, _)) ⇒
            }
          }
        }
      }

      def editOwnMessage() = {
        val sessionId = createSessionId()
        val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
        val (bob, bobAuthId, bobAuthSid, _) = createUser()

        val aliceCD = ClientData(aliceAuthId, sessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))
        val bobCD = ClientData(bobAuthId, sessionId, Some(AuthData(bob.id, bobAuthSid, 42)))

        val alicePeer = ApiPeer(ApiPeerType.Private, alice.id)
        val bobPeer = ApiPeer(ApiPeerType.Private, bob.id)

        val aliceOutPeer = whenReady(ACLUtils.getOutPeer(alicePeer, bobAuthId))(identity)
        val bobOutPeer = whenReady(ACLUtils.getOutPeer(bobPeer, aliceAuthId))(identity)

        val messRandomId = {
          implicit val cd = aliceCD
          sendMessageToUser(bob.id, ApiTextMessage("hello bob", Vector.empty, None))
        }

        {
          implicit val cd = aliceCD
          whenReady(service.handleUpdateMessage(bobOutPeer, messRandomId, ApiTextMessage("XXXXXXXXX", Vector.empty, None))) { resp ⇒
            resp should matchPattern {
              case Xor.Right(ResponseSeqDate(_, _, _)) ⇒
            }
            //            resp.toOption.get.seq
          }
          expectUpdate(classOf[UpdateMessageContentChanged]) { upd ⇒
            upd.randomId shouldEqual messRandomId
            upd.peer shouldEqual bobPeer
            inside(parseMessage(upd.message.toByteArray)) {
              case Right(ApiTextMessage("XXXXXXXXX", _, _)) ⇒
            }
          }
        }

        {
          implicit val cd = bobCD
          expectUpdate(classOf[UpdateMessageContentChanged]) { upd ⇒
            upd.randomId shouldEqual messRandomId
            upd.peer shouldEqual alicePeer
            inside(parseMessage(upd.message.toByteArray)) {
              case Right(ApiTextMessage("XXXXXXXXX", _, _)) ⇒
            }
          }
        }

        val messages = for {
          a ← HistoryMessageRepo.findNewest(alice.id, bobPeer.asModel)
          b ← HistoryMessageRepo.findNewest(bob.id, alicePeer.asModel)
        } yield List(a, b).flatten

        whenReady(db.run(messages)) { messages ⇒
          messages should have length 2
          messages foreach { mess ⇒
            inside(parseMessage(mess.messageContentData)) {
              case Right(ApiTextMessage("XXXXXXXXX", _, _)) ⇒
            }
          }
        }
      }

      def uniqueRandomId() = {
        val (alice, aliceAuthId1, aliceAuthSid1, _) = createUser()
        val (aliceAuthId2, aliceAuthSid2) = createAuthId(alice.id)

        val (bob, bobAuthId, bobAuthSid, _) = createUser()

        val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))
        val aliceClientData1 = ClientData(aliceAuthId1, 1, Some(AuthData(alice.id, aliceAuthSid1, 42)))
        val aliceClientData2 = ClientData(aliceAuthId2, 1, Some(AuthData(alice.id, aliceAuthSid2, 42)))

        val RandomId = 22L

        {
          implicit val cd = aliceClientData1
          whenReady(service.handleSendMessage(
            getOutPeer(bob.id, aliceAuthId1),
            RandomId,
            ApiTextMessage("Hello from device number one", Vector.empty, None),
            None,
            None
          )) { resp ⇒
            resp should matchPattern {
              case Ok(_) ⇒
            }
          }
        }

        val bobSeq = getCurrentSeq(bobClientData)

        {
          implicit val cd = aliceClientData2
          whenReady(service.handleSendMessage(
            getOutPeer(bob.id, aliceAuthId2),
            RandomId,
            ApiTextMessage("Hello from second device with same random id", Vector.empty, None),
            None,
            None
          )) { resp ⇒
            inside(resp) {
              case Error(MessagingRpcErors.NotUniqueRandomId) ⇒
            }
          }
        }

        {
          implicit val cd = bobClientData
          expectNoUpdate(bobSeq, classOf[UpdateMessage])
          expectNoUpdate(bobSeq, classOf[UpdateCountersChanged])
        }

        val aliceSeq1 = getCurrentSeq(aliceClientData1)
        val aliceSeq2 = getCurrentSeq(aliceClientData2)

        {
          implicit val cd = bobClientData
          whenReady(service.handleSendMessage(
            getOutPeer(alice.id, bobAuthId),
            RandomId,
            ApiTextMessage("Hello you back, and same random id again", Vector.empty, None),
            None,
            None
          )) { resp ⇒
            inside(resp) {
              case Error(MessagingRpcErors.NotUniqueRandomId) ⇒
            }
          }
        }

        {
          implicit val cd = aliceClientData1
          expectNoUpdate(aliceSeq1, classOf[UpdateMessage])
          expectNoUpdate(aliceSeq1, classOf[UpdateCountersChanged])
        }

        {
          implicit val cd = aliceClientData2
          expectNoUpdate(aliceSeq2, classOf[UpdateMessage])
          expectNoUpdate(aliceSeq2, classOf[UpdateCountersChanged])
        }

      }

    }

  }

}
