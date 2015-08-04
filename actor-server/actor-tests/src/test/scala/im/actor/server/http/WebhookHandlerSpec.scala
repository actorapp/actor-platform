package im.actor.server.http

import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.sequence.ResponseGetDifference
import org.scalatest.Inside._

import im.actor.api.rpc.{ Ok, ClientData }
import im.actor.api.rpc.messaging.{ UpdateMessage, TextMessage }
import im.actor.server._
import im.actor.server.api.http.json.Text
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.api.rpc.service.GroupsServiceHelpers
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceImpl, SequenceServiceConfig }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }

class WebhookHandlerSpec extends BaseAppSuite with GroupsServiceHelpers with MessageParsing with ImplicitGroupRegions {

  behavior of "WebhookHandler"

  it should "create group bot on group creation" in t.createGroupAndBot()

  it should "allow bot to send message to it's group" in t.sendInGroup()

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  val sequenceConfig = SequenceServiceConfig.load.toOption.get
  val sequenceService = new SequenceServiceImpl(sequenceConfig)

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
      val groupResponse = createGroup("Bot test group", Set(user2.id))
      val groupOutPeer = groupResponse.groupPeer
      val initSeq = groupResponse.seq
      val initState = groupResponse.state

      Thread.sleep(500)

      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { optBot ⇒
        optBot shouldBe defined
        val bot = optBot.get
        val firstMessage = Text("Alert! All tests are failed!")
        val (seq1, state1) = whenReady(new WebhooksHandler().send(firstMessage, bot.token)) { _ ⇒
          Thread.sleep(500) // Let peer managers write to db

          whenReady(sequenceService.handleGetDifference(initSeq, initState)) { resp ⇒
            inside(resp) {
              case Ok(ResponseGetDifference(_, _, _, updates, _, _)) ⇒
                updates should have length 1

                val update = UpdateMessage.parseFrom(CodedInputStream.newInstance(updates.head.update)).right.toOption.get
                update.message shouldEqual TextMessage(firstMessage.text, Vector.empty, None)
                update.senderUserId shouldEqual bot.userId
            }
            val diff = resp.toOption.get
            (diff.seq, diff.state)
          }
        }

        val secondMessage = Text("It's ok now!")
        whenReady(new WebhooksHandler().send(secondMessage, bot.token)) { _ ⇒
          Thread.sleep(500) // Let peer managers write to db

          whenReady(sequenceService.handleGetDifference(seq1, state1)) { resp ⇒
            inside(resp) {
              case Ok(ResponseGetDifference(_, _, _, updates, _, _)) ⇒
                updates should have length 1

                val update = UpdateMessage.parseFrom(CodedInputStream.newInstance(updates.head.update)).right.toOption.get
                update.message shouldEqual TextMessage(secondMessage.text, Vector.empty, None)
                update.senderUserId shouldEqual bot.userId
            }
          }
        }
      }
    }
  }

}
