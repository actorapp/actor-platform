package im.actor.server.http

import im.actor.api.rpc.ClientData
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.messaging.{ TextMessage, UpdateMessage }
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server._
import im.actor.server.api.http.json.Text
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.api.rpc.service.GroupsServiceHelpers
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.group.GroupOffice
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }

class WebhookHandlerSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with MessageParsing
  with ImplicitGroupRegions
  with ImplicitSequenceService
  with ImplicitAuthService
  with SequenceMatchers {

  behavior of "WebhookHandler"

  it should "create group bot on group creation" in t.createGroupAndBot()

  it should "allow bot to send message to it's group" in t.sendInGroup()

  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")
  implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)

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
      val handler = new WebhooksHandler()

      val groupResponse = createGroup("Bot test group", Set(user2.id))
      val groupOutPeer = groupResponse.groupPeer
      val initSeq = groupResponse.seq
      val initState = groupResponse.state

      Thread.sleep(1000)

      val token = whenReady(GroupOffice.getIntegrationToken(groupOutPeer.groupId, user1.id)) { optToken ⇒
        optToken shouldBe defined
        optToken.get
      }

      val firstMessage = Text("Alert! All tests are failed!")
      whenReady(handler.send(firstMessage, token)) { _ ⇒
        expectUpdatesUnordered(failUnmatched)(initSeq, initState, Set(UpdateMessage.header, UpdateCountersChanged.header)) {
          case (UpdateMessage.header, u) ⇒
            val update = parseUpdate[UpdateMessage](u)
            update.message shouldEqual TextMessage(firstMessage.text, Vector.empty, None)
          case (UpdateCountersChanged.header, update) ⇒ parseUpdate[UpdateCountersChanged](update)
        }
      }

      val (seq1, state1) = whenReady(sequenceService.handleGetState()) { resp ⇒
        val ResponseSeq(seq, state) = resp.toOption.get
        (seq, state)
      }

      val secondMessage = Text("It's ok now!")
      whenReady(handler.send(secondMessage, token)) { _ ⇒
        expectUpdatesUnordered(failUnmatched)(seq1, state1, Set(UpdateMessage.header, UpdateCountersChanged.header)) {
          case (UpdateMessage.header, u) ⇒
            val update = parseUpdate[UpdateMessage](u)
            update.message shouldEqual TextMessage(secondMessage.text, Vector.empty, None)
          case (UpdateCountersChanged.header, update) ⇒ parseUpdate[UpdateCountersChanged](update)
        }
      }
    }
  }

}
