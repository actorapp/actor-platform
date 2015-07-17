package im.actor.server.http

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging.TextMessage
import im.actor.server.api.http.json.Text
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.api.rpc.service.GroupsServiceHelpers
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.models.Peer
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.peermanagers.{ GroupPeerManager, UserEntity }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.{ ImplicitFileStorageAdapter, BaseAppSuite, MessageParsing, persist }

class WebhookHandlerSpec extends BaseAppSuite with GroupsServiceHelpers with MessageParsing with ImplicitFileStorageAdapter {

  behavior of "WebhookHandler"

  it should "create group bot on group creation" in t.createGroupAndBot()

  it should "allow bot to send message to it's group" in t.sendInGroup()

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val privatePeerManagerRegion = UserEntity.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authService = buildAuthService()

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
        whenReady(new WebhooksHandler().send(firstMessage, bot.token)) { _ ⇒
          Thread.sleep(100) // Let peer managers write to db

          whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { messages ⇒
            messages should have length 2
            val botMessage = messages.head
            botMessage.senderUserId shouldEqual bot.userId
            parseMessage(botMessage.messageContentData) shouldEqual Right(TextMessage(firstMessage.text, Vector.empty, None))
          }
        }

        val secondMessage = Text("It's ok now!")
        whenReady(new WebhooksHandler().send(secondMessage, bot.token)) { _ ⇒
          Thread.sleep(100) // Let peer managers write to db

          whenReady(db.run(persist.HistoryMessage.find(user1.id, Peer.group(groupOutPeer.groupId)))) { messages ⇒
            messages should have length 3
            val botMessage = messages.head
            botMessage.senderUserId shouldEqual bot.userId
            parseMessage(botMessage.messageContentData) shouldEqual Right(TextMessage(secondMessage.text, Vector.empty, None))
          }
        }
      }
    }
  }

}
