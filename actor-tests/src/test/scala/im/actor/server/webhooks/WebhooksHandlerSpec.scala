package im.actor.server.webhooks

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.google.protobuf.CodedInputStream

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging.TextMessage
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.rpc.service.{ BaseServiceSuite, GroupsServiceHelpers, messaging }
import im.actor.server.models.Peer
import im.actor.server.persist
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager

class WebhooksHandlerSpec extends BaseServiceSuite with GroupsServiceHelpers {

  behavior of "Webhooks handler"

  it should "create group bot on group creation" in t.createGroupAndBot()

  it should "allow bot to send message to it's group" in t.sendInGroup()

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)

  implicit val service = new messaging.MessagingServiceImpl
  implicit val groupsService = new GroupsServiceImpl("")
  implicit val authService = buildAuthService()
  implicit val ec = system.dispatcher

  object t {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    def createGroupAndBot() = {
      val groupOutPeer = createGroup("Bot test group", Set(user2.id)).groupPeer

      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { optBot ⇒
        optBot shouldBe defined
        val bot = optBot.get
        bot.groupId shouldEqual groupOutPeer.groupId
      }
    }

    def sendInGroup() = {
      val groupOutPeer = createGroup("Bot test group", Set(user2.id)).groupPeer

      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { optBot ⇒
        optBot shouldBe defined
        val bot = optBot.get
        val firstMessage = Text("Alert! All tests are failed!")
        whenReady(new WebhookHandler(service).send(firstMessage, bot.token)) { _ ⇒
          whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { messages ⇒
            messages should have length 2
            val botMessage = messages.head
            botMessage.senderUserId shouldEqual bot.userId
            parseMessage(botMessage.messageContentData) shouldEqual Right(TextMessage(firstMessage.text, Vector.empty, None))
          }
        }

        val secondMessage = Text("It's ok now!")
        whenReady(new WebhookHandler(service).send(secondMessage, bot.token)) { _ ⇒
          whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { messages ⇒
            messages should have length 3
            val botMessage = messages.head
            botMessage.senderUserId shouldEqual bot.userId
            parseMessage(botMessage.messageContentData) shouldEqual Right(TextMessage(secondMessage.text, Vector.empty, None))
          }
        }
      }
    }

    private def parseMessage(body: Array[Byte]) = {
      val in = CodedInputStream.newInstance(body.drop(4))
      TextMessage.parseFrom(in)
    }

  }

}
